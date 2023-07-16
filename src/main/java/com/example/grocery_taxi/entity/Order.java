package com.example.grocery_taxi.entity;

import com.example.grocery_taxi.dto.UserDto;
import com.example.grocery_taxi.model.OrderState;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(name = "order_state", nullable = false, length = 20)
  private OrderState orderState;

  @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
  private BigDecimal totalAmount;

  @Column(nullable = false, columnDefinition = "boolean default true")
  private boolean editable;

  @JsonManagedReference
  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<OrderItem> orderItems;

  @Transient
  private BigDecimal approximatePrice;


  public Order() {
    this.orderItems = new HashSet<>();
  }

  public void setUser(User user) {
    this.user = user;
  }

  public void setState(OrderState orderState) {
    this.orderState = orderState;
  }

  public OrderState getState() {
    return orderState;
  }

  public Set<OrderItem> getOrderItems() {
    return orderItems;
  }

  public boolean isClosed() {
    return orderState == OrderState.CLOSED;
  }

  public void setClosed(boolean closed) {
    if (closed) {
      orderState = OrderState.CLOSED;
    } else {
      throw new IllegalStateException("Cannot set order as not closed.");
    }
  }


  public UserDto getUserDto() {
    UserDto userDto = new UserDto();
    userDto.setEmail(user.getEmail());
    userDto.setFirstName(user.getFirstName());
    userDto.setLastName(user.getLastName());
    userDto.setAddress(user.getAddress());
    userDto.setPhone_number(user.getPhone_number());
    // Set other properties as needed
    return userDto;
  }
}