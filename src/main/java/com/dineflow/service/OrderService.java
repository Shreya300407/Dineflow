package com.dineflow.service;

import com.dineflow.dto.OrderItemRequest;
import com.dineflow.dto.OrderRequest;
import com.dineflow.model.FoodItem;
import com.dineflow.model.Order;
import com.dineflow.model.OrderItem;
import com.dineflow.repository.FoodItemRepository;
import com.dineflow.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    public static final double STANDARD_DELIVERY_FEE = 40.0;

    private final OrderRepository orderRepository;
    private final FoodItemRepository foodItemRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository, FoodItemRepository foodItemRepository) {
        this.orderRepository = orderRepository;
        this.foodItemRepository = foodItemRepository;
    }

    /**
     * Requirement 7 & 8: Place an order calculating prices and totals on the backend
     * using trusted database prices, and saving order and items transactionally.
     */
    @Transactional
    public Order placeOrder(OrderRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Order request cannot be null");
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart cannot be empty. Please add items to place an order.");
        }

        // Generate unique order number (e.g., DF-20261025-A1B2C3)
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String orderNumber = "DF-" + datePart + "-" + randomPart;

        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setCustomerName(request.getCustomerName().trim());
        order.setCustomerPhone(request.getCustomerPhone().trim());
        order.setDeliveryAddress(request.getDeliveryAddress().trim());
        order.setDeliveryInstructions(request.getDeliveryInstructions() != null ? request.getDeliveryInstructions().trim() : null);
        order.setStatus(com.dineflow.model.OrderStatus.PLACED);
        order.setCreatedAt(LocalDateTime.now());

        double calculatedSubtotal = 0.0;

        for (OrderItemRequest itemReq : request.getItems()) {
            if (itemReq.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity for item ID " + itemReq.getFoodItemId() + " must be at least 1");
            }

            FoodItem foodItem = foodItemRepository.findById(itemReq.getFoodItemId())
                    .orElseThrow(() -> new IllegalArgumentException("Food item with ID " + itemReq.getFoodItemId() + " was not found in catalog"));

            // Use trusted database price, not frontend submitted price
            double authoritativePrice = foodItem.getPrice();
            double lineSubtotal = authoritativePrice * itemReq.getQuantity();
            calculatedSubtotal += lineSubtotal;

            OrderItem orderItem = new OrderItem(
                    foodItem,
                    foodItem.getName(),
                    authoritativePrice,
                    itemReq.getQuantity(),
                    lineSubtotal
            );

            order.addItem(orderItem);
        }

        double deliveryFee = STANDARD_DELIVERY_FEE;
        double totalAmount = calculatedSubtotal + deliveryFee;

        order.setSubtotal(calculatedSubtotal);
        order.setDeliveryFee(deliveryFee);
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);
        log.info("Order {} successfully placed for customer '{}' with {} items. Total: ₹{}", 
                savedOrder.getOrderNumber(), savedOrder.getCustomerName(), savedOrder.getItems().size(), savedOrder.getTotalAmount());

        return savedOrder;
    }

    public Optional<Order> getOrderById(Long id) {
        if (id == null) return Optional.empty();
        return orderRepository.findById(id);
    }

    public Optional<Order> getOrderByOrderNumber(String orderNumber) {
        if (orderNumber == null || orderNumber.trim().isEmpty()) {
            return Optional.empty();
        }
        return orderRepository.findByOrderNumber(orderNumber.trim().toUpperCase());
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Requirement 4, 5, 7: Update order status with transition validation.
     */
    @Transactional
    public Order updateOrderStatus(Long orderId, com.dineflow.model.OrderStatus newStatus) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("New order status cannot be null");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order with ID " + orderId + " was not found"));

        return applyStatusTransition(order, newStatus);
    }

    /**
     * Requirement 4, 5, 7: Update order status by public alphanumeric order number.
     */
    @Transactional
    public Order updateOrderStatusByOrderNumber(String orderNumber, com.dineflow.model.OrderStatus newStatus) {
        if (orderNumber == null || orderNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Order number cannot be empty");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("New order status cannot be null");
        }

        Order order = orderRepository.findByOrderNumber(orderNumber.trim().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Order with number '" + orderNumber + "' was not found"));

        return applyStatusTransition(order, newStatus);
    }

    private Order applyStatusTransition(Order order, com.dineflow.model.OrderStatus newStatus) {
        com.dineflow.model.OrderStatus currentStatus = order.getStatus();

        if (currentStatus == newStatus) {
            return order; // Idempotent update
        }

        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new IllegalStateException("Invalid status transition: Cannot change order status from " 
                    + currentStatus + " to " + newStatus);
        }

        order.setStatus(newStatus);
        Order updated = orderRepository.save(order);
        log.info("Order {} status updated from {} to {}", order.getOrderNumber(), currentStatus, newStatus);
        return updated;
    }
}
