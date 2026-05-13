package com.phonecase.model;

import java.time.LocalDateTime;

/**
 * Замовлення чохла.
 */
public class Order extends BaseEntity {

  public enum Status {
    PENDING("В обробці"),
    CONFIRMED("Підтверджено"),
    SHIPPED("Відправлено"),
    DELIVERED("Отримано"),
    CANCELLED("Скасовано");

    private final String displayName;
    Status(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }

    @Override public String toString() { return displayName; }
  }

  private Integer userId;
  private Integer designId;
  private Integer phoneModelId;
  private int quantity;
  private double totalPrice;
  private Status status;
  private LocalDateTime orderDate;
  private String phoneNumber;
  private String address;

  private String designName;
  private String phoneModelName;
  private String username;

  public Order() { this.status = Status.PENDING; }

  public Integer getUserId() { return userId; }
  public void setUserId(Integer userId) { this.userId = userId; }
  public Integer getDesignId() { return designId; }
  public void setDesignId(Integer designId) { this.designId = designId; }
  public Integer getPhoneModelId() { return phoneModelId; }
  public void setPhoneModelId(Integer phoneModelId) { this.phoneModelId = phoneModelId; }
  public int getQuantity() { return quantity; }
  public void setQuantity(int quantity) { this.quantity = quantity; }
  public double getTotalPrice() { return totalPrice; }
  public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
  public Status getStatus() { return status; }
  public void setStatus(Status status) { this.status = status; }
  public LocalDateTime getOrderDate() { return orderDate; }
  public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
  public String getPhoneNumber() { return phoneNumber; }
  public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
  public String getAddress() { return address; }
  public void setAddress(String address) { this.address = address; }
  public String getDesignName() { return designName; }
  public void setDesignName(String designName) { this.designName = designName; }
  public String getPhoneModelName() { return phoneModelName; }
  public void setPhoneModelName(String phoneModelName) { this.phoneModelName = phoneModelName; }
  public String getUsername() { return username; }
  public void setUsername(String username) { this.username = username; }

  @Override
  public String toString() {
    return "Order{id=" + id + ", userId=" + userId + ", status=" + status + "}";
  }
}