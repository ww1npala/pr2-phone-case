-- =============================================================
-- DDL-скрипти для бази даних PhoneCase Manager
-- Тема: Розробка модуля керування дизайнами чохлів для мобільних телефонів
-- Автор: Студент групи КН-3
-- Дата: 2026
-- СУБД: SQLite 3
-- =============================================================

-- Вмикаємо підтримку зовнішніх ключів
PRAGMA foreign_keys = ON;

-- =============================================================
-- Таблиця: users (Користувачі системи)
-- Нормальна форма: 3НФ
-- =============================================================
CREATE TABLE IF NOT EXISTS users (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,  -- Первинний ключ
    username    TEXT    NOT NULL UNIQUE,             -- Унікальне ім'я користувача
    password    TEXT    NOT NULL,                   -- Хеш пароля (BCrypt)
    email       TEXT    NOT NULL UNIQUE,             -- Унікальна email-адреса
    role        TEXT    NOT NULL DEFAULT 'USER'      -- Роль: USER, ADMIN, MANAGER
                        CHECK(role IN ('USER', 'ADMIN', 'MANAGER')),
    created_at  TEXT    NOT NULL DEFAULT (datetime('now')),  -- Дата реєстрації
    is_active   INTEGER NOT NULL DEFAULT 1          -- 1=активний, 0=заблокований
                        CHECK(is_active IN (0, 1))
);

-- =============================================================
-- Таблиця: categories (Категорії дизайнів)
-- Нормальна форма: 3НФ
-- =============================================================
CREATE TABLE IF NOT EXISTS categories (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        TEXT    NOT NULL UNIQUE,             -- Назва категорії (унікальна)
    description TEXT                                 -- Опис категорії
);

-- =============================================================
-- Таблиця: phone_models (Моделі мобільних телефонів)
-- Нормальна форма: 3НФ
-- =============================================================
CREATE TABLE IF NOT EXISTS phone_models (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    brand        TEXT    NOT NULL,                   -- Бренд (Apple, Samsung...)
    model_name   TEXT    NOT NULL,                   -- Назва моделі
    screen_size  REAL,                              -- Розмір екрану (дюйми)
    release_year INTEGER,                           -- Рік випуску
    UNIQUE (brand, model_name)                       -- Унікальна комбінація бренд+модель
);

-- =============================================================
-- Таблиця: designs (Дизайни чохлів) — ГОЛОВНА ТАБЛИЦЯ
-- Зв'язок 1:N з categories та users
-- Нормальна форма: 3НФ
-- =============================================================
CREATE TABLE IF NOT EXISTS designs (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    name         TEXT    NOT NULL,                   -- Назва дизайну
    description  TEXT,                              -- Детальний опис
    category_id  INTEGER NOT NULL,                   -- FK -> categories
    image_path   TEXT,                              -- Шлях до зображення
    price        REAL    NOT NULL DEFAULT 0.0        -- Ціна в гривнях
                         CHECK(price >= 0),
    is_available INTEGER NOT NULL DEFAULT 1          -- Доступність для замовлення
                         CHECK(is_available IN (0, 1)),
    created_by   INTEGER NOT NULL,                   -- FK -> users (автор)
    created_at   TEXT    NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
    FOREIGN KEY (created_by)  REFERENCES users(id)      ON DELETE RESTRICT
);

-- =============================================================
-- Таблиця: design_compatibility (Сумісність дизайн-телефон)
-- Проміжна таблиця для зв'язку N:M між designs та phone_models
-- =============================================================
CREATE TABLE IF NOT EXISTS design_compatibility (
    design_id      INTEGER NOT NULL,
    phone_model_id INTEGER NOT NULL,
    PRIMARY KEY (design_id, phone_model_id),         -- Складний первинний ключ
    FOREIGN KEY (design_id)      REFERENCES designs(id)      ON DELETE CASCADE,
    FOREIGN KEY (phone_model_id) REFERENCES phone_models(id) ON DELETE CASCADE
);

