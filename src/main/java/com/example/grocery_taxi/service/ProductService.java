package com.example.grocery_taxi.service;

import com.example.grocery_taxi.entity.Product;
import com.example.grocery_taxi.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductService {

  private final ProductRepository productRepository;

  @Autowired
  public ProductService(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  public List<Product> getAllProducts() {
    return productRepository.findAll();
  }

  public int getAvailableQuantity(Product product) {
    return product.getAvailableQuantity();
  }

  public Product getProductById(Long productId) {
    return productRepository.findById(productId)
        .orElseThrow(() -> new IllegalArgumentException("Product not found"));
  }


}
