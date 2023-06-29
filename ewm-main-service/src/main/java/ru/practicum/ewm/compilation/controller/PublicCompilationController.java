package ru.practicum.ewm.compilation.controller;

import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.service.CompilationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/compilations")
@RequiredArgsConstructor
public class PublicCompilationController {
    private final CompilationService service;

    @GetMapping("/{compId}")
    public CompilationDto getCompilation(@PathVariable Long compId) {
        log.info("Получение подборки.");
        return service.getCompilation(compId);
    }

    @GetMapping
    public List<CompilationDto> getCompilationsByFilters(@RequestParam(required = false) Boolean pinned,
                                                         @RequestParam(required = false, defaultValue = "0") Integer from,
                                                         @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("Получение списка подборок.");
        return service.getCompilationsByFilters(pinned, from, size);
    }
}
