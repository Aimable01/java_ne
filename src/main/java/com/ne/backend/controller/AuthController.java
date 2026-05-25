package com.ne.backend.controller;

import com.ne.backend.dto.*;
import com.ne.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register new user")
    @PostMapping("/register")
    public ApiResponse<?> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        authService.register(request);

        return ApiResponse.builder()
                .success(true)
                .message("User registered successfully")
                .data(null)
                .build();
    }

    @Operation(summary = "Login user")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {

        return ApiResponse.<LoginResponse>builder()
                .success(true)
                .message("Login successful")
                .data(authService.login(request))
                .build();
    }
}