-- =============================================================
-- Таблиця: orders (Замовлення чохлів)
-- Зв'язок N:1 з users, designs, phone_models
-- Нормальна форма: 3НФ
-- =============================================================
CREATE TABLE IF NOT EXISTS orders (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id        INTEGER NOT NULL,                 -- FK -> users
    design_id      INTEGER NOT NULL,                 -- FK -> designs
    phone_model_id INTEGER NOT NULL,                 -- FK -> phone_models
    quantity       INTEGER NOT NULL DEFAULT 1
                           CHECK(quantity > 0),      -- Кількість > 0
    total_price    REAL    NOT NULL
                           CHECK(total_price >= 0),  -- Загальна сума
    status         TEXT    NOT NULL DEFAULT 'PENDING'
                           CHECK(status IN ('PENDING', 'COMPLETED', 'CANCELLED')),
    order_date     TEXT    NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (user_id)        REFERENCES users(id)        ON DELETE RESTRICT,
    FOREIGN KEY (design_id)      REFERENCES designs(id)      ON DELETE RESTRICT,
    FOREIGN KEY (phone_model_id) REFERENCES phone_models(id) ON DELETE RESTRICT
);

-- =============================================================
-- ІНДЕКСИ для оптимізації запитів
-- =============================================================
CREATE INDEX IF NOT EXISTS idx_designs_category    ON designs(category_id);
CREATE INDEX IF NOT EXISTS idx_designs_created_by  ON designs(created_by);
CREATE INDEX IF NOT EXISTS idx_designs_available   ON designs(is_available);
CREATE INDEX IF NOT EXISTS idx_orders_user         ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_status       ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_design       ON orders(design_id);
CREATE INDEX IF NOT EXISTS idx_phone_models_brand  ON phone_models(brand);

-- =============================================================
-- ТРИГЕРИ для автоматизації
-- =============================================================

-- Тригер: перевірка що дизайн доступний перед створенням замовлення
CREATE TRIGGER IF NOT EXISTS trg_check_design_available
BEFORE INSERT ON orders
BEGIN
    SELECT CASE
        WHEN (SELECT is_available FROM designs WHERE id = NEW.design_id) = 0
        THEN RAISE(ABORT, 'Дизайн недоступний для замовлення')
    END;
END;

-- Тригер: оновлення total_price при зміні кількості
CREATE TRIGGER IF NOT EXISTS trg_update_total_price
BEFORE UPDATE OF quantity ON orders
BEGIN
    SELECT CASE
        WHEN NEW.quantity <= 0
        THEN RAISE(ABORT, 'Кількість повинна бути більше 0')
    END;
END;

-- =============================================================
-- ПРЕДСТАВЛЕННЯ (VIEWS — Віртуальні таблиці)
-- =============================================================

-- VIEW: Повна інформація про дизайн (JOIN з categories та users)
CREATE VIEW IF NOT EXISTS v_design_details AS
    SELECT
        d.id,
        d.name             AS design_name,
        d.description,
        d.price,
        d.is_available,
        c.name             AS category_name,
        u.username         AS created_by_username,
        d.created_at
    FROM designs d
    JOIN categories c ON d.category_id = c.id
    JOIN users u      ON d.created_by  = u.id;

-- VIEW: Статистика замовлень (JOIN кількох таблиць)
CREATE VIEW IF NOT EXISTS v_order_stats AS
    SELECT
        o.id              AS order_id,
        u.username,
        d.name            AS design_name,
        c.name            AS category_name,
        pm.brand || ' ' || pm.model_name AS phone_model,
        o.quantity,
        o.total_price,
        o.status,
        o.order_date
    FROM orders o
    JOIN users u         ON o.user_id        = u.id
    JOIN designs d       ON o.design_id      = d.id
    JOIN categories c    ON d.category_id    = c.id
    JOIN phone_models pm ON o.phone_model_id = pm.id;

-- VIEW: Популярні дизайни (кількість замовлень)
CREATE VIEW IF NOT EXISTS v_popular_designs AS
    SELECT
        d.id,
        d.name,
        c.name  AS category,
        d.price,
        COUNT(o.id)       AS order_count,
        SUM(o.total_price) AS total_revenue
    FROM designs d
    LEFT JOIN orders o   ON d.id          = o.design_id
    JOIN categories c    ON d.category_id = c.id
    GROUP BY d.id, d.name, c.name, d.price;
