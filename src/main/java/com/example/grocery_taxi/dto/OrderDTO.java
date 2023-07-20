package com.example.grocery_taxi.dto;

import com.example.grocery_taxi.entity.Order;
import com.example.grocery_taxi.entity.OrderItem;
import com.example.grocery_taxi.model.OrderState;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderDTO {
  private int id;
  private OrderState orderState;
  private BigDecimal totalAmount;
  private RegistrationResponseDto userInfo;
  private Set<OrderItemDTO> orderItems;


  public OrderDTO(Order order, List<OrderItemDTO> orderItems) {
    this.id = order.getId();
    this.orderState = order.getOrderState();
    this.totalAmount = order.getTotalAmount();
    this.userInfo = new RegistrationResponseDto(order.getUserDto(), order.getUserId());
    this.orderItems = new HashSet<>();

    for (OrderItem orderItem : order.getOrderItems()) {
      OrderItemDTO orderItemDTO = new OrderItemDTO(orderItem.getId(), orderItem.getProduct().getName(), orderItem.getQuantity(), orderItem.getAmount());
      this.orderItems.add(orderItemDTO);
    }
  }

}
