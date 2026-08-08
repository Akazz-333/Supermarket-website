package com.supermarket.inventory.controller;

import com.supermarket.inventory.dto.resp.ApiResponse;
import com.supermarket.inventory.dto.resp.DashboardSummaryResponse;
import com.supermarket.inventory.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard & Analytics", description = "Real-time supermarket metrics, stock alerts, order status counts, and revenue insights")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get executive dashboard analytics summary", description = "Allowed for ADMIN and MANAGER roles. Provides real-time metrics, low-stock alerts, revenue, and order breakdown.")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getDashboardSummary() {
        DashboardSummaryResponse summary = dashboardService.getDashboardSummary();
        return ResponseEntity.ok(ApiResponse.success("Dashboard summary retrieved successfully", summary));
    }
}
