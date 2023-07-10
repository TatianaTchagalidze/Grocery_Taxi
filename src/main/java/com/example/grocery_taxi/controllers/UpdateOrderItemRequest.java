package com.example.grocery_taxi.controllers;

import lombok.Data;

@Data
public class UpdateOrderItemRequest {
  private int quantity;
  private int productId;
}
