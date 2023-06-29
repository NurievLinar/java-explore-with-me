package ru.practicum.ewm.event.controller;

import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventsShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEvent;
import ru.practicum.ewm.event.service.EventsService;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/users/{userId}/events")
@RequiredArgsConstructor
public class PrivateEventsController {
    private final EventsService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@PathVariable Long userId,
                                    @RequestBody @Valid NewEventDto dto) {
        log.info("Create Event from userId {}, dto {}", userId, dto);
        return service.createEvent(userId, dto);
    }

    @GetMapping
    public List<EventsShortDto> getEventsFromUser(@PathVariable Long userId,
                                                  @RequestParam(required = false, defaultValue = "0") Integer from,
                                                  @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("Get Events from userId {}, from {}, size{}", userId, from, size);
        return service.getEventsFromUser(userId, from, size);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventWithOwner(@PathVariable Long userId,
                                          @PathVariable Long eventId) {
        log.info("Get Event with eventId {} from userId {}", eventId, userId);
        return service.getEventWithOwner(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@PathVariable Long userId,
                                    @PathVariable Long eventId,
                                    @RequestBody @Valid UpdateEvent dto) {
        log.info("Update Event with eventId {} from userId {}, dto {}", eventId, userId, dto);
        return service.updateEvent(userId, eventId, dto);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getRequestsForUserForThisEvent(@PathVariable Long userId,
                                                                        @PathVariable Long eventId) {
        log.info("Get Requests for userId {}, for eventId {}", userId, eventId);
        return service.getRequestsForUserForThisEvent(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult changeRequestsStatus(@PathVariable Long userId,
                                                               @PathVariable Long eventId,
                                                               @RequestBody EventRequestStatusUpdateRequest dto) {
        log.info("Change Requests status with userId {}, eventId {}, dto {} ", userId, eventId, dto);
        return service.changeRequestsStatus(userId, eventId, dto);
    }

}
