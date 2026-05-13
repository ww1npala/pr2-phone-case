package com.phonecase.repository;

import com.phonecase.config.DatabaseConnection;
import com.phonecase.model.Category;
import com.phonecase.model.Design;
import com.phonecase.model.PhoneModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Реалізація репозиторію дизайнів через JDBC.
 * Реалізує патерни: Data Mapper, Repository, Lazy Initialization.
 */

@Singleton
public class DesignRepositoryImpl implements DesignRepository {

    private static final Logger logger = LoggerFactory.getLogger(DesignRepositoryImpl.class);
    private final DatabaseConnection db;

    @Inject
    public DesignRepositoryImpl(DatabaseConnection db) {
        this.db = db;
    }

    @Override
    public Design save(Design design) {
        final String sql = """
            INSERT INTO designs (name, description, category_id, image_path, price, is_available, created_by)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        Connection conn = null;
        try {
            conn = db.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                mapToStatement(ps, design);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        design.setId(keys.getInt(1));
                    }
                }
            }

            if (design.getCompatibleModels() != null) {
                for (PhoneModel pm : design.getCompatibleModels()) {
                    addCompatibility(design.getId(), pm.getId());
                }
            }
          logger.debug("Дизайн збережено: id={}, name={}", design.getId(), design.getName());
          return findById(design.getId()).orElse(design);
        } catch (SQLException e) {
            logger.error("Помилка збереження дизайну: {}", e.getMessage(), e);
            throw new RuntimeException("Не вдалось зберегти дизайн", e);
        } finally {
            db.releaseConnection(conn);
        }
    }

    @Override
    public Optional<Design> findById(Integer id) {
        final String sql = """
            SELECT d.*, c.name AS cat_name, c.description AS cat_desc
            FROM designs d JOIN categories c ON d.category_id = c.id
            WHERE d.id = ?
        """;
        Connection conn = null;
        try {
            conn = db.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Design d = mapFromResultSet(rs);
                        // Lazy Initialization: завантажуємо сумісні моделі за потребою
                        d.setCompatibleModels(loadCompatibleModels(conn, id));
                        return Optional.of(d);
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Помилка пошуку дизайну за id={}: {}", id, e.getMessage(), e);
        } finally {
            db.releaseConnection(conn);
        }
        return Optional.empty();
    }

    @Override
    public List<Design> findAll() {
        return executeListQuery("""
            SELECT d.*, c.name AS cat_name, c.description AS cat_desc
            FROM designs d JOIN categories c ON d.category_id = c.id
            ORDER BY d.id DESC
        """);
    }

    @Override
    public List<Design> findAvailable() {
        return executeListQuery("""
            SELECT d.*, c.name AS cat_name, c.description AS cat_desc
            FROM designs d JOIN categories c ON d.category_id = c.id
            WHERE d.is_available = 1
            ORDER BY d.id DESC
        """);
    }

    @Override
    public List<Design> findByCategoryId(int categoryId) {
        final String sql = """
            SELECT d.*, c.name AS cat_name, c.description AS cat_desc
            FROM designs d JOIN categories c ON d.category_id = c.id
            WHERE d.category_id = ?
            ORDER BY d.name
        """;
        return executeListQuery(sql, categoryId);
    }

    @Override
    public List<Design> searchByName(String query) {
        final String sql = """
            SELECT d.*, c.name AS cat_name, c.description AS cat_desc
            FROM designs d JOIN categories c ON d.category_id = c.id
            WHERE LOWER(d.name) LIKE LOWER(?)
            ORDER BY d.name
        """;
        return executeListQuery(sql, "%" + query + "%");
    }

    @Override
    public List<Design> findByPhoneModelId(int phoneModelId) {
        final String sql = """
            SELECT d.*, c.name AS cat_name, c.description AS cat_desc
            FROM designs d
            JOIN categories c ON d.category_id = c.id
            JOIN design_compatibility dc ON d.id = dc.design_id
            WHERE dc.phone_model_id = ?
            ORDER BY d.name
        """;
        return executeListQuery(sql, phoneModelId);
    }

    @Override
    public List<Design> findByCreatedBy(int userId) {
        final String sql = """
            SELECT d.*, c.name AS cat_name, c.description AS cat_desc
            FROM designs d JOIN categories c ON d.category_id = c.id
            WHERE d.created_by = ?
            ORDER BY d.created_at DESC
        """;
        return executeListQuery(sql, userId);
    }

    @Override
    public Design update(Design design) {
        final String sql = """
            UPDATE designs SET name=?, description=?, category_id=?, image_path=?,
                               price=?, is_available=?
            WHERE id=?
        """;
        Connection conn = null;
        try {
            conn = db.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, design.getName());
                ps.setString(2, design.getDescription());
                ps.setInt(3, design.getCategory().getId());
                ps.setString(4, design.getImagePath());
                ps.setDouble(5, design.getPrice());
                ps.setInt(6, design.isAvailable() ? 1 : 0);
                ps.setInt(7, design.getId());
                ps.executeUpdate();
            }
            logger.debug("Дизайн оновлено: id={}", design.getId());
            return design;
        } catch (SQLException e) {
            logger.error("Помилка оновлення дизайну: {}", e.getMessage(), e);
            throw new RuntimeException("Не вдалось оновити дизайн", e);
        } finally {
            db.releaseConnection(conn);
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        Connection conn = null;
        try {
            conn = db.getConnection();
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM designs WHERE id=?")) {
                ps.setInt(1, id);
                int affected = ps.executeUpdate();
                logger.debug("Дизайн видалено: id={}, affected={}", id, affected);
                return affected > 0;
            }
        } catch (SQLException e) {
            logger.error("Помилка видалення дизайну: {}", e.getMessage(), e);
            return false;
        } finally {
            db.releaseConnection(conn);
        }
    }

    @Override
    public int count() {
        Connection conn = null;
        try {
            conn = db.getConnection();
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM designs")) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            logger.error("Помилка підрахунку дизайнів: {}", e.getMessage());
            return 0;
        } finally {
            db.releaseConnection(conn);
        }
    }

    @Override
    public void addCompatibility(int designId, int phoneModelId) {
        final String sql = "INSERT OR IGNORE INTO design_compatibility (design_id, phone_model_id) VALUES (?,?)";
        Connection conn = null;
        try {
            conn = db.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, designId);
                ps.setInt(2, phoneModelId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            logger.error("Помилка додавання сумісності: {}", e.getMessage(), e);
        } finally {
            db.releaseConnection(conn);
        }
    }

    @Override
    public void removeAllCompatibilities(int designId) {
        Connection conn = null;
        try {
            conn = db.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM design_compatibility WHERE design_id=?")) {
                ps.setInt(1, designId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            logger.error("Помилка видалення сумісностей: {}", e.getMessage(), e);
        } finally {
            db.releaseConnection(conn);
        }
    }

    private Design mapFromResultSet(ResultSet rs) throws SQLException {
        Design d = new Design();
        d.setId(rs.getInt("id"));
        d.setName(rs.getString("name"));
        d.setDescription(rs.getString("description"));
        d.setImagePath(rs.getString("image_path"));
        d.setPrice(rs.getDouble("price"));
        d.setAvailable(rs.getInt("is_available") == 1);
        d.setCreatedBy(rs.getInt("created_by"));

        String createdAt = rs.getString("created_at");
        if (createdAt != null) {
            try { d.setCreatedAt(LocalDateTime.parse(createdAt.replace(" ", "T"))); }
            catch (Exception ignored) {}
        }

        Category cat = new Category();
        cat.setId(rs.getInt("category_id"));
        cat.setName(rs.getString("cat_name"));
        cat.setDescription(rs.getString("cat_desc"));
        d.setCategory(cat);

        return d;
    }

    private void mapToStatement(PreparedStatement ps, Design d) throws SQLException {
        ps.setString(1, d.getName());
        ps.setString(2, d.getDescription());
        ps.setInt(3, d.getCategory().getId());
        ps.setString(4, d.getImagePath());
        ps.setDouble(5, d.getPrice());
        ps.setInt(6, d.isAvailable() ? 1 : 0);
        ps.setInt(7, d.getCreatedBy() != null ? d.getCreatedBy() : 1);
    }

    private List<PhoneModel> loadCompatibleModels(Connection conn, int designId) throws SQLException {
        final String sql = """
            SELECT pm.* FROM phone_models pm
            JOIN design_compatibility dc ON pm.id = dc.phone_model_id
            WHERE dc.design_id = ?
        """;
        List<PhoneModel> models = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, designId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PhoneModel pm = new PhoneModel();
                    pm.setId(rs.getInt("id"));
                    pm.setBrand(rs.getString("brand"));
                    pm.setModelName(rs.getString("model_name"));
                    pm.setScreenSize(rs.getDouble("screen_size"));
                    pm.setReleaseYear(rs.getInt("release_year"));
                    models.add(pm);
                }
            }
        }
        return models;
    }

    private List<Design> executeListQuery(String sql, Object... params) {
        List<Design> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = db.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapFromResultSet(rs));
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Помилка виконання запиту: {}", e.getMessage(), e);
        } finally {
            db.releaseConnection(conn);
        }
        return list;
    }
}