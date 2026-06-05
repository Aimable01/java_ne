package com.ne.backend.controller;

import com.ne.backend.dto.ApiResponse;
import com.ne.backend.dto.tariff.CreateTariffRequest;
import com.ne.backend.dto.tariff.TariffResponse;
import com.ne.backend.enums.MeterType;
import com.ne.backend.service.TariffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tariffs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tariff Management", description = "APIs for managing tariffs")
@SecurityRequirement(name = "bearerAuth")
public class TariffController {

    private final TariffService tariffService;

    @Operation(summary = "Create a new tariff", description = "Creates a new tariff with the provided details")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tariff created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed or invalid version"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ApiResponse<TariffResponse> create(@Valid @RequestBody CreateTariffRequest request) {
        log.info("Create tariff request received");
        TariffResponse response = tariffService.create(request);
        return ApiResponse.<TariffResponse>builder()
                .success(true)
                .message("Tariff created successfully")
                .data(response)
                .build();
    }

    @Operation(summary = "Get tariff by ID", description = "Retrieves a tariff by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tariff retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tariff not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE', 'CUSTOMER')")
    @GetMapping("/{id}")
    public ApiResponse<TariffResponse> getById(@PathVariable Long id) {
        log.info("Get tariff by ID request received: {}", id);
        TariffResponse response = tariffService.getById(id);
        return ApiResponse.<TariffResponse>builder()
                .success(true)
                .message("Tariff retrieved successfully")
                .data(response)
                .build();
    }

    @Operation(summary = "Get all tariffs with pagination", description = "Retrieves all tariffs with pagination support")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tariffs retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE')")
    @GetMapping
    public ApiResponse<Page<TariffResponse>> getAll(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir
    ) {
        log.info("Get all tariffs request received - page: {}, size: {}", page, size);
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<TariffResponse> response = tariffService.getAll(pageable);
        return ApiResponse.<Page<TariffResponse>>builder()
                .success(true)
                .message("Tariffs retrieved successfully")
                .data(response)
                .build();
    }

    @Operation(summary = "Search tariffs", description = "Search tariffs with filters and pagination")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tariffs retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE')")
    @GetMapping("/search")
    public ApiResponse<Page<TariffResponse>> search(
            @Parameter(description = "Meter type filter") @RequestParam(required = false) MeterType meterType,
            @Parameter(description = "Active filter") @RequestParam(required = false) Boolean active,
            @Parameter(description = "Effective date filter") @RequestParam(required = false) LocalDate effectiveDate,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir
    ) {
        log.info("Search tariffs request received");
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<TariffResponse> response = tariffService.search(meterType, active, effectiveDate, pageable);
        return ApiResponse.<Page<TariffResponse>>builder()
                .success(true)
                .message("Tariffs retrieved successfully")
                .data(response)
                .build();
    }

    @Operation(summary = "Get active tariffs by meter type", description = "Retrieves all active tariffs for a specific meter type")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tariffs retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE', 'CUSTOMER')")
    @GetMapping("/meter-type/{meterType}")
    public ApiResponse<List<TariffResponse>> getActiveTariffsByMeterType(@PathVariable MeterType meterType) {
        log.info("Get active tariffs by meter type request received: {}", meterType);
        List<TariffResponse> response = tariffService.getActiveTariffsByMeterType(meterType);
        return ApiResponse.<List<TariffResponse>>builder()
                .success(true)
                .message("Tariffs retrieved successfully")
                .data(response)
                .build();
    }

    @Operation(summary = "Get latest active tariff by meter type", description = "Retrieves the latest active tariff for a specific meter type")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tariff retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No active tariff found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE', 'CUSTOMER')")
    @GetMapping("/meter-type/{meterType}/latest")
    public ApiResponse<TariffResponse> getLatestActiveTariffByMeterType(@PathVariable MeterType meterType) {
        log.info("Get latest active tariff by meter type request received: {}", meterType);
        TariffResponse response = tariffService.getLatestActiveTariffByMeterType(meterType);
        return ApiResponse.<TariffResponse>builder()
                .success(true)
                .message("Tariff retrieved successfully")
                .data(response)
                .build();
    }

    @Operation(summary = "Delete tariff", description = "Deletes a tariff by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tariff deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tariff not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {
        log.info("Delete tariff request received: {}", id);
        tariffService.delete(id);
        return ApiResponse.builder()
                .success(true)
                .message("Tariff deleted successfully")
                .data(null)
                .build();
    }
}
