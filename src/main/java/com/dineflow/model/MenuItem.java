package com.dineflow.model;

import java.util.List;

public class MenuItem {
    private Long id;
    private String name;
    private String category;
    private double price;
    private String rating;
    private String badge;
    private String badgeType;
    private String image;
    private String description;
    private List<String> tags;

    public MenuItem() {}

    public MenuItem(Long id, String name, String category, double price, String rating, 
                    String badge, String badgeType, String image, String description, List<String> tags) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.rating = rating;
        this.badge = badge;
        this.badgeType = badgeType;
        this.image = image;
        this.description = description;
        this.tags = tags;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }

    public String getBadge() { return badge; }
    public void setBadge(String badge) { this.badge = badge; }

    public String getBadgeType() { return badgeType; }
    public void setBadgeType(String badgeType) { this.badgeType = badgeType; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}
