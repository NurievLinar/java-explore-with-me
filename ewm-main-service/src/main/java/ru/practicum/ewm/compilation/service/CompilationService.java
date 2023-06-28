package ru.practicum.ewm.compilation.service;

import ru.practicum.ewm.compilation.dto.CompilationCreateDto;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.CompilationUpdateDto;

import java.util.List;

public interface CompilationService {

    CompilationDto saveCompilation(CompilationCreateDto compilationCreateDto);

    CompilationDto updateCompilation(Long compId, CompilationUpdateDto compilationUpdateDto);

    List<CompilationDto> getAllCompilations(Boolean pinned, Integer from, Integer size);

    CompilationDto getCompilationById(Long compId);

    void deleteCompilation(Long compId);
}
