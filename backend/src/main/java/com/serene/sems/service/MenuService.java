package com.serene.sems.service;

import com.serene.sems.dto.CreateMenuItemRequest;
import com.serene.sems.dto.MenuItemResponse;
import com.serene.sems.dto.UpdateMenuItemRequest;
import com.serene.sems.exception.ResourceNotFoundException;
import com.serene.sems.model.MenuItem;
import com.serene.sems.model.Role;
import com.serene.sems.repository.MenuItemRepository;
import com.serene.sems.repository.RoleRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MenuService {

    private final MenuItemRepository menuItemRepository;
    private final RoleRepository roleRepository;

    public MenuService(MenuItemRepository menuItemRepository, RoleRepository roleRepository) {
        this.menuItemRepository = menuItemRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<MenuItemResponse> listAllAdmin() {
        return menuItemRepository.findAllWithRoles().stream()
                .sorted(Comparator.comparingInt(MenuItem::getSortOrder).thenComparing(MenuItem::getId))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<MenuItemResponse> listForCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return List.of();
        }
        Set<String> userRoles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.startsWith("ROLE_") ? a.substring(6) : a)
                .collect(Collectors.toSet());

        return menuItemRepository.findAllWithRoles().stream()
                .filter(MenuItem::isEnabled)
                .filter(m -> m.getRoles().stream().anyMatch(r -> userRoles.contains(r.getName())))
                .sorted(Comparator.comparingInt(MenuItem::getSortOrder).thenComparing(MenuItem::getId))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public MenuItemResponse create(CreateMenuItemRequest req) {
        MenuItem m = new MenuItem();
        m.setLabel(req.getLabel());
        m.setPath(req.getPath());
        m.setIcon(req.getIcon());
        m.setSortOrder(req.getSortOrder());
        m.setEnabled(req.isEnabled());
        if (req.getParentId() != null) {
            MenuItem parent = menuItemRepository.findById(req.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent menu not found"));
            m.setParent(parent);
        }
        m.setRoles(resolveRoles(req.getRoleNames()));
        return toResponse(menuItemRepository.save(m));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public MenuItemResponse update(Long id, UpdateMenuItemRequest req) {
        MenuItem m = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
        if (req.getLabel() != null) {
            m.setLabel(req.getLabel());
        }
        if (req.getPath() != null) {
            m.setPath(req.getPath());
        }
        if (req.getIcon() != null) {
            m.setIcon(req.getIcon());
        }
        if (req.getSortOrder() != null) {
            m.setSortOrder(req.getSortOrder());
        }
        if (req.getEnabled() != null) {
            m.setEnabled(req.getEnabled());
        }
        if (req.getParentId() != null) {
            if (req.getParentId().equals(id)) {
                throw new IllegalArgumentException("Menu item cannot be its own parent");
            }
            MenuItem parent = menuItemRepository.findById(req.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent menu not found"));
            m.setParent(parent);
        }
        if (req.getRoleNames() != null) {
            m.setRoles(resolveRoles(req.getRoleNames()));
        }
        return toResponse(menuItemRepository.save(m));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void delete(Long id) {
        if (!menuItemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Menu item not found");
        }
        menuItemRepository.deleteById(id);
    }

    /**
     * Seeds default portal navigation (SPA paths) when the table is empty.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void ensureDefaultMenusIfEmpty() {
        if (menuItemRepository.count() > 0) {
            return;
        }
        Role admin = roleRepository.findByName("ADMIN").orElseThrow();
        Role dealer = roleRepository.findByName("DEALER").orElseThrow();

        addSeed("/admin", "Dashboard", "DashboardOutlined", 10, admin);
        addSeed("/admin/dealers", "Dealers", "TeamOutlined", 20, admin);
        addSeed("/admin/customers", "Customers", "UserOutlined", 30, admin);
        addSeed("/admin/products", "Products", "ShoppingOutlined", 40, admin);
        addSeed("/admin/orders", "Orders", "ShoppingCartOutlined", 50, admin);
        addSeed("/admin/users", "Users", "UserOutlined", 60, admin);
        addSeed("/admin/audit-logs", "Audit logs", "FileSearchOutlined", 70, admin);
        addSeed("/admin/menus", "Menus", "MenuOutlined", 75, admin);

        addSeed("/dealer", "Dashboard", "DashboardOutlined", 10, dealer);
        addSeed("/dealer/customers", "My customers", "UserOutlined", 20, dealer);
        addSeed("/dealer/products", "Products", "ShoppingOutlined", 30, dealer);
        addSeed("/dealer/orders", "Orders", "ShoppingCartOutlined", 40, dealer);
        addSeed("/dealer/profile", "Profile", "UserOutlined", 50, dealer);
    }

    private void addSeed(String path, String label, String icon, int sort, Role role) {
        MenuItem m = new MenuItem();
        m.setPath(path);
        m.setLabel(label);
        m.setIcon(icon);
        m.setSortOrder(sort);
        m.setEnabled(true);
        m.getRoles().add(role);
        menuItemRepository.save(m);
    }

    /**
     * Adds the admin "Menus" CRUD link for DBs that were seeded before that entry existed.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void ensureAdminMenusCrudLink() {
        if (menuItemRepository.existsByPath("/admin/menus")) {
            return;
        }
        Role admin = roleRepository.findByName("ADMIN").orElseThrow();
        addSeed("/admin/menus", "Menus", "MenuOutlined", 75, admin);
    }

    private Set<Role> resolveRoles(Set<String> names) {
        Set<Role> set = new HashSet<>();
        for (String n : names) {
            Role r = roleRepository.findByName(n)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown role: " + n));
            set.add(r);
        }
        return set;
    }

    private MenuItemResponse toResponse(MenuItem m) {
        MenuItemResponse r = new MenuItemResponse();
        r.setId(m.getId());
        r.setLabel(m.getLabel());
        r.setPath(m.getPath());
        r.setIcon(m.getIcon());
        r.setSortOrder(m.getSortOrder());
        r.setParentId(m.getParent() != null ? m.getParent().getId() : null);
        r.setEnabled(m.isEnabled());
        r.setRoleNames(m.getRoles().stream().map(Role::getName).sorted().collect(Collectors.toList()));
        return r;
    }
}
