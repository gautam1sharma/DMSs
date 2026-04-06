package com.serene.sems.repository;

import com.serene.sems.model.Customer;
import com.serene.sems.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long>, CustomerRepositoryCustom {

    long countByDealerId(Long dealerId);

    List<Customer> findByDealerId(Long dealerId);

    List<Customer> findByDealerIdIn(Collection<Long> dealerIds);

    Optional<Customer> findByUser(User user);
}
