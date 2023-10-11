package ru.practicum.event.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventUserRequestDto;
import ru.practicum.event.service.EventService;
import ru.practicum.request.dto.EventRequestStatusUpdateRequestDto;
import ru.practicum.request.dto.EventRequestStatusUpdateResultDto;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.service.RequestService;
import ru.practicum.validation.ValidationGroup;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@Slf4j
@Validated
@RequestMapping("/users/{userId}/events")
public class EventController {
    private final EventService eventService;
    private final RequestService requestService;

    @Autowired
    public EventController(EventService eventService, RequestService requestService) {
        this.eventService = eventService;
        this.requestService = requestService;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @Validated(ValidationGroup.AddEvent.class)
    @PostMapping
    public EventFullDto addEvent(@Valid @RequestBody NewEventDto newEventDto,
                                 @PathVariable(name = "userId") Long id) {
        if (newEventDto.getRequestModeration() == null) {
            newEventDto.setRequestModeration(true);
        }
        EventFullDto eventFull = eventService.addEvent(newEventDto, id);
        log.info("User id ={} created a new event id = {}", id, eventFull.getId());
        return eventFull;
    }

    @GetMapping
    public List<EventShortDto> findEvents(@PathVariable(name = "userId") Long id,
                                          @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                          @RequestParam(defaultValue = "10") @Positive int size) {
        List<EventShortDto> eventShort = eventService.findEvents(id, from, size);
        log.info("List of user events id {}", id);
        return eventShort;
    }

    @GetMapping("/{eventId}")
    public EventFullDto findEvent(@PathVariable(name = "userId") Long idUser,
                                  @PathVariable(name = "eventId") Long idEvent) {
        EventFullDto eventFull = eventService.findEvent(idUser, idEvent);
        log.info("Receiving data by event id = {}", idEvent);
        return eventFull;
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@PathVariable(name = "userId") Long idUser,
                                    @PathVariable(name = "eventId") Long idEven,
                                    @RequestBody @Valid UpdateEventUserRequestDto updateEventUserRequestDto) {
        EventFullDto eventFull = eventService.updateEvent(idUser, idEven, updateEventUserRequestDto);
        log.info("User id = {}, changed the event id = {}", idUser, idEven);
        return eventFull;
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> findRequestsByUsersEvent(@PathVariable(name = "userId") Long idUser,
                                                                  @PathVariable(name = "eventId") Long idEvent) {
        List<ParticipationRequestDto> participationRequest = requestService.findRequestsByUsersEvent(idEvent, idUser);
        log.info("Getting a list of requests for an event id = {}, user id = {}", idEvent, idUser);
        return participationRequest;
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResultDto updateStatusRequests(@PathVariable(name = "userId") Long idUser,
                                                                  @PathVariable(name = "eventId") Long idEvent,
                                                                  @RequestBody EventRequestStatusUpdateRequestDto statusUpdateRequest) {
        EventRequestStatusUpdateResultDto eventRequestStatusUpdateResult = requestService.updateStatusRequests(idUser, idEvent, statusUpdateRequest);
        log.info("User id = {} changes the status of requests for an event id = {}.", idUser, idEvent);
        return eventRequestStatusUpdateResult;
    }
}