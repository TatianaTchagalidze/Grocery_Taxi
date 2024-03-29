package com.example.grocery_taxi.controllers;

import com.example.grocery_taxi.dto.ClosedOrderDTO;
import com.example.grocery_taxi.dto.OrderDTO;
import com.example.grocery_taxi.entity.User;
import com.example.grocery_taxi.exception.OrderServiceException;
import com.example.grocery_taxi.model.UserRole;
import com.example.grocery_taxi.service.OrderService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

  private final OrderService orderService;

  @Autowired
  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @PostMapping
  public ResponseEntity<?> createOrder(@RequestBody List<AddOrderItemRequest> requestList) throws OrderServiceException {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User authenticatedUser = (User) authentication.getPrincipal();

    if (authenticatedUser != null && authenticatedUser.getRole() != UserRole.Courier) {
      OrderDTO order = orderService.createOrder(Math.toIntExact(authenticatedUser.getId()), requestList);

      return ResponseEntity.ok(order);
    }

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
  }


  @GetMapping("/{orderId}/items")
  public ResponseEntity<List<Integer>> getOrderItemIds(@PathVariable int orderId) {
    List<Integer> itemIds = orderService.getOrderItemIds(orderId);
    return ResponseEntity.ok(itemIds);
  }


  @PutMapping("/{orderId}/items/{itemId}")
  public ResponseEntity<String> updateOrderItemQuantity(
      @PathVariable int orderId,
      @PathVariable int itemId,
      @RequestBody Map<String, Integer> requestBody
  ) {
    int quantity = requestBody.get("quantity");

    try {
      orderService.updateOrderItemQuantity(orderId, itemId, quantity);
      return ResponseEntity.ok("Order item quantity updated successfully.");
    } catch (OrderServiceException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
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

  @GetMapping("/{orderId}")
  public ResponseEntity<String> getOrderState(@PathVariable int orderId) throws OrderServiceException {
    String orderState = orderService.getOrderState(orderId);

    if (orderState != null) {
      return ResponseEntity.ok(orderState);
    }

    return ResponseEntity.notFound().build();
  }


  @PutMapping("/{orderId}/cancel")
  public ResponseEntity<String> cancelOrder(@PathVariable int orderId) throws OrderServiceException {
    orderService.cancelOrder(orderId);
    return ResponseEntity.ok("Order cancelled successfully.");
  }

  @PutMapping("/{orderId" +
      "" +
      "}/close")
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

  @GetMapping("/closed/{userId}")
  public List<ClosedOrderDTO> getClosedOrdersForCustomer(@PathVariable int userId) throws OrderServiceException {
    return orderService.getClosedOrdersForCustomer(userId);
  }

  @GetMapping("/in_progress/{orderId}")
  public ResponseEntity<OrderDTO> getInProgressOrderForCustomer(@PathVariable int orderId) throws OrderServiceException {
    OrderDTO orderDTO = orderService.getInProgressOrderForCustomer(orderId);
    return ResponseEntity.ok(orderDTO);
  }
}