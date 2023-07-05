package ru.practicum.ewm.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.State;
import ru.practicum.ewm.event.repository.EventsRepository;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.mapper.RequestMapper;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.ewm.request.mapper.RequestMapper.toRequestDto;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final EventsRepository eventRepository;
    private final UserRepository userRepository;

    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        if (userId == null || eventId == null) {
            throw new BadRequestException("Некорректный запрос");
        }
        User user = checkUser(userId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено."));
        if (userId.equals(event.getInitiator().getId())) {
            throw new ConflictException("Инициатор не может добавить запрос.");
        }
        if (State.PENDING.equals(event.getState()) || State.CANCELED.equals(event.getState())) {
            throw new ConflictException("Невозможно участвовать в неопубликованном событии.");
        }
        if (event.getParticipantLimit() != 0 && event.getParticipantLimit() <= event.getConfirmedRequests())
            throw new ConflictException("У события достигнут лимит запросов на участие.");
        Request request;
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            request = Request.builder()
                    .eventId(eventId)
                    .created(LocalDateTime.now())
                    .requester(user)
                    .status(State.CONFIRMED)
                    .build();
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
        } else {
            request = Request.builder()
                    .eventId(eventId)
                    .created(LocalDateTime.now())
                    .requester(user)
                    .status(State.PENDING)
                    .build();
        }
        eventRepository.save(event);
        return toRequestDto(requestRepository.save(request));
    }

    public List<ParticipationRequestDto> getRequestsForUser(Long userId) {
        User user = checkUser(userId);
        List<Request> requests = requestRepository.findByRequester(user);
        return requests.stream()
                .map(RequestMapper::toRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        checkUser(userId);
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден."));
        request.setStatus(State.CANCELED);
        return toRequestDto(requestRepository.save(request));
    }

    private User checkUser(Long idUser) {
        return userRepository.findById(idUser)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }
}
