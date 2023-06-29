package ru.practicum.ewm.event.service;

import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventsShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEvent;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

public interface EventsService {
    EventFullDto createEvent(Long userId, NewEventDto dto);

    List<EventsShortDto> getEventsFromUser(Long userId, Integer from, Integer size);

    EventFullDto getEventWithOwner(Long userId, Long eventId);

    EventFullDto updateEvent(Long userId, Long eventId, UpdateEvent dto);

    List<EventFullDto> getEventsForAdmin(List<Long> users, List<String> states, List<Long> categories,
                                         LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEvent dto);

    List<ParticipationRequestDto> getRequestsForUserForThisEvent(Long userId, Long eventId);

    EventRequestStatusUpdateResult changeRequestsStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest dto);

    List<EventsShortDto> getEventsWithFilters(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                              LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, Integer from,
                                              Integer size, HttpServletRequest request);

    EventFullDto getEventWithFullInfoById(Long id, HttpServletRequest request);
}
