package ru.practicum.ewm.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventsShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEvent;
import ru.practicum.ewm.event.service.EventsService;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;

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
        log.info("Сохранение события.");
        return service.createEvent(userId, dto);
    }

    @GetMapping
    public List<EventsShortDto> getEventsFromUser(@PathVariable Long userId,
                                                  @RequestParam(required = false, defaultValue = "0") Integer from,
                                                  @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("Получение событий.");
        return service.getEventsFromUser(userId, from, size);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventWithOwner(@PathVariable Long userId,
                                          @PathVariable Long eventId) {
        log.info("Получение события.");
        return service.getEventWithOwner(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@PathVariable Long userId,
                                    @PathVariable Long eventId,
                                    @RequestBody @Valid UpdateEvent dto) {
        log.info("Изменение события.");
        return service.updateEvent(userId, eventId, dto);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getRequestsForUserForThisEvent(@PathVariable Long userId,
                                                                        @PathVariable Long eventId) {
        log.info("Получение запроса на участие в событии.");
        return service.getRequestsForUserForThisEvent(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult changeRequestsStatus(@PathVariable Long userId,
                                                               @PathVariable Long eventId,
                                                               @RequestBody EventRequestStatusUpdateRequest dto) {
        log.info("Изменение статуса заявки.");
        return service.changeRequestsStatus(userId, eventId, dto);
    }

}
