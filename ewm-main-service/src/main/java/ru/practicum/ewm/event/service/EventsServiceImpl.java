package ru.practicum.ewm.event.service;

import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventsShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEvent;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.Sort;
import ru.practicum.ewm.event.model.State;
import ru.practicum.ewm.event.model.StateAction;
import ru.practicum.ewm.event.repository.EventsRepository;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.IncorrectStateException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.location.model.Location;
import ru.practicum.ewm.location.repository.LocationRepository;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.mapper.RequestMapper;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stat.client.StatClient;
import ru.practicum.ewm.stat.dto.HitDto;
import ru.practicum.ewm.stat.dto.StatDto;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static ru.practicum.ewm.event.mapper.EventMapper.toEvent;
import static ru.practicum.ewm.event.mapper.EventMapper.toEventFullDto;
import static ru.practicum.ewm.request.mapper.RequestMapper.toRequestDto;
import static ru.practicum.ewm.UtilityClass.formatter;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@ComponentScan(basePackages = {"ru.practicum.client"})
public class EventsServiceImpl implements EventsService {
    private final EventsRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final RequestRepository requestRepository;
    private final StatClient statClient;

    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto dto) {
        if (dto.getPaid() == null) {
            dto.setPaid(false);
        }
        if (dto.getParticipantLimit() == null) {
            dto.setParticipantLimit(0L);
        }
        if (dto.getRequestModeration() == null) {
            dto.setRequestModeration(true);
        }
        LocalDateTime nowDateTime = LocalDateTime.now();
        checkDateTimeForDto(nowDateTime, dto.getEventDate());
        Category category = categoryRepository.findById(dto.getCategory())
                .orElseThrow(() -> new NotFoundException("Категория с таким id  не найдена"));
        User user = getUserModel(userId);
        locationRepository.save(dto.getLocation());
        Event event = toEvent(dto, category, user, nowDateTime);
        return toEventFullDto(eventRepository.save(event), 0L);
    }

    public List<EventsShortDto> getEventsFromUser(Long userId, Integer from, Integer size) {
        PageRequest page = PageRequest.of(from, size);
        User user = getUserModel(userId);
        List<Event> events = eventRepository.findByInitiator(user, page);
        Map<Long, Long> hits = getStatisticFromListEvents(events);
        return events.stream()
                .map(EventMapper::toEventShortDto)
                .peek(e -> e.setViews(hits.getOrDefault(e.getId(), 0L)))
                .collect(Collectors.toList());
    }

    public EventFullDto getEventWithOwner(Long userId, Long eventId) {
        checkUser(userId);
        Event event = findEventById(eventId);
        EventFullDto eventFullDto = EventMapper.toEventFullDto(event, 0L);
        Map<Long, Long> hits = getStatisticFromListEvents(List.of(event));
        eventFullDto.setViews(hits.getOrDefault(event.getId(), 0L));
        return eventFullDto;
    }

    @Transactional
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEvent dto) {
        Event event = findEventById(eventId);
        checkUser(userId);
        if (dto.getEventDate() != null) {
            checkDateTimeForDto(LocalDateTime.now(), dto.getEventDate());
        }
        if (!(event.getState().equals(State.CANCELED) || event.getState().equals(State.PENDING))) {
            throw new IncorrectStateException("Некорректный статус.");
        }
        if (dto.getStateAction() != null) {
            switch (dto.getStateAction()) {
                case SEND_TO_REVIEW:
                    event.setState(State.PENDING);
                    break;
                case CANCEL_REVIEW:
                    event.setState(State.CANCELED);
                    break;
                default:
                    throw new IncorrectStateException("Некорректный статус dto.");
            }
        }
        return getEventFullDto(dto, event);
    }

    public List<EventFullDto> getEventsForAdmin(List<Long> users, List<String> states, List<Long> categories,
                                                LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size) {
        PageRequest page = PageRequest.of(from, size);
        List<State> stateList = null;
        LocalDateTime start = null;
        LocalDateTime end = null;
        if (states != null) {
            stateList = states.stream()
                    .map(State::valueOf)
                    .collect(Collectors.toList());
        }
        if (rangeStart != null) {
            start = rangeStart;
        }
        if (rangeEnd != null) {
            end = rangeEnd;
        }
        List<Event> events = eventRepository.getEventsWithUsersStatesCategoriesDateTime(
                users, stateList, categories, start, end, page);
        Map<Long, Long> hits = getStatisticFromListEvents(events);
        return events.stream()
                .map(e -> EventMapper.toEventFullDto(e, hits.getOrDefault(e.getId(), 0L)))
                .collect(Collectors.toList());
    }

    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEvent dto) {
        Event event = findEventById(eventId);
        if (dto.getEventDate() != null) {
            if (LocalDateTime.now().plusHours(1).isAfter(dto.getEventDate())) {
                throw new BadRequestException("Ошибка. Дата и время на которые намечено событие " +
                        "не может быть раньше, чем через час от текущего момента");
            }
        } else {
            if (dto.getStateAction() != null) {
                if (dto.getStateAction().equals(StateAction.PUBLISH_EVENT) &&
                        LocalDateTime.now().plusHours(1).isAfter(event.getEventDate())) {
                    throw new IncorrectStateException("Ошибка. Дата и время публикуемого события " +
                            "не может быть раньше, чем через час от текущего момента");
                }
                if (dto.getStateAction().equals(StateAction.PUBLISH_EVENT) && !(event.getState().equals(State.PENDING))) {
                    throw new IncorrectStateException("Некорректный статус. Событие можно публиковать, " +
                            "только если оно в состоянии ожидания публикации.");
                }
                if (dto.getStateAction().equals(StateAction.REJECT_EVENT) && event.getState().equals(State.PUBLISHED)) {
                    throw new IncorrectStateException("Некорректный статус. Событие можно отклонить, " +
                            "только если оно еще не опубликовано.");
                }
            }
        }
        if (dto.getStateAction() != null) {
            switch (dto.getStateAction()) {
                case REJECT_EVENT:
                    event.setState(State.CANCELED);
                    break;
                case PUBLISH_EVENT:
                    event.setState(State.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
                default:
                    throw new IncorrectStateException("Некорректный статус dto.");
            }
        }
        return getEventFullDto(dto, event);
    }

    public List<EventsShortDto> getEventsWithFilters(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                                     LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, Integer from,
                                                     Integer size, HttpServletRequest request) {
        PageRequest page = PageRequest.of(from, size);
        List<Event> events = new ArrayList<>();
        checkDateTime(rangeStart, rangeEnd);
        if (onlyAvailable) {
            if (sort == null) {
                events = eventRepository.getAvailableEventsWithFiltersDateSorted(
                        text, State.PUBLISHED, categories, paid, rangeStart, rangeEnd, page);
            } else {
                switch (Sort.valueOf(sort)) {
                    case EVENT_DATE:
                        events = eventRepository.getAvailableEventsWithFiltersDateSorted(
                                text, State.PUBLISHED, categories, paid, rangeStart, rangeEnd, page);
                        addStatistic(request);
                        Map<Long, Long> hits = getStatisticFromListEvents(events);
                        return events.stream()
                                .map(EventMapper::toEventShortDto)
                                .peek(e -> e.setViews(hits.get(e.getId())))
                                .collect(Collectors.toList());
                    case VIEWS:
                        events = eventRepository.getAvailableEventsWithFilters(
                                text, State.PUBLISHED, categories, paid, rangeStart, rangeEnd, page);
                        addStatistic(request);
                        Map<Long, Long> hits3 = getStatisticFromListEvents(events);
                        return events.stream()
                                .map(EventMapper::toEventShortDto)
                                .sorted(Comparator.comparing(EventsShortDto::getViews))
                                .peek(e -> e.setViews(hits3.get(e.getId())))
                                .collect(Collectors.toList());
                }
            }
        } else {
            if (sort == null) {
                events = eventRepository.getAllEventsWithFiltersDateSorted(
                        text, State.PUBLISHED, categories, paid, rangeStart, rangeEnd, page);
            } else {
                switch (Sort.valueOf(sort)) {
                    case EVENT_DATE:
                        events = eventRepository.getAllEventsWithFiltersDateSorted(
                                text, State.PUBLISHED, categories, paid, rangeStart, rangeEnd, page);
                        addStatistic(request);
                        return events.stream()
                                .map(EventMapper::toEventShortDto)
                                .collect(Collectors.toList());
                    case VIEWS:
                        events = eventRepository.getAllEventsWithFilters(
                                text, State.PUBLISHED, categories, paid, rangeStart, rangeEnd, page);
                        addStatistic(request);
                        Map<Long, Long> hits = getStatisticFromListEvents(events);
                        return events.stream()
                                .map(EventMapper::toEventShortDto)
                                .sorted(Comparator.comparing(EventsShortDto::getViews))
                                .peek(e -> e.setViews(hits.get(e.getId())))
                                .collect(Collectors.toList());
                }
            }
        }
        addStatistic(request);
        Map<Long, Long> hits = getStatisticFromListEvents(events);
        return events.stream()
                .map(EventMapper::toEventShortDto)
                .peek(e -> e.setViews(hits.get(e.getId())))
                .collect(Collectors.toList());
    }

    public EventFullDto getEventWithFullInfoById(Long id, HttpServletRequest request) {
        Event event = findEventById(id);
        if (!event.getState().equals(State.PUBLISHED)) {
            throw new NotFoundException("Событие еще не опубликовано");
        }
        addStatistic(request);
        EventFullDto eventFullDto = EventMapper.toEventFullDto(event, 0L);
        Map<Long, Long> hits = getStatisticFromListEvents(List.of(event));
        eventFullDto.setViews(hits.get(event.getId()));
        return eventFullDto;
    }

    public List<ParticipationRequestDto> getRequestsForUserForThisEvent(Long userId, Long eventId) {
        checkUser(userId);
        checkEvent(eventId);
        List<Request> requests = requestRepository.findByEventId(eventId);
        return requests.stream()
                .map(RequestMapper::toRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public EventRequestStatusUpdateResult changeRequestsStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest dto) {
        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();
        checkUser(userId);
        Event event = findEventById(eventId);
        if (!event.getRequestModeration() || event.getParticipantLimit().equals(0L)) {
            throw new ConflictException("Подтверждение заявок не требуется");
        }
        long limitBalance = event.getParticipantLimit() - event.getConfirmedRequests();
        if (event.getParticipantLimit() != 0 && limitBalance <= 0) {
            throw new ConflictException("У события достигнут лимит запросов.");
        }
        if (dto.getStatus().equals(State.REJECTED.toString())) {
            for (Long requestId : dto.getRequestIds()) {
                Request request = requestRepository.findById(requestId)
                        .orElseThrow(() -> new NotFoundException("Запрос не найден."));
                if (request.getStatus().equals(State.PENDING)) {
                    request.setStatus(State.REJECTED);
                    rejectedRequests.add(toRequestDto(request));
                }
            }
        }
        for (int i = 0; i < dto.getRequestIds().size(); i++) {
            Request request = requestRepository.findById(dto.getRequestIds().get(i))
                    .orElseThrow(() -> new NotFoundException("Запрос не найден."));
            if (limitBalance != 0) {
                if (request.getStatus().equals(State.PENDING)) {
                    request.setStatus(State.CONFIRMED);
                    event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                    eventRepository.save(event);
                    confirmedRequests.add(toRequestDto(request));
                    limitBalance--;
                }
            } else {
                if (request.getStatus().equals(State.PENDING)) {
                    request.setStatus(State.REJECTED);
                    rejectedRequests.add(toRequestDto(request));
                }
            }
        }
        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmedRequests)
                .rejectedRequests(rejectedRequests)
                .build();
    }

    private void addStatistic(HttpServletRequest request) {
        String app = "ewm-main-service";
        statClient.createHit(HitDto.builder()
                .app(app)
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build());
    }

    private EventFullDto getEventFullDto(UpdateEvent dto, Event event) {
        Event updatedEvent = updateEventFields(event, dto);
        Event updatedEventFromDB = eventRepository.save(updatedEvent);
        EventFullDto eventFullDto = EventMapper.toEventFullDto(event, 0L);
        Map<Long, Long> hits = getStatisticFromListEvents(List.of(updatedEventFromDB));
        eventFullDto.setViews(hits.getOrDefault(event.getId(), 0L));
        return eventFullDto;
    }

    private Map<Long, Long> getStatisticFromListEvents(List<Event> events) {
        List<Long> idEvents = events.stream()
                .map(Event::getId)
                .collect(Collectors.toList());
        String start = LocalDateTime.now().minusYears(100).format(formatter);
        String end = LocalDateTime.now().format(formatter);
        String eventsUri = "/events/";
        List<String> uris = idEvents.stream().map(id -> eventsUri + id).collect(Collectors.toList());
        List<StatDto> viewStatDto = statClient.getStat(start, end, uris, true);
        Map<Long, Long> hits = new HashMap<>();
        for (StatDto statsDto : viewStatDto) {
            String uri = statsDto.getUri();
            hits.put(Long.parseLong(uri.substring(eventsUri.length())), statsDto.getHits());
        }
        return hits;
    }

    private void checkDateTime(LocalDateTime start, LocalDateTime end) {
        if (start == null) {
            start = LocalDateTime.now().minusYears(100);
        }
        if (end == null) {
            end = LocalDateTime.now();
        }
        if (start.isAfter(end)) {
            throw new BadRequestException("Некорректный запрос.");
        }
    }

    private Event updateEventFields(Event event, UpdateEvent dto) {
        ofNullable(dto.getAnnotation()).ifPresent(event::setAnnotation);
        ofNullable(dto.getCategory()).ifPresent(category -> event.setCategory(categoryRepository.findById(category)
                .orElseThrow(() -> new NotFoundException("Категория не найдена"))));
        ofNullable(dto.getDescription()).ifPresent(event::setDescription);
        ofNullable(dto.getEventDate()).ifPresent(
                event::setEventDate);
        if (dto.getLocation() != null) {
            List<Location> location = locationRepository.findByLatAndLon(dto.getLocation().getLat(), dto.getLocation().getLon());
            if (location.isEmpty()) {
                locationRepository.save(dto.getLocation());
            }
            event.setLocation(dto.getLocation());
        }
        ofNullable(dto.getPaid()).ifPresent(event::setPaid);
        ofNullable(dto.getParticipantLimit()).ifPresent(event::setParticipantLimit);
        ofNullable(dto.getRequestModeration()).ifPresent(event::setRequestModeration);
        ofNullable(dto.getTitle()).ifPresent(event::setTitle);
        return event;
    }

    private void checkDateTimeForDto(LocalDateTime nowDateTime, LocalDateTime dtoDateTime) {
        if (nowDateTime.plusHours(2).isAfter(dtoDateTime)) {
            throw new BadRequestException("Ошибка даты и времени");
        }
    }

    private void checkUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }
    }

    private User getUserModel(Long idUser) {
        return userRepository.findById(idUser)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    private Event findEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));
    }

    private void checkEvent(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Событие не найдено");
        }
    }
}
