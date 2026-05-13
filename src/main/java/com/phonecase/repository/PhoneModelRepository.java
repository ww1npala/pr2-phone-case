package com.phonecase.repository;

import com.phonecase.model.PhoneModel;
import java.util.List;
import java.util.Optional;

/** Репозиторій моделей телефонів. */

public interface PhoneModelRepository extends CrudRepository<PhoneModel, Integer> {
    List<PhoneModel> findByBrand(String brand);
    Optional<PhoneModel> findByBrandAndModel(String brand, String modelName);
}