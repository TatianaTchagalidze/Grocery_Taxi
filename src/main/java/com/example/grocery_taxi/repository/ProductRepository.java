package com.example.grocery_taxi.repository;

import com.example.grocery_taxi.entity.Product;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
  List<Product> findAll();
}



