package com.serene.dms.repository;

import com.serene.dms.entity.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface VehicleRepository extends JpaRepository<Vehicle, Long>, QuerydslPredicateExecutor<Vehicle> {
    Page<Vehicle> findByDealerId(Long dealerId, Pageable pageable);
    Page<Vehicle> findByDealerIdAndStatus(Long dealerId, Vehicle.VehicleStatus status, Pageable pageable);
    long countByDealerIdAndStatus(Long dealerId, Vehicle.VehicleStatus status);
    boolean existsByVin(String vin);
}
