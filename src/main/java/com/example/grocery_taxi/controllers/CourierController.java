// CourierController.java
package com.example.grocery_taxi.controllers;

import com.example.grocery_taxi.dto.OrderDTO;
import com.example.grocery_taxi.exception.OrderServiceException;
import com.example.grocery_taxi.service.CourierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

  public ResponseEntity<List<OrderDTO>> getOpenOrders() {
    List<OrderDTO> openOrders = courierService.getOpenOrders();
    return ResponseEntity.ok(openOrders);
  }

  @PostMapping("/orders/{orderId}/pickup")
  public ResponseEntity<Integer> pickUpOrder(@PathVariable int orderId) throws OrderServiceException {
    int pickedOrderId = courierService.pickUpOrder(orderId);
    return ResponseEntity.ok(pickedOrderId );
  }

}