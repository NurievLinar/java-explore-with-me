package ru.practicum.ewm.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.State;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.location.model.Location;
import ru.practicum.ewm.location.repository.LocationRepository;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.stat.client.StatClient;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.ewm.UtilityClass.DATE_TIME_FORMATTER;
import static ru.practicum.ewm.event.mapper.EventMapper.*;
import static ru.practicum.ewm.event.model.State.*;
import static ru.practicum.ewm.location.mapper.LocationMapper.toLocation;
import static ru.practicum.ewm.request.model.StatusRequest.CONFIRMED;

@Service
@RequiredArgsConstructor
@Transactional
public class EventServiceImpl implements EventService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final LocationRepository locationRepository;
    private final CategoryRepository categoryRepository;
    private final RequestRepository requestRepository;
    private final StatClient statClient;

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        return toEventsDto(eventRepository.findAllByInitiatorId(userId, PageRequest.of(from / size, size)));
    }

    @Override
    public EventFullDto saveEvent(Long userId, NewEventDto newEventDto) {
        User user = getUser(userId);
        Event event = toEvent(newEventDto);

        validateDate(LocalDateTime.parse(newEventDto.getEventDate(), DATE_TIME_FORMATTER));
        Location location = locationRepository.save(toLocation(newEventDto.getLocation()));
        event.setCategory(getCategory(newEventDto.getCategory()));
        event.setLocation(location);
        event.setInitiator(user);

        return toEventDto(eventRepository.save(event));
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventByUserId(Long eventId, Long userId) {
        Event event = getEvent(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new BadRequestException("Невозможно получить полную информацию о событии.");
        }
        return setConfirmedRequests(toEventDto(event));
    }

    @Override
    public EventFullDto updateEventByUserId(Long eventId, Long userId, PrivateUpdateEventDto eventDto) {
        if (eventDto.getEventDate() != null) {
            validateDate(LocalDateTime.parse(eventDto.getEventDate(), DATE_TIME_FORMATTER));
        }
        Event event = getEvent(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Невозможно получить полную информацию о событии.");
        }
        if (event.getState() == (PUBLISHED)) {
            throw new ConflictException("Изменить можно только отмененные события.");
        }
        switch (eventDto.getStateAction()) {
            case SEND_TO_REVIEW:
                event.setState(PENDING);
                break;
            case CANCEL_REVIEW:
                event.setState(CANCELED);
                break;
        }
        return setConfirmedRequests(toEventDto(eventRepository.save(event)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEvents(String text, List<Long> categoryIds, Boolean paid, String rangeStart,
                                         String rangeEnd, Boolean onlyAvailable, String sort, int from, int size,
                                         HttpServletRequest httpServletRequest) {
        //addStat(httpServletRequest);
        List<EventShortDto> events = eventRepository.searchEvents(text, categoryIds, paid, PUBLISHED,
                        PageRequest.of(from / size, size))
                .stream()
                .filter(event -> rangeStart != null ?
                        event.getEventDate().isAfter(LocalDateTime.parse(rangeStart, DATE_TIME_FORMATTER)) :
                        event.getEventDate().isAfter(LocalDateTime.now())
                                && rangeEnd != null ? event.getEventDate().isBefore(LocalDateTime.parse(rangeEnd,
                                DATE_TIME_FORMATTER)) :
                                event.getEventDate().isBefore(LocalDateTime.MAX))
                .map(EventMapper::toEventShortDto)
                .map(this::setConfirmedRequests)
                .collect(Collectors.toList());
        if (Boolean.TRUE.equals(onlyAvailable)) {
            events = events.stream().filter(shortEventDto ->
                    shortEventDto.getConfirmedRequests() < eventRepository
                            .findById(shortEventDto.getId()).get().getParticipantLimit() ||
                            eventRepository.findById(shortEventDto.getId()).get().getParticipantLimit() == 0
            ).collect(Collectors.toList());
        }
        if (sort != null) {
            switch (sort) {
                case "EVENT_DATE":
                    events = events
                            .stream()
                            .sorted(Comparator.comparing(EventShortDto::getEventDate))
                            .collect(Collectors.toList());
                    break;
                case "VIEWS":
                    events = events
                            .stream()
                            .sorted(Comparator.comparing(EventShortDto::getViews))
                            .collect(Collectors.toList());
                    break;
                default:
                    throw new BadRequestException("Сортировкавозможна только по просмотрам или дате события.");
            }
        }
        return events;
    }

    /*private void addStat(HttpServletRequest httpServletRequest) {
        String app = "ewm-main-service";
        statClient.createHit(HitDto.builder()
                .app(app)
                .uri(httpServletRequest.getRequestURI())
                .ip(httpServletRequest.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build());
    }*/

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventById(Long eventId, HttpServletRequest httpServletRequest) {
        //addStat(httpServletRequest);
        Event event = getEvent(eventId);
        if (!event.getState().equals(PUBLISHED)) {
            throw new BadRequestException("Событие должно быть опубликовано.");
        }
        viewCounter(eventId);
        return setConfirmedRequests(toEventDto(event));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getEventsByAdmin(List<Long> users, List<String> states, List<Long> categories,
                                               String rangeStart, String rangeEnd, int from, int size) {
        List<State> stateList = states == null ? null : states
                .stream()
                .map(State::valueOf)
                .collect(Collectors.toList());
        return eventRepository.searchEventsByAdmin(users, stateList, categories, PageRequest.of(from / size, size))
                .stream()
                .filter(event -> rangeStart != null ?
                        event.getEventDate().isAfter(LocalDateTime.parse(rangeStart, DATE_TIME_FORMATTER)) :
                        event.getEventDate().isAfter(LocalDateTime.now())
                                && rangeEnd != null ? event.getEventDate().isBefore(LocalDateTime.parse(rangeEnd,
                                DATE_TIME_FORMATTER)) : event.getEventDate().isBefore(LocalDateTime.MAX))
                .map(EventMapper::toEventDto)
                .map(this::setConfirmedRequests)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto updateEventByAdmin(Long eventId, AdminUpdateEventDto eventDto) {

        if (eventDto.getEventDate() != null) {
            validateDate(LocalDateTime.parse(eventDto.getEventDate(), DATE_TIME_FORMATTER));
        }
        Event event = getEvent(eventId);
        if (eventDto.getTitle() != null) {
            event.setTitle(eventDto.getTitle());
        }
        if (eventDto.getAnnotation() != null) {
            event.setAnnotation(eventDto.getAnnotation());
        }
        if (eventDto.getDescription() != null) {
            event.setDescription(eventDto.getDescription());
        }
        if (eventDto.getCategory() != null) {
            Category category = getCategory(eventDto.getCategory());
            event.setCategory(category);
        }
        if (eventDto.getEventDate() != null) {
            event.setEventDate(LocalDateTime.parse(eventDto.getEventDate(), DATE_TIME_FORMATTER));
        }
        if (eventDto.getLocation() != null) {
            event.setLocation(event.getLocation());
        }
        if (eventDto.getPaid() != null) {
            event.setPaid(eventDto.getPaid());
        }
        if (eventDto.getParticipantLimit() != null) {
            event.setParticipantLimit(eventDto.getParticipantLimit());
        }
        if (eventDto.getRequestModeration() != null) {
            event.setRequestModeration(eventDto.getRequestModeration());
        }

        switch (eventDto.getStateAction()) {
            case PUBLISH_EVENT:
                if (event.getState().equals(PENDING)) {
                    event.setState(PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                } else {
                    throw new ConflictException("Событие можно публиковать, только если оно в состоянии ожидания.");
                }
                break;
            case REJECT_EVENT:
                if (event.getState().equals(PENDING)) {
                    event.setState(CANCELED);
                } else {
                    throw new ConflictException("Событие можно отклонить, только если оно еще не опубликовано.");
                }
                break;
        }
        return toEventDto(eventRepository.save(event));
    }

    private void validateDate(LocalDateTime eventDate) {
        LocalDateTime date = LocalDateTime.now().plusHours(2);
        if (eventDate.isBefore(date)) {
            throw new ConflictException("Дата события должна быть не менее чем через два часа.");
        }
    }

    private EventFullDto setConfirmedRequests(EventFullDto eventDto) {
        eventDto.setConfirmedRequests(requestRepository.countParticipationByEventIdAndStatus(eventDto.getId(),
                CONFIRMED));
        return eventDto;
    }

    private EventShortDto setConfirmedRequests(EventShortDto eventDto) {
        eventDto.setConfirmedRequests(requestRepository.countParticipationByEventIdAndStatus(eventDto.getId(),
                CONFIRMED));
        return eventDto;
    }

    private void viewCounter(Long id) {
        Event event = getEvent(id);
        long views = event.getViews() + 1;
        event.setViews(views);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Неверный ID пользователя."));
    }

    private Event getEvent(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Неверный ID события."));
    }

    private Category getCategory(Long categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(() ->
                new NotFoundException("Неверный ID категории."));
    }
}
