package com.example.grocery_taxi.controllers;

import lombok.Data;

@Data
public class AddOrderItemRequest {
  private int productId;
  private int quantity;
}
