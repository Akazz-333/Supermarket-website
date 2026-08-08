package com.supermarket.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supermarket.inventory.dto.req.ProductRequest;
import com.supermarket.inventory.dto.resp.CategoryResponse;
import com.supermarket.inventory.dto.resp.ProductResponse;
import com.supermarket.inventory.security.CustomUserDetailsService;
import com.supermarket.inventory.security.JwtTokenProvider;
import com.supermarket.inventory.service.ProductService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private ProductResponse productResponse;
    private ProductRequest productRequest;

    @BeforeEach
    void setUp() {
        CategoryResponse categoryResp = new CategoryResponse(1L, "Dairy", "Dairy items", LocalDateTime.now(), LocalDateTime.now());

        productResponse = new ProductResponse(
                1L,
                "SKU-MILK01",
                "Organic Milk",
                "Fresh milk",
                new BigDecimal("4.99"),
                30,
                5,
                false,
                categoryResp,
                "Supplier",
                "123",
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        productRequest = new ProductRequest(
                "SKU-MILK01",
                "Organic Milk",
                "Fresh milk",
                new BigDecimal("4.99"),
                30,
                5,
                1L,
                "Supplier",
                "123"
        );
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void getProductById_Success() throws Exception {
        when(productService.getProductById(1L)).thenReturn(productResponse);

        mockMvc.perform(get("/api/v1/products/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sku").value("SKU-MILK01"))
                .andExpect(jsonPath("$.data.name").value("Organic Milk"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void createProduct_Success() throws Exception {
        when(productService.createProduct(any(ProductRequest.class))).thenReturn(productResponse);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sku").value("SKU-MILK01"));
    }
}
