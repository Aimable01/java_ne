package com.ne.backend.controller;

import com.ne.backend.dto.*;
import com.ne.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication endpoints for user registration and login")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Register new user",
            description = "Creates a new user account with email and password"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User registered successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation failed or email already exists"
            )
    })
    @PostMapping("/register")
    public ApiResponse<?> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        log.info("Registration request received for email: {}", request.getEmail());

        authService.register(request);

        return ApiResponse.builder()
                .success(true)
                .message("User registered successfully")
                .data(null)
                .build();
    }

    @Operation(
            summary = "Initiate login",
            description = "Validates credentials and sends OTP to user's email"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "OTP sent successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation failed"
            )
    })
    @PostMapping("/login")
    public ApiResponse<?> login(
            @Valid @RequestBody LoginRequest request
    ) {
        log.info("Login initiation request received for email: {}", request.getEmail());

        authService.initiateLogin(request);

        return ApiResponse.builder()
                .success(true)
                .message("OTP sent to your email. Please verify to complete login.")
                .data(null)
                .build();
    }

    @Operation(
            summary = "Verify OTP and complete login",
            description = "Verifies the OTP code and returns JWT token"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Login successful, token returned"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid or expired OTP"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
    @PostMapping("/verify-otp")
    public ApiResponse<LoginResponse> verifyOtp(
            @Valid @RequestBody OtpVerificationRequest request
    ) {
        log.info("OTP verification request received for email: {}", request.getEmail());

        return ApiResponse.<LoginResponse>builder()
                .success(true)
                .message("Login successful")
                .data(authService.verifyOtpAndLogin(
                        request.getEmail(),
                        request.getCode()
                ))
                .build();
    }
}