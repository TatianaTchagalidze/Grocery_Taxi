package com.example.grocery_taxi.service;

import com.example.grocery_taxi.controllers.AddOrderItemRequest;
import com.example.grocery_taxi.dto.ClosedOrderDTO;
import com.example.grocery_taxi.dto.OrderDTO;
import com.example.grocery_taxi.dto.OrderItemDTO;
import com.example.grocery_taxi.dto.RegistrationResponseDto;
import com.example.grocery_taxi.entity.Order;
import com.example.grocery_taxi.entity.OrderItem;
import com.example.grocery_taxi.entity.Product;
import com.example.grocery_taxi.entity.User;
import com.example.grocery_taxi.exception.OrderServiceException;
import com.example.grocery_taxi.model.OrderState;
import com.example.grocery_taxi.model.UserRole;
import com.example.grocery_taxi.repository.OrderItemRepository;
import com.example.grocery_taxi.repository.OrderRepository;
import com.example.grocery_taxi.repository.ProductRepository;
import com.example.grocery_taxi.repository.UserRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
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
  private final OrderItemRepository orderItemRepository;

  @Autowired
  public OrderService(OrderRepository orderRepository, ProductRepository productRepository,
                      UserRepository userRepository, OrderItemRepository orderItemRepository) {
    this.orderRepository = orderRepository;
    this.productRepository = productRepository;
    this.userRepository = userRepository;
    this.orderItemRepository = orderItemRepository;

  }

  public OrderDTO createOrder(int userId, List<AddOrderItemRequest> requestList)
      throws OrderServiceException {
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
    order = orderRepository.save(order);

    List<OrderItemDTO> orderItemDTOs = new ArrayList<>();

    for (AddOrderItemRequest request : requestList) {
      OrderItem orderItem =
          addOrderItem(order.getId(), request.getProductId(), request.getQuantity());
      OrderItemDTO orderItemDTO = new OrderItemDTO(
          orderItem.getId(),
          orderItem.getProduct().getName(),
          orderItem.getQuantity(),
          orderItem.getAmount()
      );
      orderItemDTOs.add(orderItemDTO);
    }

    return new OrderDTO(order, orderItemDTOs);
  }


  public OrderItem addOrderItem(int orderId, int productId, int quantity)
      throws OrderServiceException {
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
      throw new OrderServiceException(
          "Requested quantity exceeds available quantity for product: " + product.getName());
    }

    OrderItem orderItem = new OrderItem();
    orderItem.setOrder(order);
    orderItem.setProduct(product);
    orderItem.setQuantity(quantity);

    BigDecimal productPrice = product.getPrice();
    BigDecimal amount = productPrice != null ? productPrice.multiply(BigDecimal.valueOf(quantity)) :
        BigDecimal.ZERO;
    orderItem.setAmount(amount);

    order.getOrderItems().add(orderItem);

    updateOrderAmounts(order);

    orderRepository.save(order);

    return orderItem;
  }

  public void updateOrderItemQuantity(int orderId, int itemId, int newQuantity)
      throws OrderServiceException {
    Order order = getOrderById(orderId);

    Optional<OrderItem> optionalOrderItem = order.getOrderItems().stream()
        .filter(orderItem -> orderItem.getId() == itemId)
        .findFirst();

    if (optionalOrderItem.isEmpty()) {
      throw new OrderServiceException("Invalid order item: " + itemId);
    }

    OrderItem orderItem = optionalOrderItem.get();

    // Get the product associated with the order item
    Product product = orderItem.getProduct();

    // Check if the new quantity exceeds the available quantity
    int availableQuantity = product.getAvailableQuantity();
    if (newQuantity > availableQuantity) {
      throw new OrderServiceException(
          "Requested quantity exceeds available quantity for product: " + product.getName());
    }

    // Calculate the updated amount based on the new quantity
    BigDecimal productPrice = product.getPrice();
    BigDecimal newAmount =
        productPrice != null ? productPrice.multiply(BigDecimal.valueOf(newQuantity)) :
            BigDecimal.ZERO;

    // Update the order item quantity and amount
    orderItem.setQuantity(newQuantity);
    orderItem.setAmount(newAmount);

    // Update the total order amount
    updateOrderAmounts(order);

    // Save the changes to the order
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

    orderRepository.save(order);
  }

  private void updateOrderAmounts(Order order) {
    BigDecimal totalAmount = BigDecimal.ZERO;

    for (OrderItem orderItem : order.getOrderItems()) {
      BigDecimal productPrice = orderItem.getProduct().getPrice();
      int quantity = orderItem.getQuantity();
      BigDecimal amount =
          productPrice != null ? productPrice.multiply(BigDecimal.valueOf(quantity)) :
              BigDecimal.ZERO;
      orderItem.setAmount(amount);
      totalAmount = totalAmount.add(amount);
    }

    order.setTotalAmount(totalAmount);
  }


  public List<ClosedOrderDTO> getClosedOrdersForCustomer(int userId) throws OrderServiceException {
    Optional<User> optionalUser = userRepository.findById(userId);
    if (optionalUser.isEmpty()) {
      throw new OrderServiceException("User not found with ID: " + userId);
    }

    User customer = optionalUser.get();

    List<Order> allOrders = orderRepository.findAll(); // Retrieve all orders
    List<Order> closedOrders = allOrders.stream()
        .filter(order -> order.getUser().equals(customer)) // Filter orders for the specific customer
        .filter(order -> order.getState() == OrderState.CLOSED) // Filter closed orders
        .collect(Collectors.toList()); // Collect the filtered orders into a list

    List<ClosedOrderDTO> closedOrderDTOs = new ArrayList<>();

    for (Order order : closedOrders) {
      List<OrderItemDTO> orderItems = order.getOrderItems().stream()
          .map(orderItem -> new OrderItemDTO(
              orderItem.getId(),
              orderItem.getProduct().getName(),
              orderItem.getQuantity(),
              orderItem.getAmount()
          ))
          .collect(Collectors.toList());

      ClosedOrderDTO closedOrderDTO = new ClosedOrderDTO(
          order.getId(),
          order.getOrderState(),
          order.getTotalAmount(),
          new RegistrationResponseDto(order.getUserDto(), order.getUserId()),
          orderItems
      );
      closedOrderDTOs.add(closedOrderDTO);
    }

    return closedOrderDTOs;
  }

  public List<Integer> getOrderItemIds(int orderId) {
    List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
    List<Integer> itemIds = orderItems.stream()
        .map(OrderItem::getId)
        .collect(Collectors.toList());
    return itemIds;
  }


  public OrderDTO getInProgressOrderForCustomer(int orderId) throws OrderServiceException {
    Optional<Order> optionalOrder = orderRepository.findById(orderId);
    if (optionalOrder.isEmpty()) {
      throw new OrderServiceException("Order not found with ID: " + orderId);
    }

    Order order = optionalOrder.get();

    if (order.getState() != OrderState.IN_PROGRESS) {
      throw new OrderServiceException("The order with ID " + orderId + " is not in progress.");
    }

    List<OrderItemDTO> orderItems = new ArrayList<>(); // Empty list of OrderItemDTO

    return new OrderDTO(order, orderItems);
  }
}