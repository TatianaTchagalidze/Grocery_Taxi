package com.example.grocery_taxi.entity;

import java.math.BigDecimal;
import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Product")
@Getter
@Setter
public class Product {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private int id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "description")
  private String description;

  @Column(name = "price", nullable = false)
  private BigDecimal price;

  @Column(name = "available_quantity")
  private int availableQuantity;
}
