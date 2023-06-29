package ru.practicum.ewm.request.mapper;

import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.model.Request;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.UtilityClass;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestMapper {

    public static ParticipationRequestDto toRequestDto(Request request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .event(request.getEventId())
                .created(request.getCreated().format(UtilityClass.formatter))
                .requester(request.getRequester().getId())
                .status(request.getStatus())
                .build();
    }
}
