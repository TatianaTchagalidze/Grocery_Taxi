// CourierService.java
package com.example.grocery_taxi.service;

import com.example.grocery_taxi.entity.Order;
import com.example.grocery_taxi.exception.OrderServiceException;
import com.example.grocery_taxi.model.OrderState;
import com.example.grocery_taxi.repository.OrderRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;

@Service
public class CourierService {

  private final OrderRepository orderRepository;

  @Autowired
  public CourierService(OrderRepository orderRepository) {
    this.orderRepository = orderRepository;
  }

  public List<Order> getOpenOrders() {
    return orderRepository.findByOrderState(OrderState.OPEN);

  }

  public void pickUpOrder(int orderId) throws OrderServiceException {
    Order order = getOrderById(orderId);

    if (order.getState() == OrderState.IN_PROGRESS) {
      throw new OrderServiceException("Order is already picked up by another courier.");
    }

    if (order.getState() != OrderState.CONFIRMED) {
      throw new OrderServiceException("Order is not available for pickup.");
    }

    order.setState(OrderState.IN_PROGRESS);
    orderRepository.save(order);
  }


  private Order getOrderById(int orderId) throws OrderServiceException {
    Optional<Order> optionalOrder = orderRepository.findById(orderId);
    if (optionalOrder.isEmpty()) {
      throw new OrderServiceException("Order not found with ID: " + orderId);
    }
    return optionalOrder.get();
  }
}
