package com.serene.sems.controller;

import com.serene.sems.dto.MenuItemResponse;
import com.serene.sems.service.MenuService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${app.api.base-path}/me")
@Tag(name = "Current user")
@SecurityRequirement(name = "bearerAuth")
public class CurrentUserMenuController {

    private final MenuService menuService;

    public CurrentUserMenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/menus")
    public List<MenuItemResponse> menusForCurrentUser() {
        return menuService.listForCurrentUser();
    }
}
