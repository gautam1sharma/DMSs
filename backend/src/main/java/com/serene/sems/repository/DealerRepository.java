package com.serene.sems.repository;

import com.serene.sems.model.Dealer;
import com.serene.sems.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DealerRepository extends JpaRepository<Dealer, Long> {

    Optional<Dealer> findByUser(User user);

    Optional<Dealer> findByUserUsername(String username);

    Page<Dealer> findByCompanyNameContainingIgnoreCase(String q, Pageable pageable);

    /** Active dealers in this exact jurisdiction (city match is case-insensitive). */
    List<Dealer> findByCountryCodeAndStateCodeAndCityIgnoreCaseAndActiveTrueOrderByIdAsc(
            String countryCode, String stateCode, String city);
}
