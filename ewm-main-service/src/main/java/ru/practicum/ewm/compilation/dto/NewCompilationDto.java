package ru.practicum.ewm.compilation.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Set;

@Getter
@Setter
@Builder
public class NewCompilationDto {
    private Set<Long> events;
    private Boolean pinned;
    @NotBlank
    @Size(max = 50)
    private String title;
}
