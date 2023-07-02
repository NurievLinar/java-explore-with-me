package ru.practicum.ewm.request.dto;

import lombok.Builder;
import lombok.Getter;
import ru.practicum.ewm.event.model.State;

@Getter
@Builder
public class ParticipationRequestDto {
    private Long id;
    private Long event;
    private String created;
    private Long requester;
    private State status;
}
