package com.phonecase.service;

import com.phonecase.dto.DesignDTO;
import com.phonecase.model.Category;
import com.phonecase.model.Design;
import com.phonecase.repository.CategoryRepository;
import com.phonecase.repository.DesignRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Інтеграційні тести для DesignServiceImpl.
 * Перевіряє взаємодію між сервісним шаром та репозиторіями.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DesignService — інтеграційні тести")
class DesignServiceImplTest {

    @Mock
    private DesignRepository designRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private DesignServiceImpl designService;

    private Category testCategory;
    private Design testDesign;

    @BeforeEach
    void setUp() {
        testCategory = new Category("Природа", "Природні мотиви");
        testCategory.setId(1);

        testDesign = new Design.Builder()
                .name("Карпатський захід")
                .description("Гарний захід сонця")
                .category(testCategory)
                .price(249.99)
                .available(true)
                .createdBy(1)
                .build();
        testDesign.setId(1);
    }

    @Test
    @DisplayName("createDesign — успішне створення дизайну")
    void testCreateDesignSuccess() {
        DesignDTO dto = new DesignDTO();
        dto.setName("Новий дизайн");
        dto.setDescription("Опис");
        dto.setCategoryId(1);
        dto.setPrice(199.99);
        dto.setAvailable(true);
        dto.setCreatedBy(1);

        when(categoryRepository.findById(1)).thenReturn(Optional.of(testCategory));
        when(designRepository.save(any(Design.class))).thenAnswer(inv -> {
            Design d = inv.getArgument(0);
            d.setId(100);
            return d;
        });

        DesignDTO result = designService.createDesign(dto);

        assertNotNull(result);
        assertEquals("Новий дизайн", result.getName());
        verify(designRepository, times(1)).save(any(Design.class));
        verify(categoryRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("createDesign — кидає виключення при порожній назві")
    void testCreateDesignEmptyName() {
        DesignDTO dto = new DesignDTO();
        dto.setName("   ");
        dto.setCategoryId(1);
        dto.setPrice(100);

        assertThrows(IllegalArgumentException.class, () -> designService.createDesign(dto));
        verify(designRepository, never()).save(any());
    }

    @Test
    @DisplayName("createDesign — кидає виключення при від'ємній ціні")
    void testCreateDesignNegativePrice() {
        DesignDTO dto = new DesignDTO();
        dto.setName("Тест");
        dto.setCategoryId(1);
        dto.setPrice(-50);

        assertThrows(IllegalArgumentException.class, () -> designService.createDesign(dto));
    }

    @Test
    @DisplayName("createDesign — кидає виключення при ненайденій категорії")
    void testCreateDesignCategoryNotFound() {
        DesignDTO dto = new DesignDTO();
        dto.setName("Тест");
        dto.setCategoryId(999);
        dto.setPrice(100);

        when(categoryRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> designService.createDesign(dto));
    }

    @Test
    @DisplayName("findById — повертає DTO для існуючого дизайну")
    void testFindByIdSuccess() {
        when(designRepository.findById(1)).thenReturn(Optional.of(testDesign));

        Optional<DesignDTO> result = designService.findById(1);

        assertTrue(result.isPresent());
        assertEquals("Карпатський захід", result.get().getName());
        assertEquals(249.99, result.get().getPrice(), 0.001);
    }

    @Test
    @DisplayName("findById — повертає порожній Optional для неіснуючого")
    void testFindByIdNotFound() {
        when(designRepository.findById(999)).thenReturn(Optional.empty());

        Optional<DesignDTO> result = designService.findById(999);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("getAllDesigns — повертає список всіх дизайнів")
    void testGetAllDesigns() {
        when(designRepository.findAll()).thenReturn(List.of(testDesign));

        List<DesignDTO> result = designService.getAllDesigns();

        assertEquals(1, result.size());
        assertEquals("Карпатський захід", result.get(0).getName());
    }

    @Test
    @DisplayName("searchDesigns — порожній запит повертає всі дизайни")
    void testSearchDesignsEmptyQuery() {
        when(designRepository.findAll()).thenReturn(List.of(testDesign));

        List<DesignDTO> result = designService.searchDesigns("");

        assertEquals(1, result.size());
        verify(designRepository, times(1)).findAll();
        verify(designRepository, never()).searchByName(any());
    }

    @Test
    @DisplayName("searchDesigns — непорожній запит викликає searchByName")
    void testSearchDesignsWithQuery() {
        when(designRepository.searchByName("Карпат")).thenReturn(List.of(testDesign));

        List<DesignDTO> result = designService.searchDesigns("Карпат");

        assertEquals(1, result.size());
        verify(designRepository, times(1)).searchByName("Карпат");
    }

    @Test
    @DisplayName("deleteDesign — успішне видалення")
    void testDeleteDesign() {
        when(designRepository.deleteById(1)).thenReturn(true);

        boolean result = designService.deleteDesign(1);

        assertTrue(result);
        verify(designRepository, times(1)).deleteById(1);
    }

    @Test
    @DisplayName("getAllCategories — делегує до categoryRepository")
    void testGetAllCategories() {
        when(categoryRepository.findAll()).thenReturn(List.of(testCategory));

        List<Category> result = designService.getAllCategories();

        assertEquals(1, result.size());
        assertEquals("Природа", result.get(0).getName());
    }
}