package com.example.grocery_taxi.service;

import com.example.grocery_taxi.entity.OrderItem;
import com.example.grocery_taxi.exception.OrderItemServiceException;
import com.example.grocery_taxi.repository.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class OrderItemService {

  private final OrderItemRepository orderItemRepository;

  @Autowired
  public OrderItemService(OrderItemRepository orderItemRepository) {
    this.orderItemRepository = orderItemRepository;
  }

  public OrderItem getOrderItemById(Long itemId) throws OrderItemServiceException {
    Optional<OrderItem> optionalOrderItem = orderItemRepository.findById(itemId);
    if (optionalOrderItem.isEmpty()) {
      throw new OrderItemServiceException("Invalid order item ID: " + itemId);
    }
    return optionalOrderItem.get();
  }
}
