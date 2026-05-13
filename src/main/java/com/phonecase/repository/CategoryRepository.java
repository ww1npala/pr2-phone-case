package com.phonecase.repository;

import com.phonecase.model.Category;
import java.util.Optional;

/**
 * Репозиторій категорій.
 */

public interface CategoryRepository extends CrudRepository<Category, Integer> {
    Optional<Category> findByName(String name);
}