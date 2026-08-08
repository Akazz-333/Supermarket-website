package com.supermarket.inventory.service;

import com.supermarket.inventory.dto.req.CategoryRequest;
import com.supermarket.inventory.dto.resp.CategoryResponse;
import com.supermarket.inventory.entity.Category;
import com.supermarket.inventory.exception.CategoryDeleteException;
import com.supermarket.inventory.exception.DuplicateResourceException;
import com.supermarket.inventory.exception.ResourceNotFoundException;
import com.supermarket.inventory.repository.CategoryRepository;
import com.supermarket.inventory.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToCategoryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return mapToCategoryResponse(category);
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new DuplicateResourceException("Category with name '" + request.getName() + "' already exists");
        }

        Category category = new Category(request.getName().trim(), request.getDescription());
        Category saved = categoryRepository.save(category);
        return mapToCategoryResponse(saved);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        if (!category.getName().equalsIgnoreCase(request.getName().trim()) && categoryRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new DuplicateResourceException("Category with name '" + request.getName() + "' already exists");
        }

        category.setName(request.getName().trim());
        category.setDescription(request.getDescription());
        Category updated = categoryRepository.save(category);
        return mapToCategoryResponse(updated);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        // Enforce business rule: Prevent deletion of category associated with existing products
        if (productRepository.existsByCategoryId(id)) {
            throw new CategoryDeleteException("Cannot delete category '" + category.getName() + "' because it is associated with existing products. Reassign or delete associated products first.");
        }

        categoryRepository.delete(category);
    }

    public CategoryResponse mapToCategoryResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}
