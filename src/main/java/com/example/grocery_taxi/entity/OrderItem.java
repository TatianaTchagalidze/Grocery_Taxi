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


  public BigDecimal getAmount() {
    return null;
  }

  public void setAmount(BigDecimal multiply) {
  }
}
