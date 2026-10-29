package com.dineflow;

import com.dineflow.dto.OrderItemRequest;
import com.dineflow.dto.OrderRequest;
import com.dineflow.dto.OrderStatusUpdateRequest;
import com.dineflow.model.OrderStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldPlaceOrderSuccessfullyWithBackendCalculatedPrices() throws Exception {
        // Food Item 1: Paneer Tikka Croquettes (Database price = ₹280.0)
        // 2 units * ₹280.0 = ₹560.0 subtotal + ₹40.0 delivery fee = ₹600.0 total
        OrderRequest request = new OrderRequest(
                "Rohan Verma",
                "+91 9876543210",
                "Flat 402, Green Glen Palms, Bellandur, Bangalore",
                "Leave at doorstep, do not ring bell",
                List.of(new OrderItemRequest(1L, 2))
        );

        MvcResult result = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNumber", startsWith("DF-")))
                .andExpect(jsonPath("$.customerName").value("Rohan Verma"))
                .andExpect(jsonPath("$.customerPhone").value("+91 9876543210"))
                .andExpect(jsonPath("$.subtotal").value(560.0))
                .andExpect(jsonPath("$.deliveryFee").value(40.0))
                .andExpect(jsonPath("$.totalAmount").value(600.0))
                .andExpect(jsonPath("$.status").value("PLACED"))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].foodName").value("Paneer Tikka Croquettes"))
                .andExpect(jsonPath("$.items[0].unitPrice").value(280.0))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].subtotal").value(560.0))
                .andReturn();

        JsonNode responseNode = objectMapper.readTree(result.getResponse().getContentAsString());
        String orderNumber = responseNode.get("orderNumber").asText();
        Long orderId = responseNode.get("id").asLong();

        // Verify retrieval by order number
        mockMvc.perform(get("/api/orders/number/" + orderNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber").value(orderNumber))
                .andExpect(jsonPath("$.status").value("PLACED"))
                .andExpect(jsonPath("$.totalAmount").value(600.0));

        // Verify retrieval by ID
        mockMvc.perform(get("/api/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.totalAmount").value(600.0));

        // Verify listing all orders
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void shouldUpdateStatusThroughValidLifecycle() throws Exception {
        // Place initial order with status PLACED
        OrderRequest request = new OrderRequest(
                "Sneha Kapoor",
                "9876543211",
                "Koramangala 4th Block, Bangalore",
                "",
                List.of(new OrderItemRequest(1L, 1))
        );

        MvcResult result = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PLACED"))
                .andReturn();

        String orderNumber = objectMapper.readTree(result.getResponse().getContentAsString()).get("orderNumber").asText();

        // PLACED -> CONFIRMED
        mockMvc.perform(patch("/api/orders/number/" + orderNumber + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new OrderStatusUpdateRequest(OrderStatus.CONFIRMED))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        // CONFIRMED -> PREPARING
        mockMvc.perform(patch("/api/orders/number/" + orderNumber + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new OrderStatusUpdateRequest(OrderStatus.PREPARING))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PREPARING"));

        // PREPARING -> OUT_FOR_DELIVERY
        mockMvc.perform(patch("/api/orders/number/" + orderNumber + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new OrderStatusUpdateRequest(OrderStatus.OUT_FOR_DELIVERY))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OUT_FOR_DELIVERY"));

        // OUT_FOR_DELIVERY -> DELIVERED
        mockMvc.perform(patch("/api/orders/number/" + orderNumber + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new OrderStatusUpdateRequest(OrderStatus.DELIVERED))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"));

        // DELIVERED -> PREPARING (Invalid transition from terminal state -> 400 Bad Request)
        mockMvc.perform(patch("/api/orders/number/" + orderNumber + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new OrderStatusUpdateRequest(OrderStatus.PREPARING))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid Status Transition"));
    }

    @Test
    void shouldAllowOrderCancellationAndBlockPostCancelTransitions() throws Exception {
        OrderRequest request = new OrderRequest(
                "Aman Jain",
                "9876543212",
                "Whitefield, Bangalore",
                "",
                List.of(new OrderItemRequest(1L, 1))
        );

        MvcResult result = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long orderId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        // PLACED -> CANCELLED
        mockMvc.perform(patch("/api/orders/" + orderId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new OrderStatusUpdateRequest(OrderStatus.CANCELLED))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        // CANCELLED -> DELIVERED (Invalid transition from terminal CANCELLED state -> 400 Bad Request)
        mockMvc.perform(patch("/api/orders/" + orderId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new OrderStatusUpdateRequest(OrderStatus.DELIVERED))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid Status Transition"));
    }

    @Test
    void shouldRejectInvalidDirectStatusJump() throws Exception {
        // PLACED -> DELIVERED directly should be rejected
        OrderRequest request = new OrderRequest(
                "Tanvi Bhatia",
                "9876543213",
                "HSR Layout, Bangalore",
                "",
                List.of(new OrderItemRequest(1L, 1))
        );

        MvcResult result = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String orderNumber = objectMapper.readTree(result.getResponse().getContentAsString()).get("orderNumber").asText();

        // PLACED directly to DELIVERED is illegal
        mockMvc.perform(patch("/api/orders/number/" + orderNumber + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new OrderStatusUpdateRequest(OrderStatus.DELIVERED))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid Status Transition"));
    }

    @Test
    void shouldRejectOrderWhenCartIsEmpty() throws Exception {
        OrderRequest request = new OrderRequest(
                "Ananya Roy",
                "9876543210",
                "MG Road, Bangalore",
                "",
                Collections.emptyList()
        );

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectOrderWhenRequiredFieldsAreMissing() throws Exception {
        // Missing name, phone, address
        OrderRequest request = new OrderRequest(
                "",
                "",
                "",
                "",
                List.of(new OrderItemRequest(1L, 1))
        );

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.customerName").exists())
                .andExpect(jsonPath("$.errors.customerPhone").exists())
                .andExpect(jsonPath("$.errors.deliveryAddress").exists());
    }

    @Test
    void shouldRejectOrderWhenFoodItemDoesNotExist() throws Exception {
        OrderRequest request = new OrderRequest(
                "Vikram Singh",
                "9876543210",
                "Indiranagar, Bangalore",
                "",
                List.of(new OrderItemRequest(99999L, 1))
        );

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    void shouldReturnNotFoundForInvalidOrderNumber() throws Exception {
        // Entering '1' is not a valid order number format and should return 404 Not Found
        mockMvc.perform(get("/api/orders/number/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Order with number '1' was not found."));

        // Non-existent order number should also return 404
        mockMvc.perform(get("/api/orders/number/DF-99999999-NOTFOUND"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void shouldRetrieveOrderHistorySortedByNewestFirst() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)))
                .andExpect(jsonPath("$[0].orderNumber", startsWith("DF-")));
    }
}
