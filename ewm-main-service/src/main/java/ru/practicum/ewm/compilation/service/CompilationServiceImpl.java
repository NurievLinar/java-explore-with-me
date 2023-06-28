package ru.practicum.ewm.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.compilation.dto.CompilationCreateDto;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.CompilationUpdateDto;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.repository.CompilationRepository;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.NotFoundException;

import java.util.ArrayList;
import java.util.List;

import static ru.practicum.ewm.compilation.mapper.CompilationMapper.*;

@Service
@RequiredArgsConstructor
@Transactional
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Override
    public CompilationDto saveCompilation(CompilationCreateDto compilationCreateDto) {
        Compilation compilation = toCompilation(compilationCreateDto);
        List<Event> events = eventRepository.findAllById(compilationCreateDto.getEvents());
        compilation.setEvents(events);
        return toCompilationDto(compilationRepository.save(compilation));
    }

    @Override
    public CompilationDto updateCompilation(Long compId, CompilationUpdateDto compilationUpdateDto) {
        Compilation compilationToUpdate = getCompilation(compId);

        if (compilationToUpdate.getEvents() != null) {
            List<Event> events = new ArrayList<>();
            if (compilationUpdateDto.getEvents().size() != 0) {
                events = eventRepository.findEventsByIds(compilationUpdateDto.getEvents());
            }
            compilationToUpdate.setEvents(events);
        }
        compilationToUpdate.setPinned(compilationUpdateDto.getPinned());
        compilationToUpdate.setTitle(compilationUpdateDto.getTitle());
        return toCompilationDto(compilationRepository.save(compilationToUpdate));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getAllCompilations(Boolean pinned, Integer from, Integer size) {
        if (pinned == null) {
            return toCompilationsDto(compilationRepository.findAll(PageRequest.of(from / size, size)));
        }
        return toCompilationsDto(compilationRepository.findAllByPinned(true, PageRequest.of(from / size, size)));
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getCompilationById(Long compId) {
        return toCompilationDto(getCompilation(compId));
    }

    @Override
    public void deleteCompilation(Long compId) {
        compilationRepository.deleteById(compId);
    }

    private Compilation getCompilation(Long compId) {
        return compilationRepository.findById(compId).orElseThrow(() ->
                new NotFoundException("Неверный ID подборки."));
    }
}