package ru.practicum.ewm.compilation.service;

import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {

    CompilationDto createCompilation(NewCompilationDto dto);

    void deleteCompilation(Long compId);

    CompilationDto updateCompilations(Long compId, UpdateCompilationRequest dto);

    CompilationDto getCompilation(Long compId);

    List<CompilationDto> getCompilationsByFilters(Boolean pinned, Integer from, Integer size);
}
