package ru.practicum.ewm.request.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class EventRequestStatusUpdateRequest {
    private List<Long> requestIds;
    private String status;
}
