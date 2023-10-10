package ru.practicum.category.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.service.CategoryService;

import javax.validation.Valid;

@RestController
@Slf4j
@RequestMapping("/admin/categories")
public class AdministrationCategoryController {

    private final CategoryService categoryService;

    @Autowired
    public AdministrationCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public CategoryDto addCategory(@Valid @RequestBody CategoryDto categoryDto) {
        CategoryDto category = categoryService.addCategory(categoryDto);
        log.info("Admin added category {}", categoryDto.getName());
        return category;
    }

    @PatchMapping("/{catId}")
    public CategoryDto patchCategory(@RequestBody @Valid CategoryDto categoryDto,
                                     @PathVariable Long catId) {
        CategoryDto category = categoryService.patchCategory(catId, categoryDto);
        log.info("The admin changed the parameter from id = {} to {}.", catId, categoryDto.getName());
        return category;
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{catId}")
    public void deleteCategory(@PathVariable Long catId) {
        categoryService.deleteCategory(catId);
        log.info("Admin deletes category with id = {}.", catId);
    }
}