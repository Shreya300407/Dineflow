package com.dineflow.controller;

import com.dineflow.model.FoodItem;
import com.dineflow.service.FoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/foods")
@CrossOrigin(origins = "*")
public class FoodController {

    private final FoodService foodService;

    @Autowired
    public FoodController(FoodService foodService) {
        this.foodService = foodService;
    }

    /**
     * GET /api/foods
     * Returns all food items in the Dineflow catalog.
     * Optionally supports ?search=... keyword querying.
     */
    @GetMapping
    public List<FoodItem> getAllFoods(@RequestParam(required = false) String search) {
        if (search != null && !search.trim().isEmpty()) {
            return foodService.searchFoods(search);
        }
        return foodService.getAllFoods();
    }

    /**
     * GET /api/foods/{id}
     * Returns a single food item by its numeric ID.
     * Returns 404 NOT_FOUND with an informative JSON message if no item exists.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getFoodById(@PathVariable Long id) {
        Optional<FoodItem> food = foodService.getFoodById(id);
        if (food.isPresent()) {
            return ResponseEntity.ok(food.get());
        }

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Not Found");
        errorResponse.put("message", "Food item with ID " + id + " was not found.");
        errorResponse.put("status", HttpStatus.NOT_FOUND.value());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * GET /api/foods/category/{category}
     * Returns all food items belonging to the requested category (e.g. Starters, Main Course, Pizza, Burgers, Desserts, Drinks).
     */
    @GetMapping("/category/{category}")
    public List<FoodItem> getFoodsByCategory(@PathVariable String category) {
        return foodService.getFoodsByCategory(category);
    }
}
