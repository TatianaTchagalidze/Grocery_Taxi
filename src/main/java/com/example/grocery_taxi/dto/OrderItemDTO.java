package com.example.grocery_taxi.dto;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemDTO {
  private Integer id;
  private String productName;
  private int quantity;
  private BigDecimal amount;

  public OrderItemDTO(Integer id, String productName, int quantity, BigDecimal amount) {
    this.id = id;
    this.productName = productName;
    this.quantity = quantity;
    this.amount = amount;
  }
}

