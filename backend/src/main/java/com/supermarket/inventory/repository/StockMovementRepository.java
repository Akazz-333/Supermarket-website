package com.supermarket.inventory.repository;

import com.supermarket.inventory.entity.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    Page<StockMovement> findByProductIdOrderByCreatedAtDesc(Long productId, Pageable pageable);
    List<StockMovement> findTop20ByOrderByCreatedAtDesc();
}
