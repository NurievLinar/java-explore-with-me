package ru.practicum.ewm.request.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.request.dto.RequestDto;
import ru.practicum.ewm.request.service.RequestService;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(path = "/users/{userId}/requests")
public class RequestController {

    private final RequestService requestService;

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public RequestDto saveRequest(@PathVariable Long userId,
                                  @RequestParam Long eventId) {
        log.info("Сохранение запроса.");
        return requestService.saveRequest(userId, eventId);
    }

    @GetMapping
    public List<RequestDto> getRequests(@PathVariable Long userId) {
        log.info("Получение запросов.");
        return requestService.getRequests(userId);
    }

    @PatchMapping("/{requestId}/cancel")
    public RequestDto cancelRequest(@PathVariable Long userId,
                                    @PathVariable Long requestId) {
        log.info("Отмена запроса.");
        return requestService.cancelRequest(userId, requestId);
    }
}
