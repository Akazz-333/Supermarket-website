package com.supermarket.inventory.repository;

import com.supermarket.inventory.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByNameIgnoreCase(String name);
    Boolean existsByNameIgnoreCase(String name);
}
