package com.phonecase.service;

import com.phonecase.dto.OrderDTO;
import com.phonecase.model.Order;
import java.util.List;

public interface OrderService {
  OrderDTO createOrder(int userId, int designId, int phoneModelId, int quantity, String phoneNumber, String address);
  List<OrderDTO> getAllOrders();
  List<OrderDTO> getOrdersByUser(int userId);
  OrderDTO updateStatus(int orderId, Order.Status newStatus);
  boolean deleteOrder(int orderId);
  double getTotalRevenue();
  long countByStatus(Order.Status status);
  Order.Status nextStatus(Order.Status current);
}