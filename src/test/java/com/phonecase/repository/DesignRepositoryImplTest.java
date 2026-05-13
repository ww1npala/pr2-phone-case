package com.phonecase.repository;

import com.phonecase.config.DatabaseConnection;
import com.phonecase.model.Category;
import com.phonecase.model.Design;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Юніт-тести для DesignRepositoryImpl.
 * Використовує H2 in-memory БД для ізольованого тестування.
 * Патерн: Test Repository Pattern.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DesignRepositoryImplTest {

    private static DesignRepositoryImpl designRepository;
    private static CategoryRepositoryImpl categoryRepository;
    private static TestDatabaseConnection testDb;

    static class TestDatabaseConnection extends DatabaseConnection {
        private final Connection connection;

        TestDatabaseConnection() {
            super();
            try {
                this.connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Connection getConnection() { return connection; }

        @Override
        public void releaseConnection(Connection c) {}
    }

    @BeforeAll
    static void setup() throws SQLException {
        testDb = new TestDatabaseConnection();
        categoryRepository = new CategoryRepositoryImpl(testDb);
        designRepository   = new DesignRepositoryImpl(testDb);
        createSchema();
        seedCategories();
    }

    private static void createSchema() throws SQLException {
        Connection conn = testDb.getConnection();
        Statement st = conn.createStatement();

        st.execute("""
            CREATE TABLE IF NOT EXISTS categories (
                id INTEGER PRIMARY KEY AUTO_INCREMENT,
                name VARCHAR(100) NOT NULL UNIQUE,
                description TEXT
            )
        """);

        st.execute("""
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTO_INCREMENT,
                username VARCHAR(50) NOT NULL UNIQUE,
                password TEXT NOT NULL,
                email VARCHAR(100),
                role VARCHAR(20) DEFAULT 'USER',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                is_active INTEGER DEFAULT 1
            )
        """);

        st.execute("""
            CREATE TABLE IF NOT EXISTS designs (
                id INTEGER PRIMARY KEY AUTO_INCREMENT,
                name VARCHAR(200) NOT NULL,
                description TEXT,
                category_id INTEGER NOT NULL,
                image_path TEXT,
                price DOUBLE NOT NULL DEFAULT 0.0,
                is_available INTEGER DEFAULT 1,
                created_by INTEGER DEFAULT 1,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);

        st.execute("""
            CREATE TABLE IF NOT EXISTS phone_models (
                id INTEGER PRIMARY KEY AUTO_INCREMENT,
                brand VARCHAR(50),
                model_name VARCHAR(100),
                screen_size DOUBLE,
                release_year INTEGER
            )
        """);

        st.execute("""
            CREATE TABLE IF NOT EXISTS design_compatibility (
                design_id INTEGER NOT NULL,
                phone_model_id INTEGER NOT NULL,
                PRIMARY KEY (design_id, phone_model_id)
            )
        """);

        st.execute("INSERT INTO users (username, password, email) VALUES ('testuser', 'hash', 'test@test.com')");
    }

    private static void seedCategories() {
        Category cat = new Category("Тест-категорія", "Опис тест категорії");
        categoryRepository.save(cat);
    }


    @Test
    @Order(1)
    @DisplayName("Збереження нового дизайну")
    void testSaveDesign() {

        Category cat = categoryRepository.findAll().get(0);
        Design design = new Design.Builder()
                .name("Тестовий дизайн")
                .description("Опис тестового дизайну")
                .category(cat)
                .price(199.99)
                .available(true)
                .createdBy(1)
                .build();

        Design saved = designRepository.save(design);


        assertNotNull(saved.getId(), "ID повинен бути присвоєний після збереження");
        assertTrue(saved.getId() > 0, "ID повинен бути позитивним числом");
        assertEquals("Тестовий дизайн", saved.getName());
        assertEquals(199.99, saved.getPrice(), 0.001);
    }

    @Test
    @Order(2)
    @DisplayName("Пошук дизайну за ID")
    void testFindById() {
        Category cat = categoryRepository.findAll().get(0);
        Design design = new Design.Builder()
                .name("Дизайн для пошуку")
                .category(cat)
                .price(299.99)
                .available(true)
                .createdBy(1)
                .build();
        Design saved = designRepository.save(design);

        Optional<Design> found = designRepository.findById(saved.getId());

        assertTrue(found.isPresent(), "Дизайн повинен бути знайдений");
        assertEquals("Дизайн для пошуку", found.get().getName());
        assertEquals(299.99, found.get().getPrice(), 0.001);
    }

    @Test
    @Order(3)
    @DisplayName("Пошук неіснуючого дизайну повертає порожній Optional")
    void testFindByIdNotFound() {
        Optional<Design> result = designRepository.findById(99999);

        assertFalse(result.isPresent(), "Для неіснуючого ID повинен бути порожній Optional");
    }

    @Test
    @Order(4)
    @DisplayName("Отримання всіх дизайнів")
    void testFindAll() {
        List<Design> designs = designRepository.findAll();

        assertNotNull(designs, "Список дизайнів не повинен бути null");
        assertTrue(designs.size() >= 2, "Повинно бути щонайменше 2 дизайни (з попередніх тестів)");
    }

    @Test
    @Order(5)
    @DisplayName("Оновлення дизайну")
    void testUpdateDesign() {
        Category cat = categoryRepository.findAll().get(0);
        Design design = new Design.Builder()
                .name("Дизайн до оновлення")
                .category(cat)
                .price(100.00)
                .available(true)
                .createdBy(1)
                .build();
        Design saved = designRepository.save(design);

        saved.setName("Дизайн після оновлення");
        saved.setPrice(150.00);
        saved.setAvailable(false);
        Design updated = designRepository.update(saved);

        assertEquals("Дизайн після оновлення", updated.getName());
        assertEquals(150.00, updated.getPrice(), 0.001);
        assertFalse(updated.isAvailable());
    }

    @Test
    @Order(6)
    @DisplayName("Видалення дизайну")
    void testDeleteDesign() {
        Category cat = categoryRepository.findAll().get(0);
        Design design = new Design.Builder()
                .name("Дизайн для видалення")
                .category(cat)
                .price(50.00)
                .available(true)
                .createdBy(1)
                .build();
        Design saved = designRepository.save(design);
        int id = saved.getId();

        boolean deleted = designRepository.deleteById(id);

        assertTrue(deleted, "deleteById повинен повернути true");
        Optional<Design> found = designRepository.findById(id);
        assertFalse(found.isPresent(), "Видалений дизайн не повинен бути знайдений");
    }

    @Test
    @Order(7)
    @DisplayName("Пошук дизайнів за назвою")
    void testSearchByName() {
        Category cat = categoryRepository.findAll().get(0);
        Design d1 = new Design.Builder().name("Гірський пейзаж").category(cat).price(200).available(true).createdBy(1).build();
        Design d2 = new Design.Builder().name("Морський захід").category(cat).price(250).available(true).createdBy(1).build();
        designRepository.save(d1);
        designRepository.save(d2);

        List<Design> results = designRepository.searchByName("Гірський");

        assertFalse(results.isEmpty(), "Результати пошуку не повинні бути порожніми");
        assertTrue(results.stream().anyMatch(d -> d.getName().contains("Гірський")));
    }

    @Test
    @Order(8)
    @DisplayName("Підрахунок дизайнів")
    void testCount() {
        int count = designRepository.count();

        assertTrue(count > 0, "Кількість дизайнів повинна бути більше 0");
    }

    @Test
    @Order(9)
    @DisplayName("Builder кидає виключення при порожній назві")
    void testBuilderValidation() {
        assertThrows(IllegalStateException.class, () ->
            new Design.Builder()
                .name("")
                .price(100)
                .build()
        );
    }

    @Test
    @Order(10)
    @DisplayName("Builder кидає виключення при від'ємній ціні")
    void testBuilderNegativePrice() {
        Category cat = categoryRepository.findAll().get(0);
        assertThrows(IllegalStateException.class, () ->
            new Design.Builder()
                .name("Тест")
                .category(cat)
                .price(-10)
                .build()
        );
    }
}