package ru.practicum.ewm.compilation.dto;

import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.Size;
import java.util.Set;

@Getter
@Builder
public class UpdateCompilationRequest {
    private Set<Long> events;
    private Boolean pinned;
    @Size(max = 50)
    private String title;
}
