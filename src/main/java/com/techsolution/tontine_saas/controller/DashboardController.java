package com.techsolution.tontine_saas.controller;

import com.techsolution.tontine_saas.dtos.response.DashboardStatsResponse;
import com.techsolution.tontine_saas.services.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Statistiques globales de l'association")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Récupérer les statistiques financières en temps réel")
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardStatsResponse> getStats() {
        DashboardStatsResponse stats = dashboardService.getAssociationStats();
        return ResponseEntity.ok(stats);
    }

}
