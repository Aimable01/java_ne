package com.ne.backend.controller;

import com.ne.backend.dto.ApiResponse;
import com.ne.backend.dto.meter.CreateMeterRequest;
import com.ne.backend.dto.meter.MeterResponse;
import com.ne.backend.dto.meter.UpdateMeterRequest;
import com.ne.backend.enums.MeterStatus;
import com.ne.backend.enums.MeterType;
import com.ne.backend.service.MeterService;
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

@RestController
@RequestMapping("/api/meters")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Meter Management", description = "APIs for managing meters")
@SecurityRequirement(name = "bearerAuth")
public class MeterController {

    private final MeterService meterService;

    @Operation(summary = "Create a new meter", description = "Creates a new meter with the provided details")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Meter created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed or duplicate meter"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @PostMapping
    public ApiResponse<MeterResponse> create(@Valid @RequestBody CreateMeterRequest request) {
        log.info("Create meter request received");
        MeterResponse response = meterService.create(request);
        return ApiResponse.<MeterResponse>builder()
                .success(true)
                .message("Meter created successfully")
                .data(response)
                .build();
    }

    @Operation(summary = "Get meter by ID", description = "Retrieves a meter by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Meter retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Meter not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE', 'CUSTOMER')")
    @GetMapping("/{id}")
    public ApiResponse<MeterResponse> getById(@PathVariable Long id) {
        log.info("Get meter by ID request received: {}", id);
        MeterResponse response = meterService.getById(id);
        return ApiResponse.<MeterResponse>builder()
                .success(true)
                .message("Meter retrieved successfully")
                .data(response)
                .build();
    }

    @Operation(summary = "Get meter by meter number", description = "Retrieves a meter by its meter number")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Meter retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Meter not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE', 'CUSTOMER')")
    @GetMapping("/number/{meterNumber}")
    public ApiResponse<MeterResponse> getByMeterNumber(@PathVariable String meterNumber) {
        log.info("Get meter by number request received: {}", meterNumber);
        MeterResponse response = meterService.getByMeterNumber(meterNumber);
        return ApiResponse.<MeterResponse>builder()
                .success(true)
                .message("Meter retrieved successfully")
                .data(response)
                .build();
    }

    @Operation(summary = "Get all meters with pagination", description = "Retrieves all meters with pagination support")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Meters retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE')")
    @GetMapping
    public ApiResponse<Page<MeterResponse>> getAll(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir
    ) {
        log.info("Get all meters request received - page: {}, size: {}", page, size);
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<MeterResponse> response = meterService.getAll(pageable);
        return ApiResponse.<Page<MeterResponse>>builder()
                .success(true)
                .message("Meters retrieved successfully")
                .data(response)
                .build();
    }

    @Operation(summary = "Search meters", description = "Search meters with filters and pagination")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Meters retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE')")
    @GetMapping("/search")
    public ApiResponse<Page<MeterResponse>> search(
            @Parameter(description = "Meter number filter") @RequestParam(required = false) String meterNumber,
            @Parameter(description = "Meter type filter") @RequestParam(required = false) MeterType meterType,
            @Parameter(description = "Status filter") @RequestParam(required = false) MeterStatus status,
            @Parameter(description = "Customer ID filter") @RequestParam(required = false) Long customerId,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir
    ) {
        log.info("Search meters request received");
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<MeterResponse> response = meterService.search(meterNumber, meterType, status, customerId, pageable);
        return ApiResponse.<Page<MeterResponse>>builder()
                .success(true)
                .message("Meters retrieved successfully")
                .data(response)
                .build();
    }

    @Operation(summary = "Get meters by customer", description = "Retrieves all meters for a specific customer")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Meters retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Customer not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE', 'CUSTOMER')")
    @GetMapping("/customer/{customerId}")
    public ApiResponse<Page<MeterResponse>> getByCustomer(
            @PathVariable Long customerId,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir
    ) {
        log.info("Get meters by customer request received: {}", customerId);
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<MeterResponse> response = meterService.getByCustomer(customerId, pageable);
        return ApiResponse.<Page<MeterResponse>>builder()
                .success(true)
                .message("Meters retrieved successfully")
                .data(response)
                .build();
    }

    @Operation(summary = "Update meter", description = "Updates an existing meter")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Meter updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed or duplicate meter"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Meter not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @PutMapping("/{id}")
    public ApiResponse<MeterResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateMeterRequest request) {
        log.info("Update meter request received: {}", id);
        MeterResponse response = meterService.update(id, request);
        return ApiResponse.<MeterResponse>builder()
                .success(true)
                .message("Meter updated successfully")
                .data(response)
                .build();
    }

    @Operation(summary = "Delete meter", description = "Deletes a meter by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Meter deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Meter not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {
        log.info("Delete meter request received: {}", id);
        meterService.delete(id);
        return ApiResponse.builder()
                .success(true)
                .message("Meter deleted successfully")
                .data(null)
                .build();
    }
}
