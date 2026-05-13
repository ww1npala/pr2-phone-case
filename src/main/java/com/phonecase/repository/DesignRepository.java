package com.phonecase.repository;

import com.phonecase.model.Design;
import java.util.List;

/**
 * Репозиторій для роботи з дизайнами чохлів.
 * Розширює базовий CRUD-репозиторій специфічними запитами.
 */
public interface DesignRepository extends CrudRepository<Design, Integer> {


    List<Design> findAvailable();


    List<Design> findByCategoryId(int categoryId);


    List<Design> searchByName(String query);


    List<Design> findByPhoneModelId(int phoneModelId);


    void addCompatibility(int designId, int phoneModelId);


    void removeAllCompatibilities(int designId);


    List<Design> findByCreatedBy(int userId);
}