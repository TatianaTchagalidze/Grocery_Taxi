package com.example.grocery_taxi.dto;

import com.example.grocery_taxi.entity.Order;
import com.example.grocery_taxi.model.OrderState;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderDTO {
  private int id;
  private OrderState orderState;
  private BigDecimal totalAmount;
  private RegistrationResponseDto userInfo;


  public OrderDTO(Order order) {
    this.id = order.getId();
    this.orderState = order.getOrderState();
    this.totalAmount = order.getTotalAmount();
    this.userInfo = new RegistrationResponseDto(order.getUserDto());
  }
}
