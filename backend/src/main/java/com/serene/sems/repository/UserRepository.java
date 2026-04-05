package com.serene.sems.repository;

import com.serene.sems.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findByUsernameStartingWith(String prefix);

    /** {@code pattern} may contain SQL {@code %} wildcards, e.g. {@code dealer%@bharatsems.demo}. */
    List<User> findByEmailLike(String pattern);

    List<User> findByEmailEndingWith(String suffix);
}
