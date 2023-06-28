package ru.practicum.ewm.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.request.dto.RequestDto;
import ru.practicum.ewm.request.service.RequestService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/events")
public class EventUserController {

    private final EventService eventService;
    private final RequestService requestService;

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public EventFullDto saveEvent(@PathVariable Long userId,
                                  @Valid @RequestBody NewEventDto newEventDto) {
        log.info("Сохранение события.");
        return eventService.saveEvent(userId, newEventDto);
    }

    @GetMapping
    public List<EventShortDto> getUserEvents(@PathVariable Long userId,
                                             @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                             @Positive @RequestParam(defaultValue = "10") int size) {
        log.info("Получение событий.");
        return eventService.getUserEvents(userId, from, size);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventByUserId(@PathVariable Long eventId,
                                         @PathVariable Long userId) {
        log.info("Получение события.");
        return eventService.getEventByUserId(eventId, userId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventByUserId(@PathVariable Long eventId,
                                            @PathVariable Long userId,
                                            @RequestBody PrivateUpdateEventDto eventDto) {
        log.info("Изменение события.");
        return eventService.updateEventByUserId(eventId, userId, eventDto);
    }

    @GetMapping("/{eventId}/requests")
    public List<RequestDto> getRequests(@PathVariable Long userId,
                                        @PathVariable Long eventId) {
        log.info("Получение запроса на участие в событии.");
        return requestService.getRequests(eventId, userId);
    }

    @PatchMapping("/{eventId}/requests")
    public RequestStatusUpdateResponse updateRequest(@PathVariable Long userId,
                                                     @PathVariable Long eventId,
                                                     @RequestBody EventReqStatusUpdateReqDto updateReqDto) {
        log.info("Изменение статуса заявки.");
        return requestService.updateRequest(userId, eventId, updateReqDto);
    }
}
