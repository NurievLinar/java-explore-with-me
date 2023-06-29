package ru.practicum.ewm.request.dto;

import ru.practicum.ewm.event.model.State;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ParticipationRequestDto {
    private Long id;
    private Long event;
    private String created;
    private Long requester;
    private State status;
}
