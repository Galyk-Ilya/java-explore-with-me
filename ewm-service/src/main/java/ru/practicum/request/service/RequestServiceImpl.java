package ru.practicum.request.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.error.exceptions.NotFoundException;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.request.dto.EventRequestStatusUpdateRequestDto;
import ru.practicum.request.dto.EventRequestStatusUpdateResultDto;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.dto.RequestUpdateStatus;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Autowired
    public RequestServiceImpl(RequestRepository requestRepository,
                              EventRepository eventRepository,
                              UserRepository userRepository) {
        this.requestRepository = requestRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    @Override
    public ParticipationRequestDto createRequest(Long userId, Long eventId, LocalDateTime time) {
        Event event = eventRepository.lockById(eventId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("There is no user with this id"));
        if (requestRepository.existsByRequester_IdAndEvent_Id(userId, eventId)) {
            throw new DataIntegrityViolationException("You have already applied to participate in this event.");
        }

        if (event.getInitiator().getId().equals(userId)) {
            throw new DataIntegrityViolationException("You cannot enter an event that you have organized");
        }
        if (event.getState().equals(EventState.PENDING)) {
            throw new DataIntegrityViolationException("Applications are not accepted for an event that has not yet been published.");
        }

        if (event.getParticipantLimit() != 0 && event.getConfirmedRequests() >= event.getParticipantLimit()) {
            throw new DataIntegrityViolationException("The application limit has been exceeded. Acceptance of applications has been suspended");
        }

        RequestStatus status = null;
        if (event.getRequestModeration()) {
            status = RequestStatus.PENDING;
        }
        if (event.getRequestModeration() == null || !event.getRequestModeration() ||
                event.getParticipantLimit() == 0) {
            status = RequestStatus.CONFIRMED;
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
        }

        return RequestMapper.toParticipationRequestDto(requestRepository.save(ParticipationRequest.builder()
                .created(time)
                .event(event)
                .requester(user)
                .status(status)
                .build()));
    }

    @Override
    @Transactional
    public ParticipationRequestDto canceledRequest(Long userId, Long requestId) {
        ParticipationRequest participation = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("The request is missing or cannot be edited."));
        participation.setStatus(RequestStatus.CANCELED);
        return RequestMapper.toParticipationRequestDto(requestRepository.save(participation));
    }

    @Override
    public List<ParticipationRequestDto> findRequests(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("You cannot cancel an already approved application.");
        }
        return requestRepository.findByRequester_Id(userId).stream()
                .map(RequestMapper::toParticipationRequestDto).collect(Collectors.toList());
    }

    @Override
    public List<ParticipationRequestDto> findRequestsByUsersEvent(Long idEvent, Long idUser) {
        if (!userRepository.existsById(idUser)) {
            throw new NotFoundException("You cannot cancel an already approved application.");
        }

        if (!eventRepository.existsById(idEvent)) {
            throw new NotFoundException("No such event exists");
        }
        return requestRepository.findByEvent_Id(idEvent).stream()
                .map(RequestMapper::toParticipationRequestDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResultDto updateStatusRequests(Long idUser, Long idEvent,
                                                                  EventRequestStatusUpdateRequestDto statusUpdateRequest) {
        Event event = eventRepository.lockById(idEvent);
        if (!userRepository.existsById(idUser)) {
            throw new NotFoundException("You cannot cancel an already approved application.");
        }

        List<ParticipationRequest> requestsForChange = requestRepository
                .findAllById(statusUpdateRequest.getRequestIds());


        List<ParticipationRequest> confirmedRequestsForAns = new ArrayList<>();
        List<ParticipationRequest> rejectedRequestsForAns = new ArrayList<>();

        List<ParticipationRequest> confirmedRequestsForUp = new ArrayList<>();
        List<ParticipationRequest> rejectedRequestsForUp = new ArrayList<>();

        if (statusUpdateRequest.getStatus().equals(RequestUpdateStatus.REJECTED)) {
            for (ParticipationRequest request : requestsForChange) {
                if (request.getStatus().equals(RequestStatus.CONFIRMED)) {
                    throw new DataIntegrityViolationException("You cannot cancel an already approved application.");
                }
                rejectedRequestsForAns.add(request);
                request.setStatus(RequestStatus.REJECTED);
                rejectedRequestsForUp.add(request);
            }
        } else {
            if (event.getRequestModeration() == null || event.getRequestModeration()
                    .equals(false) || event.getParticipantLimit().equals(0)) {
                return EventRequestStatusUpdateResultDto.builder()
                        .confirmedRequests(requestRepository.findAllById(statusUpdateRequest.getRequestIds())
                                .stream().map(RequestMapper::toParticipationRequestDto).collect(Collectors.toList()))
                        .rejectedRequests(new ArrayList<>()).build();
            }
            if (event.getParticipantLimit() <= event.getConfirmedRequests()) {
                throw new DataIntegrityViolationException("The application limit has been reached. Applications are not accepted");
            }
            for (ParticipationRequest request : requestsForChange) {
                if (event.getParticipantLimit() > event.getConfirmedRequests()) {
                    confirmedRequestsForAns.add(request);
                    request.setStatus(RequestStatus.CONFIRMED);
                    confirmedRequestsForUp.add(request);
                } else {
                    if (request.getStatus().equals(RequestStatus.CONFIRMED)) {
                        throw new DataIntegrityViolationException("You cannot cancel an already approved application.");
                    }
                    rejectedRequestsForAns.add(request);
                    request.setStatus(RequestStatus.REJECTED);
                    rejectedRequestsForUp.add(request);
                }
            }
        }
        int plus = event.getConfirmedRequests();
        event.setConfirmedRequests(plus + confirmedRequestsForUp.size());

        return EventRequestStatusUpdateResultDto.builder()
                .confirmedRequests(confirmedRequestsForAns.stream().map(RequestMapper::toParticipationRequestDto).collect(Collectors.toList()))
                .rejectedRequests(rejectedRequestsForAns.stream().map(RequestMapper::toParticipationRequestDto).collect(Collectors.toList())).build();
    }
}