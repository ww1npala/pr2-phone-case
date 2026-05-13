package com.phonecase.service;

import com.phonecase.dto.DesignDTO;
import com.phonecase.model.Category;
import com.phonecase.model.Design;
import com.phonecase.repository.CategoryRepository;
import com.phonecase.repository.DesignRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * sервіс управління дизайнами чохлів.
 * rеалізує основну бізнес-логіку модуля.
 * pатерни: Facade, DI, Singleton.
 */
@Singleton
public class DesignServiceImpl implements DesignService {

    private static final Logger logger = LoggerFactory.getLogger(DesignServiceImpl.class);

    private final DesignRepository designRepository;
    private final CategoryRepository categoryRepository;

    @Inject
    public DesignServiceImpl(DesignRepository designRepository, CategoryRepository categoryRepository) {
        this.designRepository = designRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public DesignDTO createDesign(DesignDTO dto) {
        validateDesignDTO(dto);

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Категорія не знайдена: " + dto.getCategoryId()));

        Design design = new Design.Builder()
                .name(dto.getName().trim())
                .description(dto.getDescription())
                .category(category)
                .price(dto.getPrice())
                .imagePath(dto.getImagePath())
                .available(dto.isAvailable())
                .createdBy(dto.getCreatedBy())
                .build();

      Design saved = designRepository.save(design);
      saved.setCategory(category);
      logger.info("Створено новий дизайн: id={}, name={}", saved.getId(), saved.getName());
      return toDTO(saved);
    }

    @Override
    public Optional<DesignDTO> findById(int id) {
        return designRepository.findById(id).map(this::toDTO);
    }

    @Override
    public List<DesignDTO> getAllDesigns() {
        return designRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<DesignDTO> getAvailableDesigns() {
        return designRepository.findAvailable().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<DesignDTO> getDesignsByCategory(int categoryId) {
        return designRepository.findByCategoryId(categoryId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<DesignDTO> searchDesigns(String query) {
        if (query == null || query.isBlank()) return getAllDesigns();
        return designRepository.searchByName(query.trim()).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<DesignDTO> getDesignsByPhoneModel(int phoneModelId) {
        return designRepository.findByPhoneModelId(phoneModelId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public DesignDTO updateDesign(DesignDTO dto) {
        validateDesignDTO(dto);

        Design existing = designRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Дизайн не знайдено: " + dto.getId()));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Категорія не знайдена: " + dto.getCategoryId()));

        existing.setName(dto.getName().trim());
        existing.setDescription(dto.getDescription());
        existing.setCategory(category);
        existing.setPrice(dto.getPrice());
        existing.setImagePath(dto.getImagePath());
        existing.setAvailable(dto.isAvailable());

        Design updated = designRepository.update(existing);
        logger.info("Оновлено дизайн: id={}", updated.getId());
        return toDTO(updated);
    }

    @Override
    public boolean deleteDesign(int id) {
        boolean deleted = designRepository.deleteById(id);
        if (deleted) logger.info("Видалено дизайн: id={}", id);
        return deleted;
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // vалідація бізнес логіки
    private void validateDesignDTO(DesignDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isBlank())
            throw new IllegalArgumentException("Назва дизайну обов'язкова.");
        if (dto.getName().trim().length() < 2)
            throw new IllegalArgumentException("Назва дизайну повинна містити щонайменше 2 символи.");
        if (dto.getPrice() < 0)
            throw new IllegalArgumentException("Ціна не може бути від'ємною.");
        if (dto.getPrice() > 10000)
            throw new IllegalArgumentException("Ціна перевищує максимально допустиме значення (10000).");
        if (dto.getCategoryId() <= 0)
            throw new IllegalArgumentException("Категорія обов'язкова.");
    }


    private DesignDTO toDTO(Design d) {
        DesignDTO dto = new DesignDTO();
        dto.setId(d.getId());
        dto.setName(d.getName());
        dto.setDescription(d.getDescription());
        if (d.getCategory() != null) {
            dto.setCategoryId(d.getCategory().getId());
            dto.setCategoryName(d.getCategory().getName());
        }
        dto.setPrice(d.getPrice());
        dto.setAvailable(d.isAvailable());
        dto.setImagePath(d.getImagePath());
        dto.setCreatedBy(d.getCreatedBy());
        return dto;
    }
}