package com.example.grocery_taxi.controllers;

import com.example.grocery_taxi.entity.Order;
import com.example.grocery_taxi.entity.User;
import com.example.grocery_taxi.exception.OrderServiceException;
import com.example.grocery_taxi.filter.JwtAuthenticationFilter;
import com.example.grocery_taxi.model.OrderState;
import com.example.grocery_taxi.model.UserRole;
import com.example.grocery_taxi.repository.OrderRepository;
import com.example.grocery_taxi.repository.UserRepository;
import com.example.grocery_taxi.service.OrderService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/couriers")
public class CourierController {
  private final OrderService orderService;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final UserRepository userRepository;
  private final OrderRepository orderRepository;

  @Autowired
  public CourierController(OrderService orderService, JwtAuthenticationFilter jwtAuthenticationFilter, UserRepository userRepository, OrderRepository orderRepository) {
    this.orderService = orderService;
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.userRepository = userRepository;
    this.orderRepository = orderRepository;
  }

  @GetMapping("/orders/open")
  public ResponseEntity<List<Order>> getOpenOrders(HttpServletRequest request) {
    String token = jwtAuthenticationFilter.extractTokenFromCookies(request.getCookies());
    User authenticatedUser = jwtAuthenticationFilter.getAuthenticatedUserFromTokenInCookies(token);

    if (authenticatedUser != null) {
      if (authenticatedUser.getRole() != UserRole.Courier) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
      }

      List<Order> openOrders = orderService.getOrdersByState(OrderState.CONFIRMED);
      return ResponseEntity.ok(openOrders);
    }

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
  }

  @PostMapping("/orders/{orderId}/pickup")
  public ResponseEntity<String> pickUpOrder(@PathVariable int orderId, HttpServletRequest request) throws OrderServiceException {
    String token = jwtAuthenticationFilter.extractTokenFromCookies(request.getCookies());
    User authenticatedUser = jwtAuthenticationFilter.getAuthenticatedUserFromTokenInCookies(token);

    if (authenticatedUser != null) {
      Order order = orderService.getOrderById(orderId);

      // Check if the order is already picked up
      if (order.getState() == OrderState.IN_PROGRESS) {
        return ResponseEntity.badRequest().body("Order is already picked up by another courier.");
      }

      // Check if the order is available
      if (order.getState() != OrderState.CONFIRMED) {
        return ResponseEntity.badRequest().body("Order is not available for pickup.");
      }

      // Update the order state to "IN_PROGRESS"
      order.setState(OrderState.IN_PROGRESS);
      orderRepository.save(order);

      return ResponseEntity.ok("Order picked up successfully.");
    }

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
  }

}

