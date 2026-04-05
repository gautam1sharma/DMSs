package com.serene.dms.repository;

import com.serene.dms.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.math.BigDecimal;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, QuerydslPredicateExecutor<Order> {
    Optional<Order> findByOrderNumber(String orderNumber);
    Page<Order> findByDealerId(Long dealerId, Pageable pageable);
    Page<Order> findByCustomerId(Long customerId, Pageable pageable);
    long countByDealerIdAndStatus(Long dealerId, Order.OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.finalAmount), 0) FROM Order o WHERE o.dealer.id = :dealerId AND o.status = 'COMPLETED'")
    BigDecimal sumCompletedRevenueByDealer(Long dealerId);

    @Query("SELECT COALESCE(SUM(o.finalAmount), 0) FROM Order o WHERE o.status = 'COMPLETED'")
    BigDecimal sumTotalRevenue();
}
