package com.phonecase.ui.viewmodel;

import com.phonecase.dto.DesignDTO;
import javafx.beans.property.*;

/**
 * ViewModel для дизайну чохла.
 * Реалізує патерн MVVM — обгортає DesignDTO в JavaFX Property для binding.
 * Дозволяє таблиці автоматично оновлюватись при зміні даних.
 */
public class DesignViewModel {

    private final IntegerProperty id           = new SimpleIntegerProperty();
    private final StringProperty  name         = new SimpleStringProperty();
    private final StringProperty  description  = new SimpleStringProperty();
    private final StringProperty  categoryName = new SimpleStringProperty();
    private final DoubleProperty  price        = new SimpleDoubleProperty();
    private final BooleanProperty available    = new SimpleBooleanProperty();
    private final StringProperty  imagePath    = new SimpleStringProperty();


    public DesignViewModel(DesignDTO dto) {
        id.set(dto.getId() != null ? dto.getId() : 0);
        name.set(dto.getName());
        description.set(dto.getDescription());
        categoryName.set(dto.getCategoryName());
        price.set(dto.getPrice());
        available.set(dto.isAvailable());
        imagePath.set(dto.getImagePath());
    }

    public IntegerProperty idProperty()           { return id; }
    public StringProperty  nameProperty()          { return name; }
    public StringProperty  descriptionProperty()   { return description; }
    public StringProperty  categoryNameProperty()  { return categoryName; }
    public DoubleProperty  priceProperty()         { return price; }
    public BooleanProperty availableProperty()     { return available; }
    public StringProperty  imagePathProperty()     { return imagePath; }

    public int     getId()          { return id.get(); }
    public String  getName()        { return name.get(); }
    public String  getDescription() { return description.get(); }
    public String  getCategoryName(){ return categoryName.get(); }
    public double  getPrice()       { return price.get(); }
    public boolean isAvailable()    { return available.get(); }
    public String  getImagePath()   { return imagePath.get(); }


    public String getFormattedPrice() {
        return String.format("%.2f грн", price.get());
    }


    public String getAvailableDisplay() {
        return available.get() ? "✓ Так" : "✗ Ні";
    }

    public DesignDTO toDTO() {
        DesignDTO dto = new DesignDTO();
        dto.setId(id.get());
        dto.setName(name.get());
        dto.setDescription(description.get());
        dto.setCategoryName(categoryName.get());
        dto.setPrice(price.get());
        dto.setAvailable(available.get());
        dto.setImagePath(imagePath.get());
        return dto;
    }

    @Override
    public String toString() {
        return "DesignViewModel{name='" + name.get() + "', price=" + price.get() + "}";
    }
}