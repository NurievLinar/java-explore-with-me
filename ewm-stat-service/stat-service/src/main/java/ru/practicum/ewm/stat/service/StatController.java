package ru.practicum.ewm.stat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.stat.dto.HitDto;
import ru.practicum.ewm.stat.dto.StatDto;
import ru.practicum.ewm.stat.service.service.StatService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class StatController {

    private final StatService service;

    @PostMapping(path = "/hit")
    @ResponseStatus(value = HttpStatus.CREATED)
    public HitDto createHit(@RequestBody @Valid HitDto hitDto) {
        log.info("Сохранение запроса");
        return service.createHit(hitDto);
    }

    @GetMapping(path = "/stats")
    public List<StatDto> getStat(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                 @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                 @RequestParam(required = false) List<String> uris,
                                 @RequestParam(defaultValue = "false") Boolean unique) {
        log.info("Получение статистики");
        return service.getStat(start, end, uris, unique);
    }
}
