package ru.practicum.ewm.event.service;

import ru.practicum.ewm.event.dto.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface EventService {

    List<EventShortDto> getUserEvents(Long userId, int from, int size);

    EventFullDto saveEvent(Long userId, NewEventDto newEventDto);

    EventFullDto getEventByUserId(Long eventId, Long userId);

    EventFullDto updateEventByUserId(Long eventId, Long userId, PrivateUpdateEventDto eventDto);

    List<EventShortDto> getEvents(String text, List<Long> categories, Boolean paid, String rangeStart, String rangeEnd,
                                  Boolean onlyAvailable, String sort, int from, int size, HttpServletRequest httpServletRequest);

    EventFullDto getEventById(Long eventId, HttpServletRequest httpServletRequest);

    List<EventFullDto> getEventsByAdmin(List<Long> users, List<String> states, List<Long> categories,
                                        String rangeStart, String rangeEnd, int from, int size);

    EventFullDto updateEventByAdmin(Long eventId, AdminUpdateEventDto eventDto);
}
