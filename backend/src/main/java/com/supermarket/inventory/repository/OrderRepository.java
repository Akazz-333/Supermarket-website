package com.supermarket.inventory.repository;

import com.supermarket.inventory.entity.Order;
import com.supermarket.inventory.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    Optional<Order> findByOrderNumber(String orderNumber);
    Boolean existsByOrderNumber(String orderNumber);
    long countByStatus(OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status IN (com.supermarket.inventory.entity.OrderStatus.CONFIRMED, com.supermarket.inventory.entity.OrderStatus.SHIPPED, com.supermarket.inventory.entity.OrderStatus.DELIVERED)")
    BigDecimal calculateTotalRevenue();
}
