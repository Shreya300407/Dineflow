package com.dineflow.repository;

import com.dineflow.model.FoodItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {

    /**
     * Find food items by their category (case-insensitive).
     */
    List<FoodItem> findByCategoryIgnoreCase(String category);

    /**
     * Search food items matching keyword in name or description.
     */
    List<FoodItem> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);
}
