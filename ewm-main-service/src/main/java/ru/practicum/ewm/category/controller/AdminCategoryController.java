package ru.practicum.ewm.category.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.service.CategoryService;

import javax.validation.Valid;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {
    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createCategory(@RequestBody @Valid CategoryDto newCategoryDto) {
        log.info("Сохранение категории.");
        return categoryService.createCategory(newCategoryDto);
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long id) {
        log.info("Удаление категории.");
        categoryService.deleteCategory(id);
    }

    @PatchMapping("{id}")
    public CategoryDto updateCategory(@PathVariable Long id,
                                      @RequestBody @Valid CategoryDto categoryDto) {
        log.info("Обновление категории.");
        return categoryService.updateCategory(id, categoryDto);
    }
}
