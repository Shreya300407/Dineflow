package com.dineflow.controller;

import com.dineflow.dto.OrderRequest;
import com.dineflow.dto.OrderResponse;
import com.dineflow.model.Order;
import com.dineflow.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Requirement 6 & 8: Place a new food order.
     * Prices and totals are computed strictly on the backend from MySQL.
     */
    @PostMapping
    public ResponseEntity<?> placeOrder(@Valid @RequestBody OrderRequest request) {
        try {
            Order order = orderService.placeOrder(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.fromEntity(order));
        } catch (IllegalArgumentException ex) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Bad Request");
            error.put("message", ex.getMessage());
            error.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception ex) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Internal Server Error");
            error.put("message", "An unexpected error occurred while placing your order. Please try again.");
            error.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Requirement 4 & 6: Retrieve all orders (for Order History page).
     */
    @GetMapping
    public List<OrderResponse> getAllOrders() {
        return orderService.getAllOrders().stream()
                .map(OrderResponse::fromEntity)
                .toList();
    }

    /**
     * GET /api/orders/{id}
     * Retrieve order details by internal database ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        Optional<Order> orderOpt = orderService.getOrderById(id);
        if (orderOpt.isPresent()) {
            return ResponseEntity.ok(OrderResponse.fromEntity(orderOpt.get()));
        }

        Map<String, Object> error = new HashMap<>();
        error.put("error", "Not Found");
        error.put("message", "Order with ID " + id + " was not found.");
        error.put("status", HttpStatus.NOT_FOUND.value());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * GET /api/orders/number/{orderNumber}
     * Retrieve order details by public alphanumeric order number (e.g., DF-20261025-A1B2C3).
     */
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<?> getOrderByOrderNumber(@PathVariable String orderNumber) {
        Optional<Order> orderOpt = orderService.getOrderByOrderNumber(orderNumber);
        if (orderOpt.isPresent()) {
            return ResponseEntity.ok(OrderResponse.fromEntity(orderOpt.get()));
        }

        Map<String, Object> error = new HashMap<>();
        error.put("error", "Not Found");
        error.put("message", "Order with number '" + orderNumber + "' was not found.");
        error.put("status", HttpStatus.NOT_FOUND.value());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Requirement 4 & 7: Update order status by ID with transition validation.
     */
    @RequestMapping(value = "/{id}/status", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, 
                                               @Valid @RequestBody com.dineflow.dto.OrderStatusUpdateRequest request) {
        Order updated = orderService.updateOrderStatus(id, request.getStatus());
        return ResponseEntity.ok(OrderResponse.fromEntity(updated));
    }

    /**
     * Requirement 4 & 7: Update order status by order number with transition validation.
     */
    @RequestMapping(value = "/number/{orderNumber}/status", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<?> updateOrderStatusByNumber(@PathVariable String orderNumber, 
                                                       @Valid @RequestBody com.dineflow.dto.OrderStatusUpdateRequest request) {
        Order updated = orderService.updateOrderStatusByOrderNumber(orderNumber, request.getStatus());
        return ResponseEntity.ok(OrderResponse.fromEntity(updated));
    }

    /**
     * Handle invalid status transition rules.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalStateException(IllegalStateException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Invalid Status Transition");
        error.put("message", ex.getMessage());
        error.put("status", HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handle malformed JSON or unparseable enum status strings.
     */
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadable(org.springframework.http.converter.HttpMessageNotReadableException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Bad Request");
        error.put("message", "Invalid order status value provided. Allowed values: PLACED, CONFIRMED, PREPARING, OUT_FOR_DELIVERY, DELIVERED, CANCELLED.");
        error.put("status", HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Clean validation error response for @Valid annotated requests.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("error", "Validation Failed");
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("errors", fieldErrors);
        response.put("message", "Please fix the invalid fields and try again.");
        return response;
    }
}
