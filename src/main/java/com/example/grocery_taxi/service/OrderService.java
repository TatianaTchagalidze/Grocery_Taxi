package com.example.grocery_taxi.service;

import com.example.grocery_taxi.entity.Order;
import com.example.grocery_taxi.entity.OrderItem;
import com.example.grocery_taxi.entity.Product;
import com.example.grocery_taxi.entity.User;
import com.example.grocery_taxi.exception.OrderServiceException;
import com.example.grocery_taxi.model.OrderState;
import com.example.grocery_taxi.repository.OrderRepository;
import com.example.grocery_taxi.repository.ProductRepository;
import com.example.grocery_taxi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@Transactional
public class OrderService {

  private final OrderRepository orderRepository;
  private final ProductRepository productRepository;
  private final UserRepository userRepository;

  @Autowired
  public OrderService(OrderRepository orderRepository, ProductRepository productRepository, UserRepository userRepository) {
    this.orderRepository = orderRepository;
    this.productRepository = productRepository;
    this.userRepository = userRepository;
  }

  public Order createOrder(Long userId) throws OrderServiceException {
    Optional<User> optionalUser = userRepository.findById(userId);
    if (optionalUser.isEmpty()) {
      throw new OrderServiceException("User not found with ID: " + userId);
    }

    User user = optionalUser.get();

    Order order = new Order();
    order.setUser(user);
    order.setState(OrderState.DRAFT);
    order.setTotalAmount(BigDecimal.ZERO);
    order.setEditable(true);

    return orderRepository.save(order);
  }

  public void addOrderItem(Order order, Long productId, int quantity) throws OrderServiceException {
    Optional<Product> optionalProduct = productRepository.findById(productId);
    if (optionalProduct.isEmpty()) {
      throw new OrderServiceException("Product not found with ID: " + productId);
    }

    Product product = optionalProduct.get();
    int availableQuantity = product.getAvailableQuantity();
    if (quantity > availableQuantity) {
      throw new OrderServiceException("Requested quantity exceeds available quantity for product: " + product.getName());
    }

    OrderItem orderItem = new OrderItem();
    orderItem.setOrder(order);
    orderItem.setProduct(product);
    orderItem.setQuantity(quantity);

    BigDecimal productPrice = product.getPrice();
    BigDecimal amount = productPrice != null ? productPrice.multiply(BigDecimal.valueOf(quantity)) : BigDecimal.ZERO;
    orderItem.setAmount(amount);

    order.getOrderItems().add(orderItem);

    BigDecimal totalAmount = order.getTotalAmount();
    totalAmount = totalAmount != null ? totalAmount.add(amount) : amount;
    order.setTotalAmount(totalAmount);

    orderRepository.save(order);
  }


  public void updateOrderItemQuantity(OrderItem orderItem, int quantity) throws OrderServiceException {
    Order order = orderItem.getOrder();

    if (!order.isEditable()) {
      throw new OrderServiceException("Order is not editable. Cannot update order item quantity.");
    }

    int availableQuantity = orderItem.getProduct().getAvailableQuantity();
    if (quantity > availableQuantity) {
      throw new OrderServiceException("Requested quantity exceeds available quantity for product: " + orderItem.getProduct().getName());
    }

    BigDecimal oldAmount = orderItem.getAmount();

    orderItem.setQuantity(quantity);

    BigDecimal productPrice = orderItem.getProduct().getPrice();
    BigDecimal newAmount = productPrice != null ? productPrice.multiply(BigDecimal.valueOf(quantity)) : BigDecimal.ZERO;
    orderItem.setAmount(newAmount);

    BigDecimal totalAmount = order.getTotalAmount();
    if (totalAmount != null && oldAmount != null) {
      totalAmount = totalAmount.subtract(oldAmount).add(newAmount);
    } else if (totalAmount == null && oldAmount != null) {
      totalAmount = newAmount;
    } else {
      totalAmount = BigDecimal.ZERO;
    }
    order.setTotalAmount(totalAmount);

    orderRepository.save(order);
  }

  public void removeOrderItem(Order order, OrderItem orderItem) throws OrderServiceException {
    if (!order.getOrderItems().contains(orderItem)) {
      throw new OrderServiceException("Invalid order item: " + orderItem.getId());
    }

    BigDecimal amount = orderItem.getAmount();
    if (amount != null) {
      order.setTotalAmount(order.getTotalAmount().subtract(amount));
    }

    order.getOrderItems().remove(orderItem);

    orderRepository.save(order);
  }

  public void confirmOrder(Order order) throws OrderServiceException {
    if (order.getState() != OrderState.DRAFT) {
      throw new OrderServiceException("Cannot confirm an order that is not in the DRAFT state.");
    }

    if (order.getOrderItems().isEmpty()) {
      throw new OrderServiceException("Cannot confirm an order without any items.");
    }

    BigDecimal totalAmount = BigDecimal.ZERO;

    for (OrderItem orderItem : order.getOrderItems()) {
      BigDecimal amount = orderItem.getAmount();
      if (amount != null) {
        totalAmount = totalAmount.add(amount);
      }
    }

    order.setTotalAmount(totalAmount);
    order.setState(OrderState.OPEN);
    order.setEditable(false); // Disable editing of the order

    orderRepository.save(order);
  }

  public void cancelOrder(Order order) throws OrderServiceException {
    if (order.getState() != OrderState.DRAFT) {
      throw new OrderServiceException("Cannot cancel an order that is not in the DRAFT state.");
    }

    order.setState(OrderState.CANCELLED);
    order.setEditable(false); // Set the editable flag to false after cancellation

    orderRepository.save(order);
  }

  public Order getOrderById(Long orderId) throws OrderServiceException {
    Optional<Order> optionalOrder = orderRepository.findById(orderId);
    if (optionalOrder.isEmpty()) {
      throw new OrderServiceException("Order not found with ID: " + orderId);
    }
    return optionalOrder.get();
  }

  public void completeOrder(Long orderId) throws OrderServiceException {
    Order order = getOrderById(orderId);

    // Check if the order is already completed
    if (order.getState() == OrderState.COMPLETED) {
      throw new OrderServiceException("Order is already completed.");
    }

    order.setState(OrderState.COMPLETED);

    orderRepository.save(order);
  }
}
