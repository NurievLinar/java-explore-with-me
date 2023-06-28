package ru.practicum.ewm.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.event.dto.EventReqStatusUpdateReqDto;
import ru.practicum.ewm.event.dto.RequestStatusUpdateResponse;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.State;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.request.dto.RequestDto;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.model.StatusRequest;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static ru.practicum.ewm.request.mapper.RequestMapper.toRequestDto;
import static ru.practicum.ewm.request.mapper.RequestMapper.toRequestsDto;
import static ru.practicum.ewm.request.model.StatusRequest.CONFIRMED;

@Service
@RequiredArgsConstructor
@Transactional
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    public RequestDto saveRequest(Long userId, Long eventId) {
        if (requestRepository.findByEventIdAndRequesterId(eventId, userId) != null) {
            throw new ConflictException("Нельзя добавить повторный запрос.");
        }
        User user = getUser(userId);
        Event event = getEvent(eventId);

        if (Objects.equals(userId, event.getInitiator().getId())) {
            throw new ConflictException("Нельзя добавить запрос от инициализатора события.");
        }
        if (!event.getState().equals(State.PUBLISHED)) {
            throw new ConflictException("Нельзя добавить запрос на не опубликованное событие.");
        }
        if (event.getParticipantLimit() <= requestRepository
                .countParticipationByEventIdAndStatus(eventId, CONFIRMED)) {
            throw new ConflictException("Лимит участников уже заполнен.");
        }

        Request request = new Request();
        request.setCreated(LocalDateTime.now());
        request.setRequester(user);
        request.setEvent(event);

        if (Boolean.TRUE.equals(event.getRequestModeration())) {
            request.setStatus(StatusRequest.PENDING);
        } else {
            request.setStatus(CONFIRMED);
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
        }
        return toRequestDto(requestRepository.save(request));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestDto> getRequests(Long userId) {
        return toRequestsDto(requestRepository.findAllByRequesterId(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestDto> getRequests(Long eventId, Long userId) {
        getUser(userId);
        getEvent(eventId);
        return toRequestsDto(requestRepository.findAllByEventIdAndEventInitiatorId(eventId, userId));
    }

    @Override
    public RequestStatusUpdateResponse updateRequest(Long userId, Long eventId, EventReqStatusUpdateReqDto updateReqDto) {
        getUser(userId);
        Event event = getEvent(eventId);

        List<Long> ids = updateReqDto.getRequestIds();
        StatusRequest status = updateReqDto.getStatus();

        List<RequestDto> confirmedList = new ArrayList<>();
        List<RequestDto> rejectedList = new ArrayList<>();

        if (event.getParticipantLimit() == 0) {
            throw new ConflictException("Лимит участников уже заполнен.");
        }
        for (Long id : ids) {
            Request request = requestRepository.findByIdAndEvent_Id(id, eventId).orElseThrow(() ->
                    new NotFoundException("Запрос не найден."));

            if (!request.getStatus().equals(StatusRequest.PENDING)) {
                throw new ConflictException("Нельзя подтвердить запрос.");
            }
            if (status.equals(CONFIRMED)) {
                request.setStatus(CONFIRMED);
                confirmedList.add(toRequestDto(requestRepository.save(request)));
                event.setParticipantLimit(event.getParticipantLimit() - 1);
            } else {
                request.setStatus(StatusRequest.REJECTED);
                rejectedList.add(toRequestDto(requestRepository.save(request)));
            }
        }
        eventRepository.save(event);
        return new RequestStatusUpdateResponse(confirmedList, rejectedList);
    }

    @Override
    public RequestDto cancelRequest(Long userId, Long requestId) {
        Request request = requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> new BadRequestException("Только организатор может отменить запрос."));
        request.setStatus(StatusRequest.CANCELED);
        return toRequestDto(requestRepository.save(request));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Неверный ID пользователя."));
    }

    private Event getEvent(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Неверный ID события."));
    }
}