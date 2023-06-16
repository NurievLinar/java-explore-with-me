package ru.practicum.ewm.stat.service.service;

import ru.practicum.ewm.stat.dto.HitDto;
import ru.practicum.ewm.stat.dto.StatDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatService {

    HitDto createHit(HitDto hitDto);

    List<StatDto> getStat(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);
}
