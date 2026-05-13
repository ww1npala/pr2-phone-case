package com.phonecase.service;

import com.phonecase.dto.DesignDTO;
import com.phonecase.model.Category;
import java.util.List;
import java.util.Optional;

/**
 * Інтерфейс сервісу дизайнів.
 */

public interface DesignService {
    DesignDTO createDesign(DesignDTO dto);
    Optional<DesignDTO> findById(int id);
    List<DesignDTO> getAllDesigns();
    List<DesignDTO> getAvailableDesigns();
    List<DesignDTO> getDesignsByCategory(int categoryId);
    List<DesignDTO> searchDesigns(String query);
    List<DesignDTO> getDesignsByPhoneModel(int phoneModelId);
    DesignDTO updateDesign(DesignDTO dto);
    boolean deleteDesign(int id);
    List<Category> getAllCategories();
}