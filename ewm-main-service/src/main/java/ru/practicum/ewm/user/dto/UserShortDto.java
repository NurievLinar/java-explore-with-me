package ru.practicum.ewm.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserShortDto {
    private Long id;
    private String name;
}
