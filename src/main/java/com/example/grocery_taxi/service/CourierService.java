// CourierService.java
package com.example.grocery_taxi.service;

import com.example.grocery_taxi.dto.OrderDTO;
import com.example.grocery_taxi.dto.OrderItemDTO;
import com.example.grocery_taxi.entity.Order;
import com.example.grocery_taxi.exception.OrderServiceException;
import com.example.grocery_taxi.model.OrderState;
import com.example.grocery_taxi.repository.OrderRepository;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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

  @PreAuthorize("hasRole('Courier')")
  public List<OrderDTO> getOpenOrders() {
    List<Order> openOrders = orderRepository.findByOrderState(OrderState.OPEN);
    List<OrderDTO> openOrderDTOs = openOrders.stream()
        .map(order -> {
          List<OrderItemDTO> orderItems = // Retrieve the order items for the order
              order.getOrderItems().stream()
                  .map(orderItem -> new OrderItemDTO(
                      orderItem.getId(),
                      orderItem.getProduct().getName(),
                      orderItem.getQuantity(),
                      orderItem.getAmount()
                  ))
                  .collect(Collectors.toList());
          return new OrderDTO(order, orderItems);
        })
        .collect(Collectors.toList());
    return openOrderDTOs;
  }

  @PreAuthorize("hasRole('Courier')")
  public int pickUpOrder(int orderId) throws OrderServiceException {
    Order order = getOrderById(orderId);

    if (order.getState() == OrderState.IN_PROGRESS) {
      throw new OrderServiceException("Order is already picked up by another courier.");
    }

    if (order.getState() != OrderState.OPEN) {
      throw new OrderServiceException("Order is not available for pickup.");
    }

    order.setState(OrderState.IN_PROGRESS);
    orderRepository.save(order);

    return orderId; // Return the picked-up orderId
  }


  private Order getOrderById(int orderId) throws OrderServiceException {
    Optional<Order> optionalOrder = orderRepository.findById(orderId);
    if (optionalOrder.isEmpty()) {
      throw new OrderServiceException("Order not found with ID: " + orderId);
    }
    return optionalOrder.get();
  }
}