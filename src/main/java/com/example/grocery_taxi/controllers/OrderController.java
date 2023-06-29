package com.example.grocery_taxi.controllers;

import com.example.grocery_taxi.entity.Order;
import com.example.grocery_taxi.entity.OrderItem;
import com.example.grocery_taxi.entity.User;
import com.example.grocery_taxi.exception.OrderItemServiceException;
import com.example.grocery_taxi.exception.OrderServiceException;
import com.example.grocery_taxi.repository.UserRepository;
import com.example.grocery_taxi.service.OrderItemService;
import com.example.grocery_taxi.service.OrderService;
import com.example.grocery_taxi.filter.JwtAuthenticationFilter;
import com.example.grocery_taxi.config.JwtUtil;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {
  private final OrderService orderService;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final OrderItemService orderItemService;

  private final UserRepository userRepository;
  private final JwtUtil jwtUtil;

  @Autowired
  public OrderController(OrderService orderService, JwtAuthenticationFilter jwtAuthenticationFilter, OrderItemService orderItemService, UserRepository userRepository, JwtUtil jwtUtil) {
    this.orderService = orderService;
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.orderItemService = orderItemService;
    this.userRepository = userRepository;
    this.jwtUtil = jwtUtil;
  }

  @PostMapping
  public ResponseEntity<Order> createOrder(HttpServletRequest request, HttpServletResponse response) throws OrderServiceException {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      String token = jwtAuthenticationFilter.extractTokenFromCookies(cookies);
      if (token != null && jwtUtil.validateToken(token)) {
        try {
          String username = jwtUtil.extractUsernameFromToken(token);
          User authenticatedUser = userRepository.findByEmail(username).orElse(null);
          if (authenticatedUser != null) {
            Order order = orderService.createOrder(authenticatedUser.getId());
            return ResponseEntity.ok(order);
          }
        } catch (Exception e) {
          jwtAuthenticationFilter.removeTokenFromCookies(response);
        }
      }
    }
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
  }



  @PostMapping("/{orderId}/items")
  public ResponseEntity<String> addOrderItem(@PathVariable Long orderId, @RequestBody AddOrderItemRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
    String token = jwtAuthenticationFilter.extractTokenFromCookies(httpRequest.getCookies());
    User authenticatedUser = jwtAuthenticationFilter.getAuthenticatedUserFromTokenInCookies(token);

    if (authenticatedUser != null) {
      try {
        Order order = orderService.getOrderById(orderId);
        Long productId = request.getProductId();
        int quantity = request.getQuantity();

        orderService.addOrderItem(order, productId, quantity);

        return ResponseEntity.ok("Order item added successfully.");
      } catch (OrderServiceException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
      }
    } else {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
    }
  }



  @PutMapping("/{orderId}/items/{itemId}")
  public ResponseEntity<String> updateOrderItemQuantity(@PathVariable Long orderId, @PathVariable Long itemId, @RequestBody UpdateOrderItemRequest request) {
    try {
      Order order = orderService.getOrderById(orderId);
      OrderItem orderItem = orderItemService.getOrderItemById(itemId);
      int quantity = request.getQuantity();

      orderService.updateOrderItemQuantity(orderItem, quantity);

      return ResponseEntity.ok("Order item quantity updated successfully.");
    } catch (OrderServiceException | OrderItemServiceException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @DeleteMapping("/{orderId}/items/{itemId}")
  public ResponseEntity<String> removeOrderItem(@PathVariable Long orderId, @PathVariable Long itemId) {
    try {
      Order order = orderService.getOrderById(orderId);
      OrderItem orderItem = orderItemService.getOrderItemById(itemId);

      orderService.removeOrderItem(order, orderItem);

      return ResponseEntity.ok("Order item removed successfully.");
    } catch (OrderServiceException | OrderItemServiceException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @PutMapping("/{orderId}/confirm")
  public ResponseEntity<String> confirmOrder(@PathVariable Long orderId) {
    try {
      Order order = orderService.getOrderById(orderId);
      orderService.confirmOrder(order);
      return ResponseEntity.ok("Order confirmed successfully.");
    } catch (OrderServiceException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @PostMapping("/{orderId}/complete")
  public ResponseEntity<String> completeOrder(@PathVariable Long orderId) {
    try {
      orderService.completeOrder(orderId);
      return ResponseEntity.ok("Order completed successfully.");
    } catch (OrderServiceException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @PutMapping("/{orderId}/cancel")
  public ResponseEntity<String> cancelOrder(@PathVariable Long orderId) {
    try {
      Order order = orderService.getOrderById(orderId);
      orderService.cancelOrder(order);
      return ResponseEntity.ok("Order cancelled successfully.");
    } catch (OrderServiceException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }
}
