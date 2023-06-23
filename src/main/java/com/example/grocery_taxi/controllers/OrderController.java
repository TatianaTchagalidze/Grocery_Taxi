package com.example.grocery_taxi.controllers;

import com.example.grocery_taxi.entity.Order;
import com.example.grocery_taxi.entity.OrderItem;
import com.example.grocery_taxi.entity.Product;
import com.example.grocery_taxi.entity.User;
import com.example.grocery_taxi.exception.OrderItemServiceException;
import com.example.grocery_taxi.exception.OrderServiceException;
import com.example.grocery_taxi.filter.JwtAuthenticationFilter;
import com.example.grocery_taxi.service.OrderItemService;
import com.example.grocery_taxi.service.OrderService;
import com.example.grocery_taxi.service.ProductService;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {
  private final OrderService orderService;
  private final ProductService productService;
  private final OrderItemService orderItemService;

  private final JwtAuthenticationFilter jwtAuthenticationFilter;


  @Autowired
  public OrderController(OrderService orderService, ProductService productService, OrderItemService orderItemService, JwtAuthenticationFilter jwtAuthenticationFilter) {
    this.orderService = orderService;
    this.productService = productService;
    this.orderItemService = orderItemService;
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;

  }

  @PostMapping
  public ResponseEntity<Order> createOrder(HttpServletRequest request) {
    User authenticatedUser = jwtAuthenticationFilter.getAuthenticatedUserFromTokenInCookies(request);
    if (authenticatedUser != null) {
      Order order = orderService.createOrder(authenticatedUser);
      return ResponseEntity.ok(order);
    }
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
  }



  @PostMapping("/{orderId}/items")
  public ResponseEntity<String> addOrderItem(@PathVariable Long orderId, @RequestBody AddOrderItemRequest request) {
    try {
      Order order = orderService.getOrderById(orderId);
      Product product = productService.getProductById(request.getProductId());
      int quantity = request.getQuantity();

      int availableQuantity = productService.getAvailableQuantity(product);
      if (quantity > availableQuantity) {
        quantity = availableQuantity;
      }

      orderService.addOrderItem(order, product, quantity);

      return ResponseEntity.ok("Order item added successfully.");
    } catch (OrderServiceException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @PutMapping("/{orderId}/items/{itemId}")
  public ResponseEntity<String> updateOrderItemQuantity(@PathVariable Long orderId, @PathVariable Long itemId, @RequestBody UpdateOrderItemRequest request) {
    try {
      Order order = orderService.getOrderById(orderId);
      OrderItem orderItem = orderService.getOrderItemById(itemId); // Assuming you have a method to get the OrderItem by ID
      orderService.updateOrderItemQuantity(orderItem, request.getQuantity());
      return ResponseEntity.ok("Order item quantity updated successfully.");
    } catch (OrderServiceException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @DeleteMapping("/{orderId}/items/{itemId}")
  public ResponseEntity<String> removeOrderItem(@PathVariable Long orderId, @PathVariable Long itemId) {
    try {
      Order order = orderService.getOrderById(orderId);
      OrderItem orderItem = orderItemService.getOrderItemById(itemId);

      orderItemService.removeOrderItem(order, orderItem);

      return ResponseEntity.ok("Order item removed successfully.");
    } catch (OrderItemServiceException e) {
      // Handle the exception
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (OrderServiceException e) {
      throw new RuntimeException(e);
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


  @PostMapping("/{orderId}/complete")
  public ResponseEntity<String> completeOrder(@PathVariable Long orderId) {
    try {
      orderService.completeOrder(orderId);
      return ResponseEntity.ok("Order completed successfully.");
    } catch (OrderServiceException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @PostMapping("/{orderId}/confirm")
  public ResponseEntity<String> confirmOrder(@PathVariable Long orderId) {
    try {
      orderService.confirmOrder(orderId);
      return ResponseEntity.ok("Order confirmed successfully.");
    } catch (OrderServiceException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

}
