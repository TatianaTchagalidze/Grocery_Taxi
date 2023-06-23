package com.example.grocery_taxi.service;

import com.example.grocery_taxi.entity.Order;
import com.example.grocery_taxi.entity.OrderItem;
import com.example.grocery_taxi.entity.Product;
import com.example.grocery_taxi.exception.OrderItemServiceException;
import com.example.grocery_taxi.repository.OrderItemRepository;
import com.example.grocery_taxi.repository.OrderRepository;
import com.example.grocery_taxi.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class OrderItemService {

  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;
  private final ProductRepository productRepository;

  @Autowired
  public OrderItemService(OrderRepository orderRepository, OrderItemRepository orderItemRepository, ProductRepository productRepository) {
    this.orderRepository = orderRepository;
    this.orderItemRepository = orderItemRepository;
    this.productRepository = productRepository;
  }

  @Transactional
  public void addOrderItem(Order order, Long productId, int quantity) throws OrderItemServiceException {
    Optional<Product> optionalProduct = productRepository.findById(productId);
    if (optionalProduct.isEmpty()) {
      throw new OrderItemServiceException("Product not found with ID: " + productId);
    }

    Product product = optionalProduct.get();

    int availableQuantity = product.getAvailableQuantity();
    if (quantity > availableQuantity) {
      throw new OrderItemServiceException("Requested quantity exceeds available quantity for product: " + product.getName());
    }

    OrderItem orderItem = new OrderItem();
    orderItem.setOrder(order);
    orderItem.setProduct(product);
    orderItem.setQuantity(quantity);
    orderItem.setAmount(product.getPrice().multiply(BigDecimal.valueOf(quantity)));

    order.getOrderItems().add(orderItem);
    order.setTotalAmount(order.getTotalAmount().add(orderItem.getAmount()));

    orderRepository.save(order);
  }

  @Transactional
  public void removeOrderItem(Order order, OrderItem orderItem) throws OrderItemServiceException {
    if (!order.getOrderItems().contains(orderItem)) {
      throw new OrderItemServiceException("Invalid order item: " + orderItem.getId());
    }

    order.getOrderItems().remove(orderItem);
    order.setTotalAmount(order.getTotalAmount().subtract(orderItem.getAmount()));

    orderRepository.save(order);
    orderItemRepository.delete(orderItem);
  }

  public OrderItem getOrderItemById(Long itemId) throws OrderItemServiceException {
    Optional<OrderItem> optionalOrderItem = orderItemRepository.findById(itemId);
    if (optionalOrderItem.isEmpty()) {
      throw new OrderItemServiceException("Invalid order item ID: " + itemId);
    }
    return optionalOrderItem.get();
  }

}
