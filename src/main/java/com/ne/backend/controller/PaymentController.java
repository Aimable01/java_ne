package com.ne.backend.controller;

import com.ne.backend.dto.ApiResponse;
import com.ne.backend.dto.payment.CreatePaymentRequest;
import com.ne.backend.dto.payment.PaymentResponse;
import com.ne.backend.service.PaymentService;
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
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment Management", description = "APIs for managing payments")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Create a new payment", description = "Creates a new payment with the provided details")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed or duplicate reference"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Bill not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @PostMapping
    public ApiResponse<PaymentResponse> create(@Valid @RequestBody CreatePaymentRequest request) {
        log.info("Create payment request received");
        PaymentResponse response = paymentService.create(request);
        return ApiResponse.<PaymentResponse>builder()
                .success(true)
                .message("Payment created successfully")
                .data(response)
                .build();
    }

    @Operation(summary = "Get payment by ID", description = "Retrieves a payment by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Payment not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'CUSTOMER')")
    @GetMapping("/{id}")
    public ApiResponse<PaymentResponse> getById(@PathVariable Long id) {
        log.info("Get payment by ID request received: {}", id);
        PaymentResponse response = paymentService.getById(id);
        return ApiResponse.<PaymentResponse>builder()
                .success(true)
                .message("Payment retrieved successfully")
                .data(response)
                .build();
    }

    @Operation(summary = "Get payment by reference number", description = "Retrieves a payment by its reference number")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Payment not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'CUSTOMER')")
    @GetMapping("/reference/{referenceNumber}")
    public ApiResponse<PaymentResponse> getByReferenceNumber(@PathVariable String referenceNumber) {
        log.info("Get payment by reference number request received: {}", referenceNumber);
        PaymentResponse response = paymentService.getByReferenceNumber(referenceNumber);
        return ApiResponse.<PaymentResponse>builder()
                .success(true)
                .message("Payment retrieved successfully")
                .data(response)
                .build();
    }

    @Operation(summary = "Get all payments with pagination", description = "Retrieves all payments with pagination support")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payments retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @GetMapping
    public ApiResponse<Page<PaymentResponse>> getAll(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir
    ) {
        log.info("Get all payments request received - page: {}, size: {}", page, size);
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PaymentResponse> response = paymentService.getAll(pageable);
        return ApiResponse.<Page<PaymentResponse>>builder()
                .success(true)
                .message("Payments retrieved successfully")
                .data(response)
                .build();
    }

    @Operation(summary = "Search payments", description = "Search payments with filters and pagination")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payments retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @GetMapping("/search")
    public ApiResponse<Page<PaymentResponse>> search(
            @Parameter(description = "Bill ID filter") @RequestParam(required = false) Long billId,
            @Parameter(description = "Payment method filter") @RequestParam(required = false) String paymentMethod,
            @Parameter(description = "Start date filter") @RequestParam(required = false) LocalDate startDate,
            @Parameter(description = "End date filter") @RequestParam(required = false) LocalDate endDate,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir
    ) {
        log.info("Search payments request received");
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PaymentResponse> response = paymentService.search(billId, paymentMethod, startDate, endDate, pageable);
        return ApiResponse.<Page<PaymentResponse>>builder()
                .success(true)
                .message("Payments retrieved successfully")
                .data(response)
                .build();
    }

    @Operation(summary = "Get payments by bill", description = "Retrieves all payments for a specific bill")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payments retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Bill not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'CUSTOMER')")
    @GetMapping("/bill/{billId}")
    public ApiResponse<List<PaymentResponse>> getByBill(@PathVariable Long billId) {
        log.info("Get payments by bill request received: {}", billId);
        List<PaymentResponse> response = paymentService.getByBill(billId);
        return ApiResponse.<List<PaymentResponse>>builder()
                .success(true)
                .message("Payments retrieved successfully")
                .data(response)
                .build();
    }

    @Operation(summary = "Delete payment", description = "Deletes a payment by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Payment not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {
        log.info("Delete payment request received: {}", id);
        paymentService.delete(id);
        return ApiResponse.builder()
                .success(true)
                .message("Payment deleted successfully")
                .data(null)
                .build();
    }
}
