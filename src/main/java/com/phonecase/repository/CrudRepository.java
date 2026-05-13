package com.phonecase.repository;

import java.util.List;
import java.util.Optional;

/**
 * Загальний інтерфейс репозиторію. Реалізує патерн Repository.
 * Визначає базові CRUD-операції для всіх сутностей.
 */

public interface CrudRepository<T, ID> {

    T save(T entity);

    Optional<T> findById(ID id);

    List<T> findAll();

    T update(T entity);

    boolean deleteById(ID id);

    int count();
}