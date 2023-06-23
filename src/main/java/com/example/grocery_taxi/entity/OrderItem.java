package com.example.grocery_taxi.entity;

import java.math.BigDecimal;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import lombok.Setter;

@Entity
@Table(name = "orderitem")
@Data
@NoArgsConstructor
@Getter
@Setter
public class OrderItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "order_id")
  private Order order;

  @ManyToOne
  @JoinColumn(name = "product_id")
  private Product product;

  private int quantity;

  // Additional methods

  public BigDecimal getAmount() {
    return null;
  }

  public void setAmount(BigDecimal multiply) {
  }
}
