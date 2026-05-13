package com.phonecase.service;

import com.phonecase.dto.OrderDTO;
import com.phonecase.model.Design;
import com.phonecase.model.Order;
import com.phonecase.repository.DesignRepository;
import com.phonecase.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Реалізація OrderService.
 * Логіка замовлень: створення, зміна статусу, підрахунок виручки.
 * Виручка рахується тільки з DELIVERED замовлень.
 */
@Singleton
public class OrderServiceImpl implements OrderService {

  private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
  private final OrderRepository orderRepository;
  private final DesignRepository designRepository;

  @Inject
  public OrderServiceImpl(OrderRepository orderRepository, DesignRepository designRepository) {
    this.orderRepository = orderRepository;
    this.designRepository = designRepository;
  }

  @Override
  public OrderDTO createOrder(int userId, int designId, int phoneModelId,
      int quantity, String phoneNumber, String address) {
    if (quantity <= 0) throw new IllegalArgumentException("Кількість повинна бути більше 0.");
    if (phoneNumber == null || phoneNumber.isBlank())
      throw new IllegalArgumentException("Вкажіть номер телефону для зв'язку.");
    if (address == null || address.isBlank())
      throw new IllegalArgumentException("Вкажіть адресу доставки.");

    Design design = designRepository.findById(designId)
        .orElseThrow(() -> new IllegalArgumentException("Дизайн не знайдено: " + designId));
    if (!design.isAvailable())
      throw new IllegalStateException("Дизайн наразі недоступний для замовлення.");

    double totalPrice = design.getPrice() * quantity;

    Order order = new Order();
    order.setUserId(userId);
    order.setDesignId(designId);
    order.setPhoneModelId(phoneModelId);
    order.setQuantity(quantity);
    order.setTotalPrice(totalPrice);
    order.setStatus(Order.Status.PENDING);
    order.setPhoneNumber(phoneNumber);
    order.setAddress(address);

    Order saved = orderRepository.save(order);
    logger.info("Створено замовлення: id={}, userId={}, total={}", saved.getId(), userId, totalPrice);
    return toDTO(saved);
  }

  @Override
  public List<OrderDTO> getAllOrders() {
    return orderRepository.findAllWithDetails().stream().map(this::toDTO).collect(Collectors.toList());
  }

  @Override
  public List<OrderDTO> getOrdersByUser(int userId) {
    return orderRepository.findByUserId(userId).stream().map(this::toDTO).collect(Collectors.toList());
  }

  @Override
  public OrderDTO updateStatus(int orderId, Order.Status newStatus) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new IllegalArgumentException("Замовлення не знайдено: " + orderId));
    order.setStatus(newStatus);
    logger.info("Статус замовлення #{} змінено на {}", orderId, newStatus);
    return toDTO(orderRepository.update(order));
  }

  @Override
  public boolean deleteOrder(int orderId) {
    return orderRepository.deleteById(orderId);
  }

  @Override
  public double getTotalRevenue() {
    return orderRepository.findAllWithDetails().stream()
        .filter(o -> o.getStatus() == Order.Status.DELIVERED)
        .mapToDouble(Order::getTotalPrice)
        .sum();
  }

  @Override
  public long countByStatus(Order.Status status) {
    return orderRepository.findByStatus(status).size();
  }

  @Override
  public Order.Status nextStatus(Order.Status current) {
    return switch (current) {
      case PENDING   -> Order.Status.CONFIRMED;
      case CONFIRMED -> Order.Status.SHIPPED;
      case SHIPPED   -> Order.Status.DELIVERED;
      default        -> current;
    };
  }

  private OrderDTO toDTO(Order o) {
    OrderDTO dto = new OrderDTO();
    dto.setId(o.getId());
    dto.setUserId(o.getUserId());
    dto.setUsername(o.getUsername());
    dto.setDesignId(o.getDesignId());
    dto.setDesignName(o.getDesignName());
    dto.setPhoneModelId(o.getPhoneModelId());
    dto.setPhoneModelName(o.getPhoneModelName());
    dto.setQuantity(o.getQuantity());
    dto.setTotalPrice(o.getTotalPrice());
    if (o.getStatus() != null) {
      dto.setStatus(o.getStatus().getDisplayName());
      dto.setStatusRaw(o.getStatus().name());
    }
    dto.setOrderDate(o.getOrderDate() != null ? o.getOrderDate().toString() : "");
    dto.setPhoneNumber(o.getPhoneNumber());
    dto.setAddress(o.getAddress());
    return dto;
  }
}