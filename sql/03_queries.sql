SELECT
    d.id,
    d.name          AS design_name,
    d.description,
    d.price,
    c.name          AS category_name,
    u.username      AS created_by
FROM designs d
JOIN categories c ON d.category_id = c.id
JOIN users u      ON d.created_by  = u.id
WHERE d.is_available = 1
ORDER BY d.price DESC;

-- 1.2 Пошук дизайнів за частковою назвою (LIKE)
SELECT d.name, c.name AS category, d.price
FROM designs d
JOIN categories c ON d.category_id = c.id
WHERE LOWER(d.name) LIKE LOWER('%гірськ%')
ORDER BY d.name;

-- 1.3 Дизайни у ціновому діапазоні
SELECT d.name, d.price, c.name AS category
FROM designs d
JOIN categories c ON d.category_id = c.id
WHERE d.price BETWEEN 200 AND 400
ORDER BY d.price;

-- 1.4 Кількість дизайнів у кожній категорії (GROUP BY + COUNT)
SELECT
    c.name     AS category_name,
    COUNT(d.id) AS design_count,
    MIN(d.price) AS min_price,
    MAX(d.price) AS max_price,
    AVG(d.price) AS avg_price
FROM categories c
LEFT JOIN designs d ON c.id = d.category_id
GROUP BY c.id, c.name
ORDER BY design_count DESC;

-- =============================================================
-- 2. ЗАПИТИ ДО ЗАМОВЛЕНЬ
-- =============================================================

-- 2.1 Всі замовлення з деталями (з контактними даними)
SELECT
    o.id               AS order_id,
    u.username         AS client,
    o.phone_number,
    o.address,
    d.name             AS design_name,
    c.name             AS category,
    pm.brand || ' ' || pm.model_name AS phone,
    o.quantity,
    o.total_price,
    o.status,
    o.order_date
FROM orders o
JOIN users u         ON o.user_id        = u.id
JOIN designs d       ON o.design_id      = d.id
JOIN categories c    ON d.category_id    = c.id
JOIN phone_models pm ON o.phone_model_id = pm.id
ORDER BY o.order_date DESC;

-- 2.2 Замовлення конкретного користувача
SELECT
    o.id, d.name AS design_name,
    pm.brand || ' ' || pm.model_name AS phone,
    o.quantity, o.total_price, o.status, o.order_date
FROM orders o
JOIN designs d       ON o.design_id      = d.id
JOIN phone_models pm ON o.phone_model_id = pm.id
WHERE o.user_id = 2
ORDER BY o.order_date DESC;

-- 2.3 Замовлення за статусом + підрахунок
SELECT
    status,
    COUNT(*) AS count,
    SUM(total_price) AS total_sum
FROM orders
GROUP BY status;

-- 2.4 Виручка за виконаними замовленнями
SELECT
    COUNT(*) AS completed_orders,
    SUM(total_price) AS total_revenue,
    AVG(total_price) AS avg_order_value
FROM orders
WHERE status = 'COMPLETED';

-- =============================================================
-- 3. АНАЛІТИЧНІ ЗАПИТИ (HAVING, підзапити)
-- =============================================================

-- 3.1 Найпопулярніші дизайни (більше 1 замовлення)
SELECT
    d.name,
    COUNT(o.id)       AS order_count,
    SUM(o.total_price) AS total_revenue
FROM designs d
LEFT JOIN orders o ON d.id = o.design_id
GROUP BY d.id, d.name
HAVING COUNT(o.id) >= 1
ORDER BY order_count DESC, total_revenue DESC
LIMIT 10;

-- 3.2 Клієнти з найбільшою сумою замовлень
SELECT
    u.username,
    u.email,
    COUNT(o.id)       AS order_count,
    SUM(o.total_price) AS total_spent
FROM users u
JOIN orders o ON u.id = o.user_id
WHERE o.status = 'COMPLETED'
GROUP BY u.id, u.username, u.email
ORDER BY total_spent DESC;

-- 3.3 Дизайни без жодного замовлення (LEFT JOIN + IS NULL)
SELECT d.name, c.name AS category, d.price
FROM designs d
JOIN categories c ON d.category_id = c.id
LEFT JOIN orders o ON d.id = o.design_id
WHERE o.id IS NULL
ORDER BY d.name;

-- 3.4 Найпопулярніші бренди телефонів у замовленнях
SELECT
    pm.brand,
    COUNT(o.id)        AS order_count,
    SUM(o.total_price) AS revenue
FROM phone_models pm
JOIN orders o ON pm.id = o.phone_model_id
GROUP BY pm.brand
ORDER BY order_count DESC;

-- =============================================================
-- 4. ПІДЗАПИТИ (SUBQUERIES)
-- =============================================================

-- 4.1 Дизайни дорожчі за середню ціну
SELECT name, price, (SELECT AVG(price) FROM designs) AS avg_price
FROM designs
WHERE price > (SELECT AVG(price) FROM designs)
ORDER BY price DESC;

-- 4.2 Категорії з хоча б 2 дизайнами
SELECT name
FROM categories
WHERE id IN (
    SELECT category_id
    FROM designs
    GROUP BY category_id
    HAVING COUNT(*) >= 2
);

-- 4.3 Корельований підзапит: дизайни в категорії "Природа"
SELECT name, price
FROM designs
WHERE category_id = (
    SELECT id FROM categories WHERE name = 'Природа'
)
ORDER BY price;

-- =============================================================
-- 5. РОБОТА З VIEWS
-- =============================================================

-- 5.1 Використання v_design_details
SELECT * FROM v_design_details WHERE is_available = 1;

-- 5.2 Використання v_order_stats
SELECT * FROM v_order_stats
WHERE status = 'PENDING'
ORDER BY order_date;

-- 5.3 Використання v_popular_designs
SELECT * FROM v_popular_designs
ORDER BY order_count DESC
LIMIT 5;

-- =============================================================
-- 6. DML-ОПЕРАЦІЇ (UPDATE, DELETE)
-- =============================================================

-- 6.1 Оновлення статусу замовлення
UPDATE orders
SET status = 'COMPLETED'
WHERE id = 3;

-- 6.2 Деактивація дизайну
UPDATE designs
SET is_available = 0
WHERE id = 15;

-- 6.3 Зміна ціни всіх дизайнів категорії на 10%
UPDATE designs
SET price = price * 1.10
WHERE category_id = (SELECT id FROM categories WHERE name = 'Аніме');

-- 6.4 Видалення скасованих замовлень старше 30 днів
DELETE FROM orders
WHERE status = 'CANCELLED'
  AND order_date < datetime('now', '-30 days');

-- =============================================================
-- 7. ПЕРЕВІРОЧНІ ЗАПИТИ ДЛЯ ТЕСТУВАННЯ
-- =============================================================

-- Перевірка зовнішніх ключів
SELECT COUNT(*) AS orphan_designs
FROM designs
WHERE category_id NOT IN (SELECT id FROM categories);

-- Перевірка унікальності email
SELECT email, COUNT(*) AS cnt
FROM users
GROUP BY email
HAVING cnt > 1;

-- Статистика по таблицях
SELECT 'users' AS tbl, COUNT(*) AS cnt FROM users UNION ALL
SELECT 'categories', COUNT(*) FROM categories UNION ALL
SELECT 'phone_models', COUNT(*) FROM phone_models UNION ALL
SELECT 'designs', COUNT(*) FROM designs UNION ALL
SELECT 'design_compatibility', COUNT(*) FROM design_compatibility UNION ALL
SELECT 'orders', COUNT(*) FROM orders;
