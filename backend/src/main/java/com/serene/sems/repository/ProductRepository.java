package com.serene.sems.repository;

import com.serene.sems.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByName(String name);

    Page<Product> findByCategoryIgnoreCase(String category, Pageable pageable);

    Page<Product> findByNameContainingIgnoreCase(String q, Pageable pageable);

    Page<Product> findByCategoryIgnoreCaseAndNameContainingIgnoreCase(String category, String q, Pageable pageable);
}
