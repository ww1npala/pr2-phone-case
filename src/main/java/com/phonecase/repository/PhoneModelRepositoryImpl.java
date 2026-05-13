package com.phonecase.repository;

import com.phonecase.config.DatabaseConnection;
import com.phonecase.model.PhoneModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.*;
import java.util.*;

@Singleton
public class PhoneModelRepositoryImpl implements PhoneModelRepository {
    private static final Logger logger = LoggerFactory.getLogger(PhoneModelRepositoryImpl.class);
    private final DatabaseConnection db;

    @Inject
    public PhoneModelRepositoryImpl(DatabaseConnection db) { this.db = db; }

    @Override
    public PhoneModel save(PhoneModel pm) {
        Connection conn = null;
        try {
            conn = db.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO phone_models (brand,model_name,screen_size,release_year) VALUES (?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, pm.getBrand());
                ps.setString(2, pm.getModelName());
                ps.setObject(3, pm.getScreenSize());
                ps.setObject(4, pm.getReleaseYear());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) pm.setId(keys.getInt(1));
                }
            }
            return pm;
        } catch (SQLException e) {
            throw new RuntimeException("Помилка збереження моделі", e);
        } finally {
            db.releaseConnection(conn);
        }
    }

    @Override
    public Optional<PhoneModel> findById(Integer id) {
        return query("SELECT * FROM phone_models WHERE id=?", id).stream().findFirst();
    }

    @Override
    public List<PhoneModel> findByBrand(String brand) {
        return query("SELECT * FROM phone_models WHERE brand=? ORDER BY model_name", brand);
    }

    @Override
    public Optional<PhoneModel> findByBrandAndModel(String brand, String modelName) {
        return query("SELECT * FROM phone_models WHERE brand=? AND model_name=?", brand, modelName)
                .stream().findFirst();
    }

    @Override
    public List<PhoneModel> findAll() {
        return query("SELECT * FROM phone_models ORDER BY brand, model_name");
    }

    @Override
    public PhoneModel update(PhoneModel pm) {
        Connection conn = null;
        try {
            conn = db.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE phone_models SET brand=?,model_name=?,screen_size=?,release_year=? WHERE id=?")) {
                ps.setString(1, pm.getBrand());
                ps.setString(2, pm.getModelName());
                ps.setObject(3, pm.getScreenSize());
                ps.setObject(4, pm.getReleaseYear());
                ps.setInt(5, pm.getId());
                ps.executeUpdate();
            }
            return pm;
        } catch (SQLException e) { throw new RuntimeException("Помилка оновлення моделі", e); }
        finally { db.releaseConnection(conn); }
    }

    @Override
    public boolean deleteById(Integer id) {
        Connection conn = null;
        try {
            conn = db.getConnection();
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM phone_models WHERE id=?")) {
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
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM phone_models")) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) { return 0; }
        finally { db.releaseConnection(conn); }
    }

    private List<PhoneModel> query(String sql, Object... params) {
        List<PhoneModel> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = db.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) ps.setObject(i+1, params[i]);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(map(rs));
                }
            }
        } catch (SQLException e) { logger.error("query phone_models: {}", e.getMessage()); }
        finally { db.releaseConnection(conn); }
        return list;
    }

    private PhoneModel map(ResultSet rs) throws SQLException {
        PhoneModel pm = new PhoneModel();
        pm.setId(rs.getInt("id"));
        pm.setBrand(rs.getString("brand"));
        pm.setModelName(rs.getString("model_name"));
        double ss = rs.getDouble("screen_size");
        if (!rs.wasNull()) pm.setScreenSize(ss);
        int yr = rs.getInt("release_year");
        if (!rs.wasNull()) pm.setReleaseYear(yr);
        return pm;
    }
}