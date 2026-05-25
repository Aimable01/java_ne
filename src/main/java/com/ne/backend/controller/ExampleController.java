package com.ne.backend.controller;

import com.ne.backend.dto.ApiResponse;
import com.ne.backend.entity.ExampleEntity;
import com.ne.backend.service.ExampleService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/examples")
@RequiredArgsConstructor
public class ExampleController {

    private final ExampleService exampleService;

    @Operation(summary = "Get all examples")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping
    public ApiResponse<?> getAll() {

        return ApiResponse.builder()
                .success(true)
                .message("Examples fetched")
                .data(exampleService.getAll())
                .build();
    }

    @Operation(summary = "Get example by ID")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
    @GetMapping("/{id}")
    public ApiResponse<?> getById(@PathVariable Long id) {

        return ApiResponse.builder()
                .success(true)
                .message("Example fetched")
                .data(exampleService.getById(id))
                .build();
    }

    @Operation(summary = "Create example")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping
    public ApiResponse<?> create(
            @Valid @RequestBody ExampleEntity entity
    ) {

        return ApiResponse.builder()
                .success(true)
                .message("Example created")
                .data(exampleService.create(entity))
                .build();
    }

    @Operation(summary = "Update example")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PutMapping("/{id}")
    public ApiResponse<?> update(
            @PathVariable Long id,
            @RequestBody ExampleEntity entity
    ) {

        return ApiResponse.builder()
                .success(true)
                .message("Example updated")
                .data(exampleService.update(id, entity))
                .build();
    }

    @Operation(summary = "Delete example")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(
            @PathVariable Long id
    ) {

        exampleService.delete(id);

        return ApiResponse.builder()
                .success(true)
                .message("Example deleted")
                .data(null)
                .build();
    }
}