package com.mercadofacil.controller;

import com.mercadofacil.dto.request.AutorizarRequest;
import com.mercadofacil.dto.request.LoginRequest;
import com.mercadofacil.dto.response.AuthResponse;
import com.mercadofacil.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> me(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(authService.me(userDetails.getUsername()));
    }

    @PostMapping("/autorizar")
    public ResponseEntity<Void> autorizar(@RequestBody AutorizarRequest request) {
        authService.autorizar(request.email(), request.senha(), request.perfisPermitidos());
        return ResponseEntity.ok().build();
    }
}
