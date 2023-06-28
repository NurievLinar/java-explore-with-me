package ru.practicum.ewm.request.service;

import ru.practicum.ewm.event.dto.EventReqStatusUpdateReqDto;
import ru.practicum.ewm.event.dto.RequestStatusUpdateResponse;
import ru.practicum.ewm.request.dto.RequestDto;

import java.util.List;

public interface RequestService {

    RequestDto saveRequest(Long userId, Long eventId);

    List<RequestDto> getRequests(Long userId);

    List<RequestDto> getRequests(Long eventId, Long userId);

    RequestStatusUpdateResponse updateRequest(Long eventId, Long userId, EventReqStatusUpdateReqDto updateReqDto);

    RequestDto cancelRequest(Long userId, Long requestId);
}
