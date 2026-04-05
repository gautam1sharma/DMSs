package com.serene.sems.repository;

import com.serene.sems.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    @Query("select distinct m from MenuItem m left join fetch m.roles order by m.sortOrder, m.id")
    List<MenuItem> findAllWithRoles();

    boolean existsByPath(String path);
}
