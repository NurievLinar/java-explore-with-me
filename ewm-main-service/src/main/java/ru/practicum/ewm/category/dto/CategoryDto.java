package ru.practicum.ewm.category.dto;

import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Builder
public class CategoryDto {
    private Long id;
    @NotBlank
    @Size(max = 50)
    private String name;
}