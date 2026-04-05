package com.serene.dms.repository;

import com.serene.dms.entity.Dealer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.Optional;

public interface DealerRepository extends JpaRepository<Dealer, Long>, QuerydslPredicateExecutor<Dealer> {
    Optional<Dealer> findByCode(String code);
    Optional<Dealer> findByUserId(Long userId);
    boolean existsByCode(String code);
    boolean existsByEmail(String email);
    Page<Dealer> findByStatus(Dealer.DealerStatus status, Pageable pageable);
}
