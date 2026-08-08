package com.supermarket.inventory.service;

import com.supermarket.inventory.dto.req.ProductRequest;
import com.supermarket.inventory.dto.resp.CategoryResponse;
import com.supermarket.inventory.dto.resp.ProductResponse;
import com.supermarket.inventory.entity.Category;
import com.supermarket.inventory.entity.Product;
import com.supermarket.inventory.exception.DuplicateResourceException;
import com.supermarket.inventory.exception.ResourceNotFoundException;
import com.supermarket.inventory.repository.CategoryRepository;
import com.supermarket.inventory.repository.ProductRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository, CategoryService categoryService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.categoryService = categoryService;
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(
            String keyword,
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean lowStock,
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter active products by default
            predicates.add(cb.equal(root.get("active"), true));

            if (keyword != null && !keyword.trim().isEmpty()) {
                String searchPattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("name")), searchPattern);
                Predicate skuMatch = cb.like(cb.lower(root.get("sku")), searchPattern);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), searchPattern);
                predicates.add(cb.or(nameMatch, skuMatch, descMatch));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            if (Boolean.TRUE.equals(lowStock)) {
                predicates.add(cb.lessThanOrEqualTo(root.get("availableStock"), root.get("minStockLevel")));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return productRepository.findAll(spec, pageable).map(this::mapToProductResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return mapToProductResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductBySku(String sku) {
        Product product = productRepository.findBySku(sku.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with SKU: " + sku));
        return mapToProductResponse(product);
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        if (productRepository.existsBySku(request.getSku().trim())) {
            throw new DuplicateResourceException("Product with SKU '" + request.getSku() + "' already exists");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        Product product = new Product(
                request.getSku().trim().toUpperCase(),
                request.getName().trim(),
                request.getDescription(),
                request.getPrice(),
                request.getAvailableStock(),
                request.getMinStockLevel(),
                category,
                request.getSupplierName(),
                request.getSupplierContact()
        );

        Product saved = productRepository.save(product);
        return mapToProductResponse(saved);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        if (!product.getSku().equalsIgnoreCase(request.getSku().trim()) && productRepository.existsBySku(request.getSku().trim())) {
            throw new DuplicateResourceException("Product with SKU '" + request.getSku() + "' already exists");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        product.setSku(request.getSku().trim().toUpperCase());
        product.setName(request.getName().trim());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setAvailableStock(request.getAvailableStock());
        product.setMinStockLevel(request.getMinStockLevel());
        product.setCategory(category);
        product.setSupplierName(request.getSupplierName());
        product.setSupplierContact(request.getSupplierContact());

        Product updated = productRepository.save(product);
        return mapToProductResponse(updated);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        // Soft delete for historical order integrity
        product.setActive(false);
        productRepository.save(product);
    }

    public ProductResponse mapToProductResponse(Product product) {
        CategoryResponse catResp = categoryService.mapToCategoryResponse(product.getCategory());
        boolean isLowStock = product.getAvailableStock() <= product.getMinStockLevel();

        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getAvailableStock(),
                product.getMinStockLevel(),
                isLowStock,
                catResp,
                product.getSupplierName(),
                product.getSupplierContact(),
                product.getActive(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
