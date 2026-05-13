package com.phonecase.repository;

import com.phonecase.config.DatabaseConnection;
import com.phonecase.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

@Singleton
public class OrderRepositoryImpl implements OrderRepository {
    private static final Logger logger = LoggerFactory.getLogger(OrderRepositoryImpl.class);
    private final DatabaseConnection db;

    @Inject
    public OrderRepositoryImpl(DatabaseConnection db) { this.db = db; }

  @Override
  public Order save(Order order) {
    final String sql = """
        INSERT INTO orders (user_id, design_id, phone_model_id, quantity, total_price, status)
        VALUES (?,?,?,?,?,?)
    """;
    Connection conn = null;
    try {
      conn = db.getConnection();
      try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, order.getUserId());
        ps.setInt(2, order.getDesignId());
        ps.setInt(3, order.getPhoneModelId());
        ps.setInt(4, order.getQuantity());
        ps.setDouble(5, order.getTotalPrice());
        ps.setString(6, order.getStatus().name());
        ps.executeUpdate();
      }
      try (Statement st = conn.createStatement();
          ResultSet rs = st.executeQuery("SELECT last_insert_rowid()")) {
        if (rs.next()) order.setId(rs.getInt(1));
      }
      return order;
    } catch (SQLException e) {
      throw new RuntimeException("Помилка збереження замовлення", e);
    } finally {
      db.releaseConnection(conn);
    }
  }

    @Override
    public Optional<Order> findById(Integer id) {
        return findAllWithDetails().stream().filter(o -> o.getId().equals(id)).findFirst();
    }

    @Override
    public List<Order> findAll() { return findAllWithDetails(); }

    @Override
    public List<Order> findAllWithDetails() {
        return query("""
            SELECT o.*, u.username, d.name AS design_name,
                   pm.brand || ' ' || pm.model_name AS phone_model_name
            FROM orders o
            JOIN users u         ON o.user_id        = u.id
            JOIN designs d       ON o.design_id      = d.id
            JOIN phone_models pm ON o.phone_model_id = pm.id
            ORDER BY o.order_date DESC
        """);
    }

    @Override
    public List<Order> findByUserId(int userId) {
        return query("""
            SELECT o.*, u.username, d.name AS design_name,
                   pm.brand || ' ' || pm.model_name AS phone_model_name
            FROM orders o
            JOIN users u         ON o.user_id        = u.id
            JOIN designs d       ON o.design_id      = d.id
            JOIN phone_models pm ON o.phone_model_id = pm.id
            WHERE o.user_id = ?
            ORDER BY o.order_date DESC
        """, userId);
    }

    @Override
    public List<Order> findByStatus(Order.Status status) {
        return query("""
            SELECT o.*, u.username, d.name AS design_name,
                   pm.brand || ' ' || pm.model_name AS phone_model_name
            FROM orders o
            JOIN users u         ON o.user_id        = u.id
            JOIN designs d       ON o.design_id      = d.id
            JOIN phone_models pm ON o.phone_model_id = pm.id
            WHERE o.status = ?
            ORDER BY o.order_date DESC
        """, status.name());
    }

    @Override
    public Order update(Order order) {
        Connection conn = null;
        try {
            conn = db.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE orders SET status=?, quantity=?, total_price=? WHERE id=?")) {
                ps.setString(1, order.getStatus().name());
                ps.setInt(2, order.getQuantity());
                ps.setDouble(3, order.getTotalPrice());
                ps.setInt(4, order.getId());
                ps.executeUpdate();
            }
            return order;
        } catch (SQLException e) { throw new RuntimeException("Помилка оновлення замовлення", e); }
        finally { db.releaseConnection(conn); }
    }

    @Override
    public boolean deleteById(Integer id) {
        Connection conn = null;
        try {
            conn = db.getConnection();
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM orders WHERE id=?")) {
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
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM orders")) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) { return 0; }
        finally { db.releaseConnection(conn); }
    }

    @Override
    public double getTotalRevenue() {
        Connection conn = null;
        try {
            conn = db.getConnection();
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(
                         "SELECT COALESCE(SUM(total_price),0) FROM orders WHERE status='COMPLETED'")) {
                return rs.next() ? rs.getDouble(1) : 0.0;
            }
        } catch (SQLException e) { return 0.0; }
        finally { db.releaseConnection(conn); }
    }

    private List<Order> query(String sql, Object... params) {
        List<Order> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = db.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) ps.setObject(i+1, params[i]);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(map(rs));
                }
            }
        } catch (SQLException e) { logger.error("query orders: {}", e.getMessage()); }
        finally { db.releaseConnection(conn); }
        return list;
    }

    private Order map(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setId(rs.getInt("id"));
        o.setUserId(rs.getInt("user_id"));
        o.setDesignId(rs.getInt("design_id"));
        o.setPhoneModelId(rs.getInt("phone_model_id"));
        o.setQuantity(rs.getInt("quantity"));
        o.setTotalPrice(rs.getDouble("total_price"));
        try { o.setStatus(Order.Status.valueOf(rs.getString("status"))); }
        catch (Exception e) { o.setStatus(Order.Status.PENDING); }
        String od = rs.getString("order_date");
        if (od != null) {
            try { o.setOrderDate(LocalDateTime.parse(od.replace(" ","T"))); }
            catch (Exception ignored) {}
        }

        try { o.setUsername(rs.getString("username")); } catch (Exception ignored) {}
        try { o.setDesignName(rs.getString("design_name")); } catch (Exception ignored) {}
        try { o.setPhoneModelName(rs.getString("phone_model_name")); } catch (Exception ignored) {}
        return o;
    }
}