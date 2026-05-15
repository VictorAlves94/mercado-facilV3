package com.mercadofacil.controller;

import com.mercadofacil.dto.response.DashboardResponse;
import com.mercadofacil.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/resumo")
    public ResponseEntity<DashboardResponse> getResumoHoje(
            @RequestParam(required = false) Long lojaId) {
        return ResponseEntity.ok(dashboardService.getResumoHoje(lojaId));
    }
}
