package ru.practicum.ewm.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.compilation.mapper.CompilationMapper;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.repository.CompilationRepository;
import ru.practicum.ewm.event.dto.EventsShortDto;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventsRepository;
import ru.practicum.ewm.exception.NotFoundException;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static ru.practicum.ewm.compilation.mapper.CompilationMapper.toCompilationDto;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventsRepository eventsRepository;

    @Transactional
    public CompilationDto createCompilation(NewCompilationDto dto) {
        if (dto.getPinned() == null) {
            dto.setPinned(false);
        }
        Set<Event> events = new HashSet<>();
        if (dto.getEvents() != null && !dto.getEvents().isEmpty()) {
            events = eventsRepository.findAllByIdIn(dto.getEvents());
        }
        Compilation compilation = CompilationMapper.toCompilation(dto,events);
        return CompilationMapper.toCompilationDto(compilationRepository.save(compilation));
    }

    @Transactional
    public void deleteCompilation(Long compId) {
        compilationRepository.deleteById(compId);
    }

    @Transactional
    public CompilationDto updateCompilations(Long compId, UpdateCompilationRequest dto) {
        Compilation compilation = findCompilationById(compId);
        Set<Event> eventList;
        List<EventsShortDto> eventsShortDtos;
        if (dto.getEvents() != null) {
            eventList = new HashSet<>(eventsRepository.findAllById(dto.getEvents()));
            eventsShortDtos = eventList.stream()
                    .map(EventMapper::toEventShortDto)
                    .collect(Collectors.toList());
            compilation.setEvents(eventList);
        } else {
            eventList = compilation.getEvents();
            eventsShortDtos = eventList.stream()
                    .map(EventMapper::toEventShortDto)
                    .collect(Collectors.toList());
        }
        ofNullable(dto.getPinned()).ifPresent(compilation::setPinned);
        ofNullable(dto.getTitle()).ifPresent(compilation::setTitle);
        Compilation newCompilation = compilationRepository.save(compilation);
        return toCompilationDto(newCompilation, eventsShortDtos);
    }

    public CompilationDto getCompilation(Long compId) {
        return toCompilationDto(findCompilationById(compId));
    }


    public List<CompilationDto> getCompilationsByFilters(Boolean pinned, Integer from, Integer size) {
        PageRequest page = PageRequest.of(from, size);
        List<Compilation> compilations = compilationRepository.findByPinned(pinned, page);
        return compilations.stream()
                .map(CompilationMapper::toCompilationDto)
                .collect(Collectors.toList());
    }

    private Compilation findCompilationById(Long compId) {
        return compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка не найдена"));
    }
}
