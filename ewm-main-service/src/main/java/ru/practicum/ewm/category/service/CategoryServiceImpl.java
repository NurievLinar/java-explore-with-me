package ru.practicum.ewm.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventsRepository;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.IncorrectStateException;
import ru.practicum.ewm.exception.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static ru.practicum.ewm.category.mapper.CategoryMapper.toCategory;
import static ru.practicum.ewm.category.mapper.CategoryMapper.toCategoryDto;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventsRepository eventsRepository;

    @Transactional
    public CategoryDto createCategory(CategoryDto newCategoryDto) {
        if (newCategoryDto != null) {
            Category category = toCategory(newCategoryDto);
            return saveCategory(category);
        }
        return null;
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = getCategoryModel(id);
        List<Event> events = eventsRepository.findByCategory(category);
        if (!events.isEmpty()) {
            throw new ConflictException("Невозможно удалить категорию.");
        }
        categoryRepository.deleteById(id);
    }

    @Transactional
    public CategoryDto updateCategory(Long id, CategoryDto newCategoryDto) {
        Category category = getCategoryModel(id);
        ofNullable(newCategoryDto.getName()).ifPresent(category::setName);
        try {
            return toCategoryDto(categoryRepository.save(category));
        } catch (DataIntegrityViolationException e) {
            throw new IncorrectStateException("Имя должно быть уникальным");
        } catch (Exception e) {
            throw new BadRequestException("Некорректный запрос");
        }

    }

    public List<CategoryDto> getCategories(Integer from, Integer size) {
        PageRequest page = PageRequest.of(from, size);
        return categoryRepository.findAll(page).stream()
                .map(CategoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    public CategoryDto getCategory(Long id) {
        Category category = getCategoryModel(id);
        return toCategoryDto(category);
    }

    private Category getCategoryModel(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Категория не найдена"));
    }

    private CategoryDto saveCategory(Category category) {
        try {
            return toCategoryDto(categoryRepository.save(category));
        } catch (DataIntegrityViolationException e) {
            throw new IncorrectStateException("Имя должно быть уникальным");
        }
    }

}