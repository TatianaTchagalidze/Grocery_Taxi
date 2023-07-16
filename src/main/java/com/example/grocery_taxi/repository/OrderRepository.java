package com.example.grocery_taxi.repository;

import com.example.grocery_taxi.entity.Order;
import com.example.grocery_taxi.model.OrderState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
  List<Order> findByOrderState(OrderState orderState);
}