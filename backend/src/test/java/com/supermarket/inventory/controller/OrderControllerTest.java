package com.supermarket.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supermarket.inventory.dto.req.CreateOrderRequest;
import com.supermarket.inventory.dto.req.OrderItemRequest;
import com.supermarket.inventory.dto.req.OrderStatusUpdateRequest;
import com.supermarket.inventory.dto.resp.OrderItemResponse;
import com.supermarket.inventory.dto.resp.OrderResponse;
import com.supermarket.inventory.entity.OrderStatus;
import com.supermarket.inventory.security.CustomUserDetailsService;
import com.supermarket.inventory.security.JwtTokenProvider;
import com.supermarket.inventory.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        OrderItemResponse itemResp = new OrderItemResponse(1L, 10L, "SKU-MILK01", "Organic Milk", 2, new BigDecimal("4.99"), new BigDecimal("9.98"));

        orderResponse = new OrderResponse(
                100L,
                "ORD-20260808-99999",
                "Alice Smith",
                "alice@example.com",
                "1234567890",
                OrderStatus.PENDING,
                new BigDecimal("9.98"),
                "Leave at door",
                List.of(itemResp),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void createOrder_Success() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest(
                "Alice Smith",
                "alice@example.com",
                "1234567890",
                "Leave at door",
                List.of(new OrderItemRequest(10L, 2))
        );

        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(orderResponse);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderNumber").value("ORD-20260808-99999"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void updateOrderStatus_Success() throws Exception {
        orderResponse.setStatus(OrderStatus.CONFIRMED);
        OrderStatusUpdateRequest updateRequest = new OrderStatusUpdateRequest(OrderStatus.CONFIRMED);

        when(orderService.updateOrderStatus(eq(100L), any(OrderStatusUpdateRequest.class))).thenReturn(orderResponse);

        mockMvc.perform(patch("/api/v1/orders/100/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));
    }
}
