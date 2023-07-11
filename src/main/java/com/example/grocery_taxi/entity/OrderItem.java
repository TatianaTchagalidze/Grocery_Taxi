package com.example.grocery_taxi.entity;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import lombok.Setter;

@Entity
@Table(name = "orderitem")
@NoArgsConstructor
@Getter
@Setter
public class OrderItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @ManyToOne
  @JoinColumn(name = "order_id")
  private Order order;

  @ManyToOne
  @JoinColumn(name = "product_id")
  private Product product;

  private int quantity;

  private BigDecimal amount;


  public void setAmount() {
    BigDecimal productPrice = product.getPrice();
    this.amount = productPrice != null ? productPrice.multiply(BigDecimal.valueOf(quantity)) : BigDecimal.ZERO;
  }
}