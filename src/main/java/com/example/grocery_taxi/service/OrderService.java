package com.example.grocery_taxi.service;

import com.example.grocery_taxi.entity.Order;
import com.example.grocery_taxi.entity.OrderItem;
import com.example.grocery_taxi.entity.Product;
import com.example.grocery_taxi.entity.User;
import com.example.grocery_taxi.exception.OrderServiceException;
import com.example.grocery_taxi.model.OrderState;
import com.example.grocery_taxi.model.UserRole;
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

    if (user.getRole() == UserRole.Courier) {
      throw new OrderServiceException("Couriers are not allowed to create orders.");
    }

    Order order = new Order();
    order.setUser(user);
    order.setState(OrderState.DRAFT);
    order.setEditable(true);
    order.setTotalAmount(BigDecimal.ZERO);

    return orderRepository.save(order);
  }


  public void addOrderItem(int orderId, int productId, int quantity) throws OrderServiceException {
    Optional<Order> optionalOrder = orderRepository.findById(orderId);
    if (optionalOrder.isEmpty()) {
      throw new OrderServiceException("Order not found with ID: " + orderId);
    }

    Order order = optionalOrder.get();

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

    updateOrderAmounts(order);

    orderRepository.save(order);
  }

  public void removeOrderItem(int orderId, int itemId) throws OrderServiceException {
    Optional<Order> optionalOrder = orderRepository.findById(orderId);
    if (optionalOrder.isEmpty()) {
      throw new OrderServiceException("Order not found with ID: " + orderId);
    }

    Order order = optionalOrder.get();

    Optional<OrderItem> optionalOrderItem = order.getOrderItems().stream()
        .filter(orderItem -> orderItem.getId() == itemId)
        .findFirst();

    if (optionalOrderItem.isEmpty()) {
      throw new OrderServiceException("Invalid order item: " + itemId);
    }

    OrderItem orderItem = optionalOrderItem.get();
    order.getOrderItems().remove(orderItem);

    updateOrderAmounts(order);

    orderRepository.save(order);
  }

  public void confirmOrder(int orderId) throws OrderServiceException {
    Optional<Order> optionalOrder = orderRepository.findById(orderId);
    if (optionalOrder.isEmpty()) {
      throw new OrderServiceException("Order not found with ID: " + orderId);
    }

    Order order = optionalOrder.get();

    if (order.getState() != OrderState.DRAFT) {
      throw new OrderServiceException("Cannot confirm an order that is not in the DRAFT state.");
    }

    if (order.getOrderItems().isEmpty()) {
      throw new OrderServiceException("Cannot confirm an order without any items.");
    }

    updateOrderAmounts(order);

    order.setState(OrderState.OPEN);
    order.setEditable(false); // Disable editing of the order

    orderRepository.save(order);
  }

  public void cancelOrder(int orderId) throws OrderServiceException {
    Optional<Order> optionalOrder = orderRepository.findById(orderId);
    if (optionalOrder.isEmpty()) {
      throw new OrderServiceException("Order not found with ID: " + orderId);
    }

    Order order = optionalOrder.get();

    if (order.getState() != OrderState.DRAFT) {
      throw new OrderServiceException("Cannot cancel an order that is not in the DRAFT state.");
    }

    order.setState(OrderState.CANCELLED);
    order.setEditable(false); // Set the editable flag to false after cancellation

    orderRepository.save(order);
  }

  public void closeOrder(int orderId, UserRole userRole) throws OrderServiceException {
    Order order = getOrderById(orderId);

    if (userRole == UserRole.Customer) {
      closeOrderByCustomer(order);
    } else if (userRole == UserRole.Courier) {
      closeOrderByCourier(order);
    } else {
      throw new OrderServiceException("User does not have permission to close the order.");
    }
  }

  public void closeOrderByCustomer(Order order) throws OrderServiceException {
    if (order.getState() != OrderState.CONFIRMED && order.getState() != OrderState.IN_PROGRESS) {
      throw new OrderServiceException("Order cannot be closed by the customer at this state.");
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

  public Order getOrderById(int orderId) throws OrderServiceException {
    Optional<Order> optionalOrder = orderRepository.findById(orderId);
    if (optionalOrder.isEmpty()) {
      throw new OrderServiceException("Order not found with ID: " + orderId);
    }
    return optionalOrder.get();
  }
  public void reopenOrder(int orderId) throws OrderServiceException {
    Optional<Order> optionalOrder = orderRepository.findById(orderId);
    if (optionalOrder.isEmpty()) {
      throw new OrderServiceException("Order not found with ID: " + orderId);
    }

    Order order = optionalOrder.get();

    if (!order.isClosed()) {
      throw new OrderServiceException("Order is not closed. Cannot reopen.");
    }

    order.setClosed(false);
    order.setState(OrderState.OPEN);

    orderRepository.save(order);
  }

  private void updateOrderAmounts(Order order) {
    BigDecimal totalAmount = BigDecimal.ZERO;

    for (OrderItem orderItem : order.getOrderItems()) {
      BigDecimal productPrice = orderItem.getProduct().getPrice();
      int quantity = orderItem.getQuantity();
      BigDecimal amount = productPrice != null ? productPrice.multiply(BigDecimal.valueOf(quantity)) : BigDecimal.ZERO;
      orderItem.setAmount(amount);
      totalAmount = totalAmount.add(amount);
    }

    order.setTotalAmount(totalAmount);
  }
}
