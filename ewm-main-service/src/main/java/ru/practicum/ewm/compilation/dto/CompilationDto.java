package ru.practicum.ewm.compilation.dto;

import lombok.Builder;
import lombok.Getter;
import ru.practicum.ewm.event.dto.EventsShortDto;

import java.util.List;

@Getter
@Builder
public class CompilationDto {
    private Long id;
    private List<EventsShortDto> events;
    private Boolean pinned;
    private String title;
}