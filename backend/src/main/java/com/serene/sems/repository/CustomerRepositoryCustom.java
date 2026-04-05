package com.serene.sems.repository;

import com.serene.sems.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Dynamic customer queries built with QueryDSL (see {@link CustomerRepositoryImpl}).
 */
public interface CustomerRepositoryCustom {

    /**
     * @param dealerId optional filter by dealer
     * @param nameQuery optional case-insensitive substring match on full name
     */
    Page<Customer> findCustomers(Long dealerId, String nameQuery, Pageable pageable);
}
