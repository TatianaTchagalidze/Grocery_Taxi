package com.example.grocery_taxi.service;

import com.example.grocery_taxi.entity.Order;
import com.example.grocery_taxi.entity.OrderItem;
import com.example.grocery_taxi.entity.Product;
import com.example.grocery_taxi.entity.User;
import com.example.grocery_taxi.exception.OrderServiceException;
import com.example.grocery_taxi.model.OrderState;
import com.example.grocery_taxi.repository.OrderItemRepository;
import com.example.grocery_taxi.repository.OrderRepository;

import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
public class OrderService {
  private final OrderRepository orderRepository;
  private final ProductService productService;

  private final OrderItemRepository orderItemRepository;

  public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository, ProductService productService) {
    this.orderRepository = orderRepository;
    this.orderItemRepository = orderItemRepository;
    this.productService = productService;
  }

  public Order createOrder(User user) {
    Order order = new Order();
    order.setUser(user);
    order.setState(OrderState.DRAFT);
    order.setTotalAmount(BigDecimal.ZERO);
    order.setEditable(true); // Set the initial state as editable
    return orderRepository.save(order);
  }

  public void addOrderItem(Order order, Product product, int quantity) {
    int availableQuantity = productService.getAvailableQuantity(product);
    if (quantity > availableQuantity) {
      quantity = availableQuantity;
    }

    OrderItem orderItem = new OrderItem();
    orderItem.setOrder(order);
    orderItem.setProduct(product);
    orderItem.setQuantity(quantity);
    orderItem.setAmount(product.getPrice().multiply(BigDecimal.valueOf(quantity)));

    order.getOrderItems().add(orderItem);
    order.setTotalAmount(order.getTotalAmount().add(orderItem.getAmount()));
  }

  public void updateOrderItemQuantity(OrderItem orderItem, int quantity) throws OrderServiceException {
    Order order = orderItem.getOrder();

    if (!order.isEditable()) {
      throw new OrderServiceException("Order is not editable. Cannot update order item quantity.");
    }

    int availableQuantity = productService.getAvailableQuantity(orderItem.getProduct());
    if (quantity > availableQuantity) {
      quantity = availableQuantity;
    }

    orderItem.setQuantity(quantity);
    orderItem.setAmount(orderItem.getProduct().getPrice().multiply(BigDecimal.valueOf(quantity)));

    order.setTotalAmount(order.getTotalAmount()
        .subtract(orderItem.getAmount())
        .add(orderItem.getAmount()));
  }

  public void confirmOrder(Long orderId) throws OrderServiceException {
    Order order = getOrderById(orderId);

    if (order.getState() == OrderState.CONFIRMED) {
      throw new OrderServiceException("Order is already confirmed.");
    }

    if (order.getState() == OrderState.CANCELLED) {
      throw new OrderServiceException("Cannot confirm a cancelled order.");
    }
    if (order.getState() != OrderState.OPEN) {
      throw new OrderServiceException("Cannot confirm an order that is not in the OPEN state.");
    }
    order.setState(OrderState.CONFIRMED);
    order.setEditable(false); // Disable editing of the order

    orderRepository.save(order);
  }

  public void completeOrder(Long orderId) throws OrderServiceException {
    Order order = getOrderById(orderId);

    if (order.getState() == OrderState.DRAFT) {
      throw new OrderServiceException("Cannot complete a draft order.");
    }

    if (order.getState() == OrderState.CANCELLED) {
      throw new OrderServiceException("Cannot complete a cancelled order.");
    }

    if (order.getState() != OrderState.CONFIRMED) {
      throw new OrderServiceException("Cannot complete an order that is not confirmed.");
    }

    order.setState(OrderState.COMPLETED);
  }


  @Transactional
  public void cancelOrder(Order order) throws OrderServiceException {
    if (order.getState() == OrderState.CANCELLED) {
      throw new OrderServiceException("Order is already cancelled.");
    }

    if (order.getState() == OrderState.OPEN) {
      throw new OrderServiceException("Cannot cancel an already confirmed order.");
    }

    order.setState(OrderState.CANCELLED);
    order.setEditable(false); // Set the editable flag to false after cancellation

    orderRepository.save(order);
  }

  public Order getOrderById(Long orderId) throws OrderServiceException {
    return orderRepository.findById(orderId)
        .orElseThrow(() -> new OrderServiceException("Order not found."));
  }
  public OrderItem getOrderItemById(Long itemId) throws OrderServiceException {
    Optional<OrderItem> orderItemOptional = orderItemRepository.findById(itemId);
    if (orderItemOptional.isPresent()) {
      return orderItemOptional.get();
    } else {
      throw new OrderServiceException("Order item not found.");
    }
  }
}

