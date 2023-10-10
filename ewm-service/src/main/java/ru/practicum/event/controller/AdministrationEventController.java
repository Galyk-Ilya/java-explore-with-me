package ru.practicum.event.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.UpdateEventAdminRequestDto;
import ru.practicum.event.model.EventState;
import ru.practicum.event.service.EventService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@Slf4j
@Validated
@RequestMapping("/admin/events")
public class AdministrationEventController {
    private final EventService eventService;

    @Autowired
    public AdministrationEventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public List<EventFullDto> findEventsForAdmin(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<EventState> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) String rangeStart,
            @RequestParam(required = false) String rangeEnd,
            @RequestParam(required = false, defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(required = false, defaultValue = "10") @Positive int size) {
        List<EventFullDto> eventFull = eventService.findEventsForAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
        log.info("Retrieving a list of events by search parameters");
        return eventFull;
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventForAdmin(@PathVariable Long eventId,
                                            @RequestBody @Valid UpdateEventAdminRequestDto updateEventAdminRequestDto) {
        EventFullDto eventFull = eventService.updateEventForAdmin(eventId, updateEventAdminRequestDto);
        log.info("Admin edits event id ={}.", eventId);
        return eventFull;
    }
}