package com.serene.dms.repository;

import com.serene.dms.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, QuerydslPredicateExecutor<User> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    Page<User> findByEnabledTrue(Pageable pageable);

    @Modifying
    @Query("UPDATE User u SET u.failedAttempts = u.failedAttempts + 1 WHERE u.email = :email")
    void incrementFailedAttempts(String email);

    @Modifying
    @Query("UPDATE User u SET u.failedAttempts = 0, u.accountLocked = false WHERE u.email = :email")
    void resetFailedAttempts(String email);

    @Modifying
    @Query("UPDATE User u SET u.accountLocked = true WHERE u.email = :email")
    void lockAccount(String email);
}
