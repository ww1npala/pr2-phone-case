package com.phonecase.repository;

import com.phonecase.config.DatabaseConnection;
import com.phonecase.model.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.*;
import java.util.*;

@Singleton
public class CategoryRepositoryImpl implements CategoryRepository {
    private static final Logger logger = LoggerFactory.getLogger(CategoryRepositoryImpl.class);
    private final DatabaseConnection db;

    @Inject
    public CategoryRepositoryImpl(DatabaseConnection db) { this.db = db; }

    @Override
    public Category save(Category cat) {
        Connection conn = null;
        try {
            conn = db.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO categories (name, description) VALUES (?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, cat.getName());
                ps.setString(2, cat.getDescription());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) cat.setId(keys.getInt(1));
                }
            }
            return cat;
        } catch (SQLException e) {
            throw new RuntimeException("Помилка збереження категорії", e);
        } finally {
            db.releaseConnection(conn);
        }
    }

    @Override
    public Optional<Category> findById(Integer id) {
        Connection conn = null;
        try {
            conn = db.getConnection();
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM categories WHERE id=?")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return Optional.of(map(rs));
                }
            }
        } catch (SQLException e) { logger.error("findById category: {}", e.getMessage()); }
        finally { db.releaseConnection(conn); }
        return Optional.empty();
    }

    @Override
    public Optional<Category> findByName(String name) {
        Connection conn = null;
        try {
            conn = db.getConnection();
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM categories WHERE name=?")) {
                ps.setString(1, name);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return Optional.of(map(rs));
                }
            }
        } catch (SQLException e) { logger.error("findByName category: {}", e.getMessage()); }
        finally { db.releaseConnection(conn); }
        return Optional.empty();
    }

    @Override
    public List<Category> findAll() {
        List<Category> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = db.getConnection();
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT * FROM categories ORDER BY name")) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) { logger.error("findAll categories: {}", e.getMessage()); }
        finally { db.releaseConnection(conn); }
        return list;
    }

    @Override
    public Category update(Category cat) {
        Connection conn = null;
        try {
            conn = db.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE categories SET name=?, description=? WHERE id=?")) {
                ps.setString(1, cat.getName());
                ps.setString(2, cat.getDescription());
                ps.setInt(3, cat.getId());
                ps.executeUpdate();
            }
            return cat;
        } catch (SQLException e) { throw new RuntimeException("Помилка оновлення категорії", e); }
        finally { db.releaseConnection(conn); }
    }

    @Override
    public boolean deleteById(Integer id) {
        Connection conn = null;
        try {
            conn = db.getConnection();
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM categories WHERE id=?")) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) { return false; }
        finally { db.releaseConnection(conn); }
    }

    @Override
    public int count() {
        Connection conn = null;
        try {
            conn = db.getConnection();
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM categories")) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) { return 0; }
        finally { db.releaseConnection(conn); }
    }

    private Category map(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setId(rs.getInt("id"));
        c.setName(rs.getString("name"));
        c.setDescription(rs.getString("description"));
        return c;
    }
}