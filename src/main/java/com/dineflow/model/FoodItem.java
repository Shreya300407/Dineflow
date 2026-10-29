package com.dineflow.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "food_items")
public class FoodItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private double price;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    private String rating;
    private String badge;
    private String badgeType;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "food_item_tags", joinColumns = @JoinColumn(name = "food_item_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    public FoodItem() {}

    public FoodItem(Long id, String name, String description, String category, double price, String imageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    public FoodItem(String name, String description, String category, double price, String imageUrl,
                    String rating, String badge, String badgeType, List<String> tags) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.badge = badge;
        this.badgeType = badgeType;
        this.tags = tags != null ? tags : new ArrayList<>();
    }

    public FoodItem(Long id, String name, String description, String category, double price, String imageUrl,
                    String rating, String badge, String badgeType, List<String> tags) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.badge = badge;
        this.badgeType = badgeType;
        this.tags = tags != null ? tags : new ArrayList<>();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getImage() { return imageUrl; }

    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }

    public String getBadge() { return badge; }
    public void setBadge(String badge) { this.badge = badge; }

    public String getBadgeType() { return badgeType; }
    public void setBadgeType(String badgeType) { this.badgeType = badgeType; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags != null ? tags : new ArrayList<>(); }
}
