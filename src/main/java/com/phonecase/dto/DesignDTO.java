package com.phonecase.dto;

/**
 * DTO для передачі даних дизайну між шарами.
 * Реалізує патерн Data Transfer Object.
 */

public class DesignDTO {
    private Integer id;
    private String name;
    private String description;
    private int categoryId;
    private String categoryName;
    private double price;
    private boolean isAvailable;
    private String imagePath;
    private Integer createdBy;


    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public Integer getCreatedBy() { return createdBy; }
    public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }

    @Override
    public String toString() { return "DesignDTO{id=" + id + ", name='" + name + "'}"; }
}