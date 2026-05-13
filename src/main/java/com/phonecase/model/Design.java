package com.phonecase.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Дизайн чохла для мобільного телефону.
 * Містить зв'язки 1:N (категорія) та N:M (моделі телефонів через design_compatibility).
 * Реалізує Композицію — містить список сумісних моделей.
 */

public class Design extends BaseEntity {

    private String name;
    private String description;
    private Category category;
    private String imagePath;
    private double price;
    private boolean isAvailable;
    private Integer createdBy;
    private LocalDateTime createdAt;


    private List<PhoneModel> compatibleModels = new ArrayList<>();

    public Design() {}


    public Design(String name, String description, Category category, double price) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.isAvailable = true;
    }


    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }
    public Integer getCreatedBy() { return createdBy; }
    public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<PhoneModel> getCompatibleModels() { return compatibleModels; }
    public void setCompatibleModels(List<PhoneModel> compatibleModels) { this.compatibleModels = compatibleModels; }

    @Override
    public String toString() {
        return "Design{id=" + id + ", name='" + name + "', price=" + price + "}";
    }


    public static class Builder {
        private final Design design = new Design();

        public Builder name(String name)               { design.name = name;               return this; }
        public Builder description(String desc)        { design.description = desc;         return this; }
        public Builder category(Category cat)          { design.category = cat;             return this; }
        public Builder imagePath(String path)          { design.imagePath = path;           return this; }
        public Builder price(double price)             { design.price = price;              return this; }
        public Builder available(boolean avail)        { design.isAvailable = avail;        return this; }
        public Builder createdBy(Integer userId)       { design.createdBy = userId;         return this; }


        public Design build() {
            if (design.name == null || design.name.isBlank())
                throw new IllegalStateException("Назва дизайну обов'язкова");
            if (design.price < 0)
                throw new IllegalStateException("Ціна не може бути від'ємною");
            return design;
        }
    }
}