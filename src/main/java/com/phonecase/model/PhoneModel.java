package com.phonecase.model;

/**
 * Модель мобільного телефону, для якого підтримується чохол.
 */
public class PhoneModel extends BaseEntity {
    private String brand;
    private String modelName;
    private Double screenSize;
    private Integer releaseYear;

    public PhoneModel() {}
    public PhoneModel(String brand, String modelName) {
        this.brand = brand;
        this.modelName = modelName;
    }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public Double getScreenSize() { return screenSize; }
    public void setScreenSize(Double screenSize) { this.screenSize = screenSize; }
    public Integer getReleaseYear() { return releaseYear; }
    public void setReleaseYear(Integer releaseYear) { this.releaseYear = releaseYear; }


    public String getFullName() { return brand + " " + modelName; }

    @Override public String toString() { return getFullName(); }
}