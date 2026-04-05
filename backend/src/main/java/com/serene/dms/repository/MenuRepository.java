package com.serene.dms.repository;

import com.serene.dms.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    List<Menu> findByParentIsNullAndEnabledTrueOrderBySortOrderAsc();

    List<Menu> findByParentIdAndEnabledTrueOrderBySortOrderAsc(Long parentId);

    @Query("SELECT m FROM Menu m WHERE m.enabled = true AND (m.roles IS NULL OR m.roles LIKE %:role%) ORDER BY m.sortOrder")
    List<Menu> findMenusForRole(String role);
}
