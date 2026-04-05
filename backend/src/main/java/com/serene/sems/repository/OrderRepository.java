package com.serene.sems.repository;

import com.serene.sems.model.Dealer;
import com.serene.sems.model.Order;
import com.serene.sems.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    long countByDealerId(Long dealerId);

    Optional<Order> findByOrderNumber(String orderNumber);

    Page<Order> findByDealer(Dealer dealer, Pageable pageable);

    Page<Order> findByDealerId(Long dealerId, Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    Page<Order> findByDealerIdAndStatus(Long dealerId, OrderStatus status, Pageable pageable);

    List<Order> findTop5ByOrderByOrderDateDesc();

    @Query("select coalesce(sum(o.totalAmount), 0) from SemsOrder o")
    BigDecimal sumTotalAmount();

    @Query("select coalesce(sum(o.totalAmount), 0) from SemsOrder o where o.dealer.id = :dealerId")
    BigDecimal sumTotalAmountByDealerId(@Param("dealerId") Long dealerId);

    List<Order> findByDealerIdIn(Collection<Long> dealerIds);
}
