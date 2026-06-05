package com.ne.backend.controller;

import com.ne.backend.dto.ApiResponse;
import com.ne.backend.dto.bill.BillResponse;
import com.ne.backend.enums.BillStatus;
import com.ne.backend.service.BillingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Bill Management", description = "APIs for managing bills")
@SecurityRequirement(name = "bearerAuth")
public class BillController {

    private final BillingService billingService;

    @Operation(summary = "Generate a bill", description = "Generates a bill for a meter reading")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Bill generated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed or bill already exists"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Meter reading or tariff not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @PostMapping("/generate/{meterReadingId}")
    public ApiResponse<BillResponse> generateBill(@PathVariable Long meterReadingId) {
        log.info("Generate bill request received for meter reading: {}", meterReadingId);
        BillResponse response = billingService.generateBill(meterReadingId);
        return ApiResponse.<BillResponse>builder()
                .success(true)
                .message("Bill generated successfully")
                .data(response)
                .build();
    }

    @Operation(summary = "Get bill by ID", description = "Retrieves a bill by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Bill retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Bill not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE', 'CUSTOMER')")
    @GetMapping("/{id}")
    public ApiResponse<BillResponse> getById(@PathVariable Long id, Authentication authentication) {
        log.info("Get bill by ID request received: {}", id);
        BillResponse response = billingService.getById(id);
        
        // Customers can only view their own bills
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CUSTOMER"))) {
            Long customerId = (Long) authentication.getPrincipal();
            if (!response.getCustomerId().equals(customerId)) {
                throw new RuntimeException("Access denied: You can only view your own bills");
            }
        }
        
        return ApiResponse.<BillResponse>builder()
                .success(true)
                .message("Bill retrieved successfully")
                .data(response)
                .build();
    }

    @Operation(summary = "Get all bills with pagination", description = "Retrieves all bills with pagination support")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Bills retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE')")
    @GetMapping
    public ApiResponse<Page<BillResponse>> getAll(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir
    ) {
        log.info("Get all bills request received - page: {}, size: {}", page, size);
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<BillResponse> response = billingService.getAll(pageable);
        return ApiResponse.<Page<BillResponse>>builder()
                .success(true)
                .message("Bills retrieved successfully")
                .data(response)
                .build();
    }

    @Operation(summary = "Search bills", description = "Search bills with filters and pagination")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Bills retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE')")
    @GetMapping("/search")
    public ApiResponse<Page<BillResponse>> search(
            @Parameter(description = "Customer ID filter") @RequestParam(required = false) Long customerId,
            @Parameter(description = "Meter ID filter") @RequestParam(required = false) Long meterId,
            @Parameter(description = "Status filter") @RequestParam(required = false) BillStatus status,
            @Parameter(description = "Billing month filter") @RequestParam(required = false) Integer billingMonth,
            @Parameter(description = "Billing year filter") @RequestParam(required = false) Integer billingYear,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir
    ) {
        log.info("Search bills request received");
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<BillResponse> response = billingService.search(customerId, meterId, status, billingMonth, billingYear, pageable);
        return ApiResponse.<Page<BillResponse>>builder()
                .success(true)
                .message("Bills retrieved successfully")
                .data(response)
                .build();
    }

    @Operation(summary = "Get bills by customer", description = "Retrieves all bills for a specific customer")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Bills retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Customer not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE', 'CUSTOMER')")
    @GetMapping("/customer/{customerId}")
    public ApiResponse<List<BillResponse>> getByCustomer(@PathVariable Long customerId, Authentication authentication) {
        log.info("Get bills by customer request received: {}", customerId);
        
        // Customers can only view their own bills
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CUSTOMER"))) {
            Long authenticatedCustomerId = (Long) authentication.getPrincipal();
            if (!customerId.equals(authenticatedCustomerId)) {
                throw new RuntimeException("Access denied: You can only view your own bills");
            }
        }
        
        List<BillResponse> response = billingService.getByCustomer(customerId);
        return ApiResponse.<List<BillResponse>>builder()
                .success(true)
                .message("Bills retrieved successfully")
                .data(response)
                .build();
    }

    @Operation(summary = "Get my bills", description = "Retrieves all bills for the authenticated customer using JWT")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Bills retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/my-bills")
    public ApiResponse<List<BillResponse>> getMyBills(Authentication authentication) {
        log.info("Get my bills request received");
        
        Long customerId = (Long) authentication.getPrincipal();
        List<BillResponse> response = billingService.getByCustomer(customerId);
        
        return ApiResponse.<List<BillResponse>>builder()
                .success(true)
                .message("Your bills retrieved successfully")
                .data(response)
                .build();
    }

    @Operation(summary = "Approve bill", description = "Approves a pending bill")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Bill approved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bill is not in pending status"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Bill not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @PostMapping("/{id}/approve")
    public ApiResponse<BillResponse> approveBill(@PathVariable Long id) {
        log.info("Approve bill request received: {}", id);
        BillResponse response = billingService.approveBill(id);
        return ApiResponse.<BillResponse>builder()
                .success(true)
                .message("Bill approved successfully")
                .data(response)
                .build();
    }

    @Operation(summary = "Delete bill", description = "Deletes a bill by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Bill deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Cannot delete paid bills"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Bill not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {
        log.info("Delete bill request received: {}", id);
        billingService.delete(id);
        return ApiResponse.builder()
                .success(true)
                .message("Bill deleted successfully")
                .data(null)
                .build();
    }
}
