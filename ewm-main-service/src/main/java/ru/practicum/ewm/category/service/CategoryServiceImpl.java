package ru.practicum.ewm.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;

import java.util.List;

import static ru.practicum.ewm.category.mapper.CategoryMapper.*;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    public CategoryDto saveCategory(NewCategoryDto newCategoryDto) {
        Category category = toCategory(newCategoryDto);
        try {
            categoryRepository.save(category);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Категория уже существует.");
        }
        return toCategoryDto(category);
    }

    @Override
    public CategoryDto updateCategory(Long categoryId, NewCategoryDto newCategoryDto) {
        Category categoryToUpdate = getCategory(categoryId);
        Category category = toCategory(newCategoryDto);

        categoryToUpdate.setName(category.getName());

        try {
            categoryRepository.save(categoryToUpdate);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Имя категории уже существует.");
        }
        return toCategoryDto(categoryToUpdate);
    }

    @Override
    public CategoryDto getCategoryById(Long categoryId) {
        return toCategoryDto(getCategory(categoryId));
    }

    @Override
    public List<CategoryDto> getAllCategories(Integer from, Integer size) {
        return toCategoriesDto(categoryRepository.findAll(PageRequest.of(from / size, size)));
    }

    @Override
    public void deleteCategory(Long categoryId) {
        if (!eventRepository.findAllByCategoryId(categoryId).isEmpty()) {
            throw new ConflictException("Невозможно удалить категорию, когда в ней есть события.");
        }
        categoryRepository.deleteById(categoryId);
    }

    private Category getCategory(Long categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(() ->
                new NotFoundException("Неверный ID категории."));
    }
}