package com.phonecase.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Ініціалізує схему бази даних та наповнює тестовими даними.
 * DDL та DML виконуються динамічно при першому запуску.
 */
@Singleton
public class DatabaseInitializer {

  private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);
  private final DatabaseConnection dbConnection;

  @Inject
  public DatabaseInitializer(DatabaseConnection dbConnection) {
    this.dbConnection = dbConnection;
  }

  public void initialize() {
    logger.info("Ініціалізація схеми бази даних...");
    Connection conn = null;
    try {
      conn = dbConnection.getConnection();
      conn.setAutoCommit(false);

      createTables(conn);
      seedData(conn);

      conn.commit();
      logger.info("Схема та початкові дані успішно ініціалізовано.");
    } catch (SQLException e) {
      logger.error("Помилка ініціалізації БД: {}", e.getMessage(), e);
      if (conn != null) {
        try { conn.rollback(); } catch (SQLException ex) { logger.error("Rollback failed", ex); }
      }
    } finally {
      dbConnection.releaseConnection(conn);
    }
  }

  private void createTables(Connection conn) throws SQLException {
    try (Statement stmt = conn.createStatement()) {
      stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id          INTEGER PRIMARY KEY AUTOINCREMENT,
                    username    TEXT    NOT NULL UNIQUE,
                    password    TEXT    NOT NULL,
                    email       TEXT    NOT NULL UNIQUE,
                    role        TEXT    NOT NULL DEFAULT 'USER',
                    created_at  TEXT    NOT NULL DEFAULT (datetime('now')),
                    is_active   INTEGER NOT NULL DEFAULT 1
                )
            """);

      stmt.execute("""
                CREATE TABLE IF NOT EXISTS categories (
                    id          INTEGER PRIMARY KEY AUTOINCREMENT,
                    name        TEXT    NOT NULL UNIQUE,
                    description TEXT
                )
            """);

      stmt.execute("""
                CREATE TABLE IF NOT EXISTS phone_models (
                    id           INTEGER PRIMARY KEY AUTOINCREMENT,
                    brand        TEXT    NOT NULL,
                    model_name   TEXT    NOT NULL,
                    screen_size  REAL,
                    release_year INTEGER,
                    UNIQUE (brand, model_name)
                )
            """);

      stmt.execute("""
                CREATE TABLE IF NOT EXISTS designs (
                    id           INTEGER PRIMARY KEY AUTOINCREMENT,
                    name         TEXT    NOT NULL,
                    description  TEXT,
                    category_id  INTEGER NOT NULL,
                    image_path   TEXT,
                    price        REAL    NOT NULL DEFAULT 0.0,
                    is_available INTEGER NOT NULL DEFAULT 1,
                    created_by   INTEGER NOT NULL,
                    created_at   TEXT    NOT NULL DEFAULT (datetime('now')),
                    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
                    FOREIGN KEY (created_by)  REFERENCES users(id)      ON DELETE RESTRICT
                )
            """);

      stmt.execute("""
                CREATE TABLE IF NOT EXISTS design_compatibility (
                    design_id      INTEGER NOT NULL,
                    phone_model_id INTEGER NOT NULL,
                    PRIMARY KEY (design_id, phone_model_id),
                    FOREIGN KEY (design_id)      REFERENCES designs(id)      ON DELETE CASCADE,
                    FOREIGN KEY (phone_model_id) REFERENCES phone_models(id) ON DELETE CASCADE
                )
            """);

      stmt.execute("""
                CREATE TABLE IF NOT EXISTS orders (
                    id             INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id        INTEGER NOT NULL,
                    design_id      INTEGER NOT NULL,
                    phone_model_id INTEGER NOT NULL,
                    quantity       INTEGER NOT NULL DEFAULT 1,
                    total_price    REAL    NOT NULL,
                    status         TEXT    NOT NULL DEFAULT 'PENDING',
                    phone_number   TEXT,
                    address        TEXT,
                    order_date     TEXT    NOT NULL DEFAULT (datetime('now')),
                    FOREIGN KEY (user_id)        REFERENCES users(id)        ON DELETE RESTRICT,
                    FOREIGN KEY (design_id)      REFERENCES designs(id)      ON DELETE RESTRICT,
                    FOREIGN KEY (phone_model_id) REFERENCES phone_models(id) ON DELETE RESTRICT
                )
            """);

      stmt.execute("CREATE INDEX IF NOT EXISTS idx_designs_category ON designs(category_id)");
      stmt.execute("CREATE INDEX IF NOT EXISTS idx_designs_created_by ON designs(created_by)");
      stmt.execute("CREATE INDEX IF NOT EXISTS idx_orders_user ON orders(user_id)");
      stmt.execute("CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status)");
      stmt.execute("""
                CREATE VIEW IF NOT EXISTS v_design_details AS
                    SELECT d.id, d.name, d.description, d.price, d.is_available,
                           c.name AS category_name,
                           u.username AS created_by_username,
                           d.created_at
                    FROM designs d
                    JOIN categories c ON d.category_id = c.id
                    JOIN users u      ON d.created_by  = u.id
            """);

      stmt.execute("""
                CREATE VIEW IF NOT EXISTS v_order_stats AS
                    SELECT o.id, u.username, d.name AS design_name,
                           pm.brand || ' ' || pm.model_name AS phone_model,
                           o.quantity, o.total_price, o.status, o.order_date
                    FROM orders o
                    JOIN users u         ON o.user_id        = u.id
                    JOIN designs d       ON o.design_id      = d.id
                    JOIN phone_models pm ON o.phone_model_id = pm.id
            """);

      logger.info("DDL виконано успішно.");
    }
  }

  private void seedData(Connection conn) throws SQLException {
    try (Statement stmt = conn.createStatement()) {
      var rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
      rs.next();
      if (rs.getInt(1) > 0) {
        logger.info("Тестові дані вже існують — пропускаємо seed.");
        return;
      }

      stmt.execute("""
                INSERT INTO users (username, password, email, role) VALUES
                    ('admin',  '$2a$10$admin_hash_placeholder',   'admin@phonecase.ua',  'ADMIN'),
                    ('ivan',   '$2a$10$user1_hash_placeholder',   'ivan@gmail.com',      'USER'),
                    ('olena',  '$2a$10$user2_hash_placeholder',   'olena@gmail.com',     'USER'),
                    ('mykola', '$2a$10$user3_hash_placeholder',   'mykola@gmail.com',    'USER'),
                    ('daryna', '$2a$10$user4_hash_placeholder',   'daryna@gmail.com',    'USER'),
                    ('petro',  '$2a$10$user5_hash_placeholder',   'petro@gmail.com',     'USER'),
                    ('anna',   '$2a$10$user6_hash_placeholder',   'anna@gmail.com',      'USER'),
                    ('serhiy', '$2a$10$user7_hash_placeholder',   'serhiy@gmail.com',    'MANAGER'),
                    ('yulia',  '$2a$10$user8_hash_placeholder',   'yulia@gmail.com',     'USER'),
                    ('taras',  '$2a$10$user9_hash_placeholder',   'taras@gmail.com',     'USER')
            """);

      stmt.execute("""
                INSERT INTO categories (name, description) VALUES
                    ('Природа',    'Дизайни з природними мотивами'),
                    ('Абстракція', 'Абстрактні геометричні дизайни'),
                    ('Аніме',      'Персонажі та сцени з аніме'),
                    ('Спорт',      'Спортивна тематика'),
                    ('Мінімалізм', 'Мінімалістичні чисті дизайни'),
                    ('Фентезі',    'Фантастичні та казкові сюжети'),
                    ('Міська',     'Вулична та міська естетика'),
                    ('Фото',       'Реалістичні фотографічні принти')
            """);

      stmt.execute("""
                INSERT INTO phone_models (brand, model_name, screen_size, release_year) VALUES
                    ('Apple',   'iPhone 15 Pro',    6.1, 2023),
                    ('Apple',   'iPhone 15',        6.1, 2023),
                    ('Apple',   'iPhone 14',        6.1, 2022),
                    ('Samsung', 'Galaxy S24 Ultra', 6.8, 2024),
                    ('Samsung', 'Galaxy S24',       6.2, 2024),
                    ('Samsung', 'Galaxy A54',       6.4, 2023),
                    ('Xiaomi',  'Redmi Note 13',    6.67,2023),
                    ('Xiaomi',  'POCO X6',          6.67,2024),
                    ('Google',  'Pixel 8 Pro',      6.7, 2023),
                    ('Google',  'Pixel 8',          6.2, 2023),
                    ('OnePlus', '12',               6.82,2024),
                    ('Huawei',  'P60 Pro',          6.67,2023)
            """);

      stmt.execute("""
                INSERT INTO designs (name, description, category_id, price, is_available, created_by) VALUES
                    ('Горний захід',    'Пейзаж гірського заходу сонця',       1, 249.99, 1, 1),
                    ('Морські хвилі',  'Динамічний дизайн океанських хвиль',  1, 199.99, 1, 1),
                    ('Цифровий ліс',   'Абстрактний ліс у цифровому стилі',   2, 279.99, 1, 8),
                    ('Неонова геометрія','Яскрава неонова геометрія',          2, 299.99, 1, 8),
                    ('Сакура',         'Японська сакура в цвіту',             3, 349.99, 1, 1),
                    ('Naruto Uzumaki', 'Улюблений персонаж Naruto',           3, 399.99, 1, 1),
                    ('Баскетбол',      'Динамічний баскетбольний принт',      4, 219.99, 1, 8),
                    ('Футбол',         'Футбольний м'яч та поле',             4, 219.99, 1, 8),
                    ('Чорний мінімал', 'Чорний з тонкою білою лінією',        5, 149.99, 1, 1),
                    ('Білий простір',  'Максимально чистий білий дизайн',     5, 139.99, 1, 1),
                    ('Дракон',         'Дракон у стилі фентезі',              6, 449.99, 1, 8),
                    ('Ельфійський ліс','Казковий ельфійський ліс',            6, 399.99, 1, 8),
                    ('Графіті',        'Вулична графіті-культура',            7, 329.99, 1, 1),
                    ('Кіберпанк',      'Кіберпанк міський пейзаж',            7, 379.99, 1, 1),
                    ('Гірські озера',  'Фото гірських озер Карпат',          8, 289.99, 0, 1)
            """);

      stmt.execute("""
                INSERT INTO design_compatibility (design_id, phone_model_id) VALUES
                    (1,1),(1,2),(1,3),(1,4),(1,5),
                    (2,1),(2,2),(2,4),(2,5),(2,9),
                    (3,1),(3,4),(3,7),(3,8),(3,9),
                    (4,4),(4,5),(4,9),(4,10),(4,11),
                    (5,1),(5,2),(5,3),(5,6),(5,7),
                    (6,1),(6,2),(6,4),(6,5),(6,7),
                    (7,4),(7,5),(7,6),(7,9),(7,10),
                    (8,1),(8,4),(8,9),(8,11),(8,12),
                    (9,1),(9,2),(9,3),(9,4),(9,5),(9,6),(9,7),(9,8),(9,9),(9,10),
                    (10,1),(10,2),(10,3),(10,4),(10,5),
                    (11,4),(11,5),(11,9),(11,10),(11,11),
                    (12,1),(12,4),(12,9),(12,11),(12,12),
                    (13,4),(13,5),(13,6),(13,7),(13,8),
                    (14,4),(14,5),(14,9),(14,10),(14,11),
                    (15,1),(15,2),(15,3)
            """);

      stmt.execute("""
                INSERT INTO orders (user_id, design_id, phone_model_id, quantity, total_price, status) VALUES
                    (2,  1, 1, 1, 249.99, 'COMPLETED'),
                    (2,  5, 2, 2, 699.98, 'COMPLETED'),
                    (3,  4, 4, 1, 299.99, 'PENDING'),
                    (3,  6, 5, 1, 399.99, 'COMPLETED'),
                    (4,  9, 3, 3, 449.97, 'PENDING'),
                    (4, 11, 4, 1, 449.99, 'CANCELLED'),
                    (5,  2, 9, 1, 199.99, 'COMPLETED'),
                    (5,  7, 5, 2, 439.98, 'PENDING'),
                    (6, 13, 7, 1, 329.99, 'COMPLETED'),
                    (6, 14, 5, 1, 379.99, 'PENDING'),
                    (7,  3, 7, 1, 279.99, 'COMPLETED'),
                    (7,  8, 4, 2, 439.98, 'COMPLETED'),
                    (8, 12, 4, 1, 399.99, 'COMPLETED'),
                    (9, 10, 2, 1, 139.99, 'PENDING'),
                    (10, 6, 7, 1, 399.99, 'COMPLETED'),
                    (2,  9, 4, 1, 149.99, 'COMPLETED'),
                    (3, 15, 1, 1, 289.99, 'CANCELLED'),
                    (5, 11, 9, 1, 449.99, 'PENDING'),
                    (7,  4, 9, 2, 599.98, 'COMPLETED'),
                    (9,  5, 1, 1, 349.99, 'COMPLETED')
            """);

      logger.info("DML: тестові дані вставлено (COMMIT буде виконано зовні).");
    }
  }
}