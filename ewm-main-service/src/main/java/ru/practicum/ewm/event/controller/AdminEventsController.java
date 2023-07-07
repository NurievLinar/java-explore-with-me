package ru.practicum.ewm.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.UtilityClass;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.UpdateEvent;
import ru.practicum.ewm.event.service.EventsService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/admin/events")
@RequiredArgsConstructor
public class AdminEventsController {
    private final EventsService service;

    @GetMapping
    public List<EventFullDto> getEventsForAdmin(@RequestParam(required = false) List<Long> users,
                                                @RequestParam(required = false) List<String> states,
                                                @RequestParam(required = false) List<Long> categories,
                                                @RequestParam(required = false) @DateTimeFormat(pattern = UtilityClass.pattern)
                                                LocalDateTime rangeStart,
                                                @RequestParam(required = false) @DateTimeFormat(pattern = UtilityClass.pattern)
                                                LocalDateTime rangeEnd,
                                                @RequestParam(required = false, defaultValue = "0") Integer from,
                                                @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("Получение события администратором.");
        return service.getEventsForAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventByAdmin(@PathVariable Long eventId,
                                           @RequestBody @Valid UpdateEvent dto) {
        log.info("Событие изменено администратором.");
        return service.updateEventByAdmin(eventId, dto);
    }
}