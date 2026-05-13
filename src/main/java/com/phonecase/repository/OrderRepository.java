package com.phonecase.repository;

import com.phonecase.model.Order;
import java.util.List;

/** Репозиторій замовлень. */

public interface OrderRepository extends CrudRepository<Order, Integer> {
    List<Order> findByUserId(int userId);
    List<Order> findByStatus(Order.Status status);
    List<Order> findAllWithDetails();
    double getTotalRevenue();
}