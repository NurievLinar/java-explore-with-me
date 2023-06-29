package ru.practicum.ewm.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.user.dto.UserShortDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.ewm.UtilityClass;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class EventsShortDto {
    private Long id;
    private String annotation;
    private CategoryDto category;
    private Long confirmedRequests;
    @JsonFormat
            (shape = JsonFormat.Shape.STRING, pattern = UtilityClass.pattern)
    private LocalDateTime eventDate;
    private UserShortDto initiator;
    private Boolean paid;
    private String title;
    private Long views;
}
