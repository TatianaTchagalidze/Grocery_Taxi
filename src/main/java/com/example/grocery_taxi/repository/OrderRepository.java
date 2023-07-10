package com.example.grocery_taxi.repository;

import com.example.grocery_taxi.entity.Order;
import com.example.grocery_taxi.model.OrderState;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
  List<Order> findByOrderState(OrderState orderState);
}
