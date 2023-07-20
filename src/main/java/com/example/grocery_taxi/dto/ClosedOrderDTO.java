package com.example.grocery_taxi.dto;

import com.example.grocery_taxi.entity.Order;
import com.example.grocery_taxi.model.OrderState;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClosedOrderDTO {
  private int id;
  private OrderState orderState;
  private BigDecimal totalAmount;
  private RegistrationResponseDto userInfo;
  private List<OrderItemDTO> orderItems;

  public ClosedOrderDTO(int id, OrderState orderState, BigDecimal totalAmount, RegistrationResponseDto userInfo, List<OrderItemDTO> orderItems) {
    this.id = id;
    this.orderState = orderState;
    this.totalAmount = totalAmount;
    this.userInfo = userInfo;
    this.orderItems = orderItems;
  }
}


