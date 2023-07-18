package com.example.grocery_taxi.repository;

import com.example.grocery_taxi.entity.OrderItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
  List<OrderItem> findByOrderId(int orderId);

}
