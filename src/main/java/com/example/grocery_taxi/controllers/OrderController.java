package com.example.grocery_taxi.controllers;

import com.example.grocery_taxi.entity.Order;
import com.example.grocery_taxi.entity.OrderItem;
import com.example.grocery_taxi.entity.User;
import com.example.grocery_taxi.exception.OrderItemServiceException;
import com.example.grocery_taxi.exception.OrderServiceException;
import com.example.grocery_taxi.filter.JwtAuthenticationFilter;
import com.example.grocery_taxi.model.UserRole;
import com.example.grocery_taxi.service.OrderItemService;
import com.example.grocery_taxi.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {
  private final OrderService orderService;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final OrderItemService orderItemService;

  @Autowired
  public OrderController(OrderService orderService, JwtAuthenticationFilter jwtAuthenticationFilter, OrderItemService orderItemService) {
    this.orderService = orderService;
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.orderItemService = orderItemService;
  }

  @PostMapping
  public ResponseEntity<?> createOrder(HttpServletRequest request, HttpServletResponse response) throws OrderServiceException {
    String token = jwtAuthenticationFilter.extractTokenFromCookies(request.getCookies());
    User authenticatedUser = jwtAuthenticationFilter.getAuthenticatedUserFromTokenInCookies(token);

    if (authenticatedUser != null) {
      // Check if the user is a courier
      if (authenticatedUser.getRole() == UserRole.Courier) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Courier users are not allowed to create orders.");
      }

      Order order = orderService.createOrder(Math.toIntExact(authenticatedUser.getId()));
      return ResponseEntity.ok(order);
    }

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
  }

  @PostMapping("/{orderId}/items")
  public ResponseEntity<String> addOrderItems(@PathVariable int orderId, @RequestBody List<AddOrderItemRequest> requestList,
                                              HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws OrderServiceException {
    String token = jwtAuthenticationFilter.extractTokenFromCookies(httpRequest.getCookies());
    User authenticatedUser = jwtAuthenticationFilter.getAuthenticatedUserFromTokenInCookies(token);

    if (authenticatedUser != null) {
      Order order = orderService.getOrderById(orderId);
      BigDecimal totalAmountBefore = order.getTotalAmount(); // Get the current total amount

      for (AddOrderItemRequest request : requestList) {
        int productId = request.getProductId();
        int quantity = request.getQuantity();

        orderService.addOrderItem(order, productId, quantity);
      }

      BigDecimal totalAmountAfter = order.getTotalAmount(); // Get the updated total amount

      // Update the total amount in the order if it has changed
      if (totalAmountBefore.compareTo(totalAmountAfter) != 0) {
        orderService.updateOrderTotalAmount(order, totalAmountAfter);
      }

      return ResponseEntity.ok("Order items added successfully.");
    } else {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
    }
  }

  @PutMapping("/{orderId}/items/{itemId}")
  public ResponseEntity<String> updateOrderItemQuantity(@PathVariable int orderId, @PathVariable int itemId, @RequestBody UpdateOrderItemRequest request) throws OrderServiceException, OrderItemServiceException {
    Order order = orderService.getOrderById(orderId);
    OrderItem orderItem = orderItemService.getOrderItemById(itemId);
    int quantity = request.getQuantity();

    orderService.updateOrderItemQuantity(orderItem, quantity);

    return ResponseEntity.ok("Order item quantity updated successfully.");
  }


  @DeleteMapping("/{orderId}/items/{itemId}")
  public ResponseEntity<String> removeOrderItem(@PathVariable int orderId, @PathVariable int itemId) throws OrderServiceException, OrderItemServiceException {
    Order order = orderService.getOrderById(orderId);
    OrderItem orderItem = orderItemService.getOrderItemById(itemId);

    orderService.removeOrderItem(order, orderItem);

    return ResponseEntity.ok("Order item removed successfully.");
  }


  @PutMapping("/{orderId}/confirm")
  public ResponseEntity<String> confirmOrder(@PathVariable int orderId) throws OrderServiceException {
    Order order = orderService.getOrderById(orderId);
    orderService.confirmOrder(order);
    return ResponseEntity.ok("Order confirmed successfully.");
  }

  @PostMapping("/{orderId}/complete")
  public ResponseEntity<String> completeOrder(@PathVariable int orderId) throws OrderServiceException {
    orderService.completeOrder(orderId);
    return ResponseEntity.ok("Order completed successfully.");
  }

  @PutMapping("/{orderId}/cancel")
  public ResponseEntity<String> cancelOrder(@PathVariable int orderId) throws OrderServiceException {
    Order order = orderService.getOrderById(orderId);
    orderService.cancelOrder(order);
    return ResponseEntity.ok("Order cancelled successfully.");
  }

  @PutMapping("/{orderId}/close/consumer")
  public ResponseEntity<String> closeOrderByConsumer(@PathVariable int orderId) {
    try {
      Order order = orderService.getOrderById(orderId);
      orderService.closeOrderByConsumer(order);
      return ResponseEntity.ok("Order closed successfully.");
    } catch (OrderServiceException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @PutMapping("/{orderId}/close/courier")
  public ResponseEntity<String> closeOrderByCourier(@PathVariable int orderId) {
    try {
      Order order = orderService.getOrderById(orderId);
      orderService.closeOrderByCourier(order);
      return ResponseEntity.ok("Order closed successfully.");
    } catch (OrderServiceException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @PutMapping("/{orderId}/reopen")
  public ResponseEntity<String> reopenOrder(@PathVariable int orderId) {
    try {
      Order order = orderService.getOrderById(orderId);
      orderService.reopenOrder(order);
      return ResponseEntity.ok("Order reopened successfully.");
    } catch (OrderServiceException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }


}

