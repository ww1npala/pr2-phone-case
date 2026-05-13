package com.phonecase.dto;

/**
 * DTO для передачі даних замовлення.
 */
public class OrderDTO {
    private Integer id;
    private int userId;
    private String username;
    private int designId;
    private String designName;
    private int phoneModelId;
    private String phoneModelName;
    private int quantity;
    private double totalPrice;
    private String status;
    private String orderDate;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public int getDesignId() { return designId; }
    public void setDesignId(int designId) { this.designId = designId; }
    public String getDesignName() { return designName; }
    public void setDesignName(String designName) { this.designName = designName; }
    public int getPhoneModelId() { return phoneModelId; }
    public void setPhoneModelId(int phoneModelId) { this.phoneModelId = phoneModelId; }
    public String getPhoneModelName() { return phoneModelName; }
    public void setPhoneModelName(String phoneModelName) { this.phoneModelName = phoneModelName; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getOrderDate() { return orderDate; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }
}
