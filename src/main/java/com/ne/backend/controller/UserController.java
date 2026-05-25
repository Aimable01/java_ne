package com.ne.backend.controller;

import com.ne.backend.dto.ApiResponse;
import com.ne.backend.entity.User;
import com.ne.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get all users")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping
    public ApiResponse<?> getAll() {

        return ApiResponse.builder()
                .success(true)
                .message("Users fetched")
                .data(userService.getAll())
                .build();
    }

    @Operation(summary = "Get user by ID")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ApiResponse<User> getById(@PathVariable Long id) {

        return ApiResponse.<User>builder()
                .success(true)
                .message("User fetched")
                .data(userService.getById(id))
                .build();
    }

    @Operation(summary = "Delete user")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {

        userService.delete(id);

        return ApiResponse.builder()
                .success(true)
                .message("User deleted")
                .data(null)
                .build();
    }
}