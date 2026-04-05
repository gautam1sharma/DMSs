package com.serene.sems.repository;

import com.serene.sems.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long>, CustomerRepositoryCustom {

    long countByDealerId(Long dealerId);

    List<Customer> findByDealerIdIn(Collection<Long> dealerIds);
}
