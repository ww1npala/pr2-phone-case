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
    public OrderDTO createOrder(int userId, int designId, int phoneModelId, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Кількість повинна бути більше 0.");

        Design design = designRepository.findById(designId)
                .orElseThrow(() -> new IllegalArgumentException("Дизайн не знайдено: " + designId));

        if (!design.isAvailable()) throw new IllegalStateException("Дизайн наразі недоступний.");

        double totalPrice = design.getPrice() * quantity;

        Order order = new Order();
        order.setUserId(userId);
        order.setDesignId(designId);
        order.setPhoneModelId(phoneModelId);
        order.setQuantity(quantity);
        order.setTotalPrice(totalPrice);
        order.setStatus(Order.Status.PENDING);

        Order saved = orderRepository.save(order);
        logger.info("Створено замовлення: id={}, total={}", saved.getId(), totalPrice);
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
        return toDTO(orderRepository.update(order));
    }

    @Override
    public boolean deleteOrder(int orderId) {
        return orderRepository.deleteById(orderId);
    }

    @Override
    public double getTotalRevenue() {
        return orderRepository.getTotalRevenue();
    }

    @Override
    public long countByStatus(Order.Status status) {
        return orderRepository.findByStatus(status).size();
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
        dto.setStatus(o.getStatus() != null ? o.getStatus().getDisplayName() : "");
        dto.setOrderDate(o.getOrderDate() != null ? o.getOrderDate().toString() : "");
        return dto;
    }
}