package ru.practicum.category.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.service.EventPublicService;
import ru.practicum.exceptions.ConflictException;
import ru.practicum.exceptions.ObjectNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventPublicService eventPublicService;

    public CategoryService(CategoryRepository storage, EventPublicService eventPublicService) {
        this.categoryRepository = storage;
        this.eventPublicService = eventPublicService;
    }

    public Category save(Category category) {
        checkUniqueName(category.getName());
        return categoryRepository.save(category);
    }

    public void delete(Long id) {
        categoryRepository.findById(id).orElseThrow(()
                -> new ObjectNotFoundException("Category with id=" + id + " was not found"));
        eventPublicService.deleteCategoryFromEvent(id);
        categoryRepository.deleteById(id);
    }

    public Category patch(Category category, Long id) {
        Category category1 = categoryRepository.findById(id).orElseThrow(()
                -> new ObjectNotFoundException("Category with id=" + id + " was not found"));
        if (!category1.getName().equals(category.getName())) {
            checkUniqueName(category.getName());
        }
        category1.setName(category.getName());
        return categoryRepository.save(category1);
    }

    public List<CategoryDto> getAll(int from, int size) {
        Pageable pageable = PageRequest.of(from, size);
        Page<Category> categories = categoryRepository.findAll(pageable);
        return categories.stream()
                .map(CategoryMapper::toDto)
                .collect(Collectors.toList());
    }

    public CategoryDto getById(Long id) {
        Category category = findById(id);
        return CategoryMapper.toDto(category);
    }

    public Category findById(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new ObjectNotFoundException("Category with id=" + id + " was not found"));
    }

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    private void checkUniqueName(String name) {
        Category category = categoryRepository.findByName(name);
        if (category != null) {
            throw new ConflictException("This name has already exist");
        }
    }
}
