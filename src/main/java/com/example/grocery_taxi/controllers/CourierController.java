// CourierController.java
package com.example.grocery_taxi.controllers;

import com.example.grocery_taxi.entity.Order;
import com.example.grocery_taxi.exception.OrderServiceException;
import com.example.grocery_taxi.service.CourierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/couriers")
public class CourierController {

  private final CourierService courierService;

  @Autowired
  public CourierController(CourierService courierService) {
    this.courierService = courierService;
  }

  @GetMapping("/orders/open")
  @PreAuthorize("hasRole('ROLE_COURIER')")
  public ResponseEntity<List<Order>> getOpenOrders() {
    List<Order> openOrders = courierService.getOpenOrders();
    return ResponseEntity.ok(openOrders);
  }

  @PostMapping("/orders/{orderId}/pickup")
  public ResponseEntity<String> pickUpOrder(@PathVariable int orderId) throws OrderServiceException {
    courierService.pickUpOrder(orderId);
    return ResponseEntity.ok("Order picked up successfully.");
  }
}

