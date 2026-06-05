package com.ne.backend.controller;

import com.ne.backend.dto.ApiResponse;
import com.ne.backend.dto.meterreading.CreateMeterReadingRequest;
import com.ne.backend.dto.meterreading.MeterReadingResponse;
import com.ne.backend.service.MeterReadingService;
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
@RequestMapping("/api/meter-readings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Meter Reading Management", description = "APIs for managing meter readings")
@SecurityRequirement(name = "bearerAuth")
public class MeterReadingController {

    private final MeterReadingService meterReadingService;

    @Operation(summary = "Create a new meter reading", description = "Creates a new meter reading with the provided details")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Meter reading created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed or duplicate reading"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @PostMapping
    public ApiResponse<MeterReadingResponse> create(@Valid @RequestBody CreateMeterReadingRequest request) {
        log.info("Create meter reading request received");
        MeterReadingResponse response = meterReadingService.create(request);
        return ApiResponse.<MeterReadingResponse>builder()
                .success(true)
                .message("Meter reading created successfully")
                .data(response)
                .build();
    }

    @Operation(summary = "Get meter reading by ID", description = "Retrieves a meter reading by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Meter reading retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Meter reading not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE', 'CUSTOMER')")
    @GetMapping("/{id}")
    public ApiResponse<MeterReadingResponse> getById(@PathVariable Long id) {
        log.info("Get meter reading by ID request received: {}", id);
        MeterReadingResponse response = meterReadingService.getById(id);
        return ApiResponse.<MeterReadingResponse>builder()
                .success(true)
                .message("Meter reading retrieved successfully")
                .data(response)
                .build();
    }

    @Operation(summary = "Get all meter readings with pagination", description = "Retrieves all meter readings with pagination support")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Meter readings retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE')")
    @GetMapping
    public ApiResponse<Page<MeterReadingResponse>> getAll(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir
    ) {
        log.info("Get all meter readings request received - page: {}, size: {}", page, size);
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<MeterReadingResponse> response = meterReadingService.getAll(pageable);
        return ApiResponse.<Page<MeterReadingResponse>>builder()
                .success(true)
                .message("Meter readings retrieved successfully")
                .data(response)
                .build();
    }

    @Operation(summary = "Search meter readings", description = "Search meter readings with filters and pagination")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Meter readings retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE')")
    @GetMapping("/search")
    public ApiResponse<Page<MeterReadingResponse>> search(
            @Parameter(description = "Meter ID filter") @RequestParam(required = false) Long meterId,
            @Parameter(description = "Start date filter") @RequestParam(required = false) LocalDate startDate,
            @Parameter(description = "End date filter") @RequestParam(required = false) LocalDate endDate,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir
    ) {
        log.info("Search meter readings request received");
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<MeterReadingResponse> response = meterReadingService.search(meterId, startDate, endDate, pageable);
        return ApiResponse.<Page<MeterReadingResponse>>builder()
                .success(true)
                .message("Meter readings retrieved successfully")
                .data(response)
                .build();
    }

    @Operation(summary = "Get meter readings by meter", description = "Retrieves all meter readings for a specific meter")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Meter readings retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Meter not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE', 'CUSTOMER')")
    @GetMapping("/meter/{meterId}")
    public ApiResponse<List<MeterReadingResponse>> getByMeter(@PathVariable Long meterId) {
        log.info("Get meter readings by meter request received: {}", meterId);
        List<MeterReadingResponse> response = meterReadingService.getByMeter(meterId);
        return ApiResponse.<List<MeterReadingResponse>>builder()
                .success(true)
                .message("Meter readings retrieved successfully")
                .data(response)
                .build();
    }

    @Operation(summary = "Get latest meter reading by meter", description = "Retrieves the latest meter reading for a specific meter")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Meter reading retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Meter or reading not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE', 'CUSTOMER')")
    @GetMapping("/meter/{meterId}/latest")
    public ApiResponse<MeterReadingResponse> getLatestByMeter(@PathVariable Long meterId) {
        log.info("Get latest meter reading by meter request received: {}", meterId);
        MeterReadingResponse response = meterReadingService.getLatestByMeter(meterId);
        return ApiResponse.<MeterReadingResponse>builder()
                .success(true)
                .message("Latest meter reading retrieved successfully")
                .data(response)
                .build();
    }

    @Operation(summary = "Delete meter reading", description = "Deletes a meter reading by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Meter reading deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Meter reading not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {
        log.info("Delete meter reading request received: {}", id);
        meterReadingService.delete(id);
        return ApiResponse.builder()
                .success(true)
                .message("Meter reading deleted successfully")
                .data(null)
                .build();
    }
}
