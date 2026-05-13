package com.phonecase.repository;

import com.phonecase.config.DatabaseConnection;
import com.phonecase.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Реалізація UserRepository через JDBC.
 */
@Singleton
public class UserRepositoryImpl implements UserRepository {

    private static final Logger logger = LoggerFactory.getLogger(UserRepositoryImpl.class);
    private final DatabaseConnection db;

    @Inject
    public UserRepositoryImpl(DatabaseConnection db) { this.db = db; }

    @Override
    public User save(User user) {
        final String sql = "INSERT INTO users (username, password, email, role) VALUES (?,?,?,?)";
        Connection conn = null;
        try {
            conn = db.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, user.getUsername());
                ps.setString(2, user.getPassword());
                ps.setString(3, user.getEmail());
                ps.setString(4, user.getRole() != null ? user.getRole() : "USER");
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) user.setId(keys.getInt(1));
                }
            }
            return user;
        } catch (SQLException e) {
            logger.error("Помилка збереження користувача: {}", e.getMessage(), e);
            throw new RuntimeException("Не вдалось зберегти користувача", e);
        } finally {
            db.releaseConnection(conn);
        }
    }

    @Override
    public Optional<User> findById(Integer id) {
        return findByField("SELECT * FROM users WHERE id=?", id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return findByField("SELECT * FROM users WHERE username=?", username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return findByField("SELECT * FROM users WHERE email=?", email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }

    @Override
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = db.getConnection();
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT * FROM users ORDER BY id")) {
                while (rs.next()) list.add(mapFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Помилка вибірки користувачів: {}", e.getMessage(), e);
        } finally {
            db.releaseConnection(conn);
        }
        return list;
    }

    @Override
    public User update(User user) {
        final String sql = "UPDATE users SET email=?, role=?, is_active=? WHERE id=?";
        Connection conn = null;
        try {
            conn = db.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, user.getEmail());
                ps.setString(2, user.getRole());
                ps.setInt(3, user.isActive() ? 1 : 0);
                ps.setInt(4, user.getId());
                ps.executeUpdate();
            }
            return user;
        } catch (SQLException e) {
            logger.error("Помилка оновлення користувача: {}", e.getMessage(), e);
            throw new RuntimeException("Не вдалось оновити користувача", e);
        } finally {
            db.releaseConnection(conn);
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        Connection conn = null;
        try {
            conn = db.getConnection();
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE id=?")) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            logger.error("Помилка видалення користувача: {}", e.getMessage(), e);
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
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users")) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) { return 0; }
        finally { db.releaseConnection(conn); }
    }

    private Optional<User> findByField(String sql, Object param) {
        Connection conn = null;
        try {
            conn = db.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setObject(1, param);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return Optional.of(mapFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Помилка пошуку користувача: {}", e.getMessage(), e);
        } finally {
            db.releaseConnection(conn);
        }
        return Optional.empty();
    }

    private User mapFromResultSet(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setEmail(rs.getString("email"));
        u.setRole(rs.getString("role"));
        u.setActive(rs.getInt("is_active") == 1);
        String ca = rs.getString("created_at");
        if (ca != null) {
            try { u.setCreatedAt(LocalDateTime.parse(ca.replace(" ", "T"))); }
            catch (Exception ignored) {}
        }
        return u;
    }
}