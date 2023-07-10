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
import java.util.List;
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

  public Order createOrder(int userId) throws OrderServiceException {
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

  public void addOrderItem(Order order, int productId, int quantity) throws OrderServiceException {
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

    updateTotalAmount(order); // Update the total amount

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
    int oldQuantity = orderItem.getQuantity();

    orderItem.setQuantity(quantity);

    BigDecimal productPrice = orderItem.getProduct().getPrice();
    BigDecimal newAmount = productPrice != null ? productPrice.multiply(BigDecimal.valueOf(quantity)) : BigDecimal.ZERO;
    orderItem.setAmount(newAmount);

    BigDecimal totalAmount = order.getTotalAmount();
    if (totalAmount != null && oldAmount != null) {
      // Subtract the old item amount and add the new item amount to the total amount
      totalAmount = totalAmount.subtract(oldAmount).add(newAmount);
    } else if (totalAmount == null && oldAmount != null) {
      totalAmount = newAmount;
    } else {
      totalAmount = BigDecimal.ZERO;
    }
    order.setTotalAmount(totalAmount);

    // Adjust the available quantity of the product
    int quantityDifference = oldQuantity - quantity;
    orderItem.getProduct().setAvailableQuantity(availableQuantity + quantityDifference);

    orderRepository.save(order); // Save the order entity
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

  public Order getOrderById(int orderId) throws OrderServiceException {
    Optional<Order> optionalOrder = orderRepository.findById(orderId);
    if (optionalOrder.isEmpty()) {
      throw new OrderServiceException("Order not found with ID: " + orderId);
    }
    return optionalOrder.get();
  }

  public void completeOrder(int orderId) throws OrderServiceException {
    Order order = getOrderById(orderId);

    // Check if the order is already completed
    if (order.getState()== OrderState.COMPLETED) {
      throw new OrderServiceException("Order is already completed.");
    }

    order.setState(OrderState.COMPLETED);

    orderRepository.save(order);
  }

  public void updateOrderTotalAmount(Order order, BigDecimal totalAmount) {
    order.setTotalAmount(totalAmount);
    orderRepository.save(order);
  }

  public List<Order> getOrdersByState(OrderState orderState) {
    return orderRepository.findByOrderState(orderState);
  }

  private void updateTotalAmount(Order order) {
    BigDecimal totalAmount = BigDecimal.ZERO;
    for (OrderItem orderItem : order.getOrderItems()) {
      BigDecimal amount = orderItem.getAmount();
      if (amount != null) {
        totalAmount = totalAmount.add(amount);
      }
    }
    order.setTotalAmount(totalAmount);
  }

  public void closeOrderByConsumer(Order order) throws OrderServiceException {
    if (order.getState() != OrderState.CONFIRMED && order.getState() != OrderState.IN_PROGRESS) {
      throw new OrderServiceException("Order cannot be closed by the consumer at this state.");
    }

    order.setClosed(true);
    order.setState(OrderState.CLOSED);

    orderRepository.save(order);
  }

  public void closeOrderByCourier(Order order) throws OrderServiceException {
    if (order.getState() != OrderState.IN_PROGRESS) {
      throw new OrderServiceException("Order cannot be closed by the courier at this state.");
    }

    order.setClosed(true);
    order.setState(OrderState.CLOSED);

    orderRepository.save(order);
  }

  public void reopenOrder(Order order) throws OrderServiceException {
    if (!order.isClosed()) {
      throw new OrderServiceException("Order is not closed. Cannot reopen.");
    }

    order.setClosed(false);
    order.setState(OrderState.OPEN);

    orderRepository.save(order);
  }

}

