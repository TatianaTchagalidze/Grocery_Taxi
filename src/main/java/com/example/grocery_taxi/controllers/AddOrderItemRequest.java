package com.example.grocery_taxi.controllers;

import lombok.Data;

@Data
public class AddOrderItemRequest {
  private Long productId;
  private int quantity;
}
