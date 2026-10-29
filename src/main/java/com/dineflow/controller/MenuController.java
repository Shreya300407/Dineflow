package com.dineflow.controller;

import com.dineflow.model.MenuItem;
import com.dineflow.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class MenuController {

    private final MenuService menuService;

    @Autowired
    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    /**
     * Retrieve all menu items, with optional category filtering and search queries.
     */
    @GetMapping("/menu")
    public List<MenuItem> getMenu(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {
        if ((category != null && !category.equalsIgnoreCase("All")) || (search != null && !search.trim().isEmpty())) {
            return menuService.searchItems(search, category);
        }
        return menuService.getAllItems();
    }

    /**
     * Retrieve available menu categories.
     */
    @GetMapping("/menu/categories")
    public List<String> getCategories() {
        return menuService.getCategories();
    }
}
