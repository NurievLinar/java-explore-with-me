package ru.practicum.ewm.stat.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.stat.dto.StatDto;
import ru.practicum.ewm.stat.service.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

public interface HitRepository extends JpaRepository<Hit, Long> {

    @Query(" SELECT new ru.practicum.ewm.stat.dto.StatDto(h.app, h.uri, COUNT(DISTINCT h.ip))  " +
            "FROM Hit h " +
            "WHERE h.timestamp BETWEEN :start AND :end " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT (DISTINCT h.ip) DESC")
    List<StatDto> findAllUniqueIpWithoutUris(@Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);

    @Query(" SELECT new ru.practicum.ewm.stat.dto.StatDto(h.app, h.uri, COUNT(DISTINCT h.ip))  " +
            "FROM Hit h " +
            "WHERE h.uri IN :uris AND h.timestamp BETWEEN :start AND :end " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT (DISTINCT h.ip) DESC")
    List<StatDto> findAllUniqueIpWithUris(@Param("uris") List<String> uris,
                                          @Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end);


    @Query(" SELECT new ru.practicum.ewm.stat.dto.StatDto(h.app, h.uri, COUNT(h.ip))  " +
            "FROM Hit h " +
            "WHERE h.timestamp BETWEEN :start AND :end " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT (h.ip) DESC")
    List<StatDto> findAllWithoutUris(@Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end);

    @Query(" SELECT new ru.practicum.ewm.stat.dto.StatDto(h.app, h.uri, COUNT(h.uri))  " +
            "FROM Hit h " +
            "WHERE h.uri IN :uris AND h.timestamp BETWEEN :start AND :end " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT (h.uri) DESC")
    List<StatDto> findAllWithUris(@Param("uris") List<String> uris,
                                  @Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end);
}
