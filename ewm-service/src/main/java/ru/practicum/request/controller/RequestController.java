package ru.practicum.request.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.service.RequestService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/users/{userId}/requests")
public class RequestController {

    private final RequestService requestService;

    @Autowired
    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(@PathVariable("userId") Long userId,
                                                 @RequestParam("eventId") Long eventId) {
        ParticipationRequestDto participationRequest = requestService.createRequest(userId, eventId, LocalDateTime.now());
        log.info("User with id {}, creates a request to participate in an event id {}.", userId, eventId);
        return participationRequest;
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto canceledRequest(@PathVariable("userId") Long userId,
                                                   @PathVariable("requestId") Long requestId) {
        ParticipationRequestDto participationRequest = requestService.canceledRequest(userId, requestId);
        log.info("User with = {}, cancels his application id = {}.", userId, requestId);
        return participationRequest;
    }

    @GetMapping
    public List<ParticipationRequestDto> findRequests(@PathVariable("userId") Long userId) {
        List<ParticipationRequestDto> participationRequest = requestService.findRequests(userId);
        log.info("Getting a list of user requests id = {}.", userId);
        return participationRequest;
    }
}