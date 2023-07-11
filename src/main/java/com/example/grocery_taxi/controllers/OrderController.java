package com.example.grocery_taxi.controllers;

import com.example.grocery_taxi.entity.Order;
import com.example.grocery_taxi.entity.User;
import com.example.grocery_taxi.exception.OrderServiceException;
import com.example.grocery_taxi.model.UserRole;
import com.example.grocery_taxi.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:63342")
@RestController
@RequestMapping("/orders")
public class OrderController {

  private final OrderService orderService;

  @Autowired
  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @PostMapping
  public ResponseEntity<?> createOrder() throws OrderServiceException {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User authenticatedUser = (User) authentication.getPrincipal();

    if (authenticatedUser != null && authenticatedUser.getRole() != UserRole.Courier) {
      Order order = orderService.createOrder(Math.toIntExact(authenticatedUser.getId()));
      return ResponseEntity.ok(order);
    }

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
  }

  @PostMapping("/{orderId}/items")
  public ResponseEntity<String> addOrderItems(@PathVariable int orderId, @RequestBody List<AddOrderItemRequest> requestList, Authentication authentication) throws OrderServiceException {
    User authenticatedUser = (User) authentication.getPrincipal();
    for (AddOrderItemRequest request : requestList) {
      orderService.addOrderItem(orderId, request.getProductId(), request.getQuantity());
    }
    return ResponseEntity.ok("Order items added successfully.");
  }


  @DeleteMapping("/{orderId}/items/{itemId}")
  public ResponseEntity<String> removeOrderItem(@PathVariable int orderId, @PathVariable int itemId) throws OrderServiceException {
    orderService.removeOrderItem(orderId, itemId);
    return ResponseEntity.ok("Order item removed successfully.");
  }

  @PutMapping("/{orderId}/confirm")
  public ResponseEntity<String> confirmOrder(@PathVariable int orderId) throws OrderServiceException {
    orderService.confirmOrder(orderId);
    return ResponseEntity.ok("Order confirmed successfully.");
  }

  @PutMapping("/{orderId}/cancel")
  public ResponseEntity<String> cancelOrder(@PathVariable int orderId) throws OrderServiceException {
    orderService.cancelOrder(orderId);
    return ResponseEntity.ok("Order cancelled successfully.");
  }

  @PutMapping("/{orderId}/close")
  public ResponseEntity<String> closeOrder(@PathVariable int orderId) throws OrderServiceException {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User authenticatedUser = (User) authentication.getPrincipal();
    orderService.closeOrder(orderId, authenticatedUser.getRole());
    return ResponseEntity.ok("Order closed successfully.");
  }




  @PutMapping("/{orderId}/reopen")
  public ResponseEntity<String> reopenOrder(@PathVariable int orderId) throws OrderServiceException {
    orderService.reopenOrder(orderId);
    return ResponseEntity.ok("Order reopened successfully.");
  }
}