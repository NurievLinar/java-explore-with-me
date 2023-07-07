package ru.practicum.ewm.stat.service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stat.dto.HitDto;
import ru.practicum.ewm.stat.dto.StatDto;
import ru.practicum.ewm.stat.service.exception.StartEndRangeException;
import ru.practicum.ewm.stat.service.mapper.HitMapper;
import ru.practicum.ewm.stat.service.model.Hit;
import ru.practicum.ewm.stat.service.repository.HitRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatServiceImpl implements StatService {

    private final HitRepository hitRepository;

    @Override
    @Transactional
    public HitDto createHit(HitDto hitDto) {
        Hit hit = HitMapper.toHit(hitDto);
        return HitMapper.toHitDto(hitRepository.save(hit));
    }

    @Override
    public List<StatDto> getStat(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        checkDate(start, end);
        List<StatDto> stats;
        if (uris == null || uris.isEmpty()) {
            if (unique) {
                stats = hitRepository.findAllUniqueIpWithoutUris(start, end);
            } else {
                stats = hitRepository.findAllWithoutUris(start, end);
            }
        } else {
            if (unique) {
                stats = hitRepository.findAllUniqueIpWithUris(uris, start, end);
            } else {
                stats = hitRepository.findAllWithUris(uris, start, end);
            }
        }
        return stats;
    }

    private void checkDate(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new StartEndRangeException("Ошибка времени начала и конца диапазона");
        }
        if (startTime.isAfter(endTime)) {
            throw new StartEndRangeException("Ошибка времени начала и конца диапазона");
        }
    }
}
