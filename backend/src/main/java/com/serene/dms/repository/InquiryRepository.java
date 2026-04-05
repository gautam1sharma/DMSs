package com.serene.dms.repository;

import com.serene.dms.entity.Inquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface InquiryRepository extends JpaRepository<Inquiry, Long>, QuerydslPredicateExecutor<Inquiry> {
    Page<Inquiry> findByDealerId(Long dealerId, Pageable pageable);
    Page<Inquiry> findByCustomerId(Long customerId, Pageable pageable);
    long countByDealerIdAndStatus(Long dealerId, Inquiry.InquiryStatus status);
}
