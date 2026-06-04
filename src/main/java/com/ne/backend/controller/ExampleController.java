package com.ne.backend.controller;

import com.ne.backend.dto.ApiResponse;
import com.ne.backend.entity.ExampleEntity;
import com.ne.backend.service.ExampleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/examples")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Example Management", description = "Example entity management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ExampleController {

    private final ExampleService exampleService;

    @Operation(
            summary = "Get all examples",
            description = "Retrieves all examples in the system"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Examples retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping
    public ApiResponse<?> getAll() {
        log.info("Get all examples request received");

        return ApiResponse.builder()
                .success(true)
                .message("Examples fetched")
                .data(exampleService.getAll())
                .build();
    }

    @Operation(
            summary = "Get example by ID",
            description = "Retrieves a specific example by its ID"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Example retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Example not found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
    @GetMapping("/{id}")
    public ApiResponse<?> getById(@PathVariable Long id) {
        log.info("Get example by ID request received for ID: {}", id);

        return ApiResponse.builder()
                .success(true)
                .message("Example fetched")
                .data(exampleService.getById(id))
                .build();
    }

    @Operation(
            summary = "Create example",
            description = "Creates a new example entity"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Example created successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation failed"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping
    public ApiResponse<?> create(
            @Valid @RequestBody ExampleEntity entity
    ) {
        log.info("Create example request received");

        return ApiResponse.builder()
                .success(true)
                .message("Example created")
                .data(exampleService.create(entity))
                .build();
    }

    @Operation(
            summary = "Update example",
            description = "Updates an existing example entity"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Example updated successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Example not found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation failed"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PutMapping("/{id}")
    public ApiResponse<?> update(
            @PathVariable Long id,
            @RequestBody ExampleEntity entity
    ) {
        log.info("Update example request received for ID: {}", id);

        return ApiResponse.builder()
                .success(true)
                .message("Example updated")
                .data(exampleService.update(id, entity))
                .build();
    }

    @Operation(
            summary = "Delete example",
            description = "Deletes an example entity by its ID"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Example deleted successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Example not found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(
            @PathVariable Long id
    ) {
        log.info("Delete example request received for ID: {}", id);

        exampleService.delete(id);

        return ApiResponse.builder()
                .success(true)
                .message("Example deleted")
                .data(null)
                .build();
    }
}