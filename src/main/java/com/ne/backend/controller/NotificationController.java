package com.ne.backend.controller;

import com.ne.backend.dto.ApiResponse;
import com.ne.backend.dto.notification.NotificationResponse;
import com.ne.backend.enums.NotificationType;
import com.ne.backend.service.NotificationService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notification Management", description = "APIs for managing notifications")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Get notification by ID", description = "Retrieves a notification by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notification retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Notification not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE', 'CUSTOMER')")
    @GetMapping("/{id}")
    public ApiResponse<NotificationResponse> getById(@PathVariable Long id) {
        log.info("Get notification by ID request received: {}", id);
        NotificationResponse response = notificationService.getById(id);
        return ApiResponse.<NotificationResponse>builder()
                .success(true)
                .message("Notification retrieved successfully")
                .data(response)
                .build();
    }

    @Operation(summary = "Get all notifications with pagination", description = "Retrieves all notifications with pagination support")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notifications retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE')")
    @GetMapping
    public ApiResponse<Page<NotificationResponse>> getAll(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir
    ) {
        log.info("Get all notifications request received - page: {}, size: {}", page, size);
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<NotificationResponse> response = notificationService.getAll(pageable);
        return ApiResponse.<Page<NotificationResponse>>builder()
                .success(true)
                .message("Notifications retrieved successfully")
                .data(response)
                .build();
    }

    @Operation(summary = "Search notifications", description = "Search notifications with filters and pagination")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notifications retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE')")
    @GetMapping("/search")
    public ApiResponse<Page<NotificationResponse>> search(
            @Parameter(description = "Customer ID filter") @RequestParam(required = false) Long customerId,
            @Parameter(description = "Notification type filter") @RequestParam(required = false) NotificationType notificationType,
            @Parameter(description = "Read status filter") @RequestParam(required = false) Boolean read,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir
    ) {
        log.info("Search notifications request received");
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<NotificationResponse> response = notificationService.search(customerId, notificationType, read, pageable);
        return ApiResponse.<Page<NotificationResponse>>builder()
                .success(true)
                .message("Notifications retrieved successfully")
                .data(response)
                .build();
    }

    @Operation(summary = "Get notifications by customer", description = "Retrieves all notifications for a specific customer")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notifications retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Customer not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE', 'CUSTOMER')")
    @GetMapping("/customer/{customerId}")
    public ApiResponse<List<NotificationResponse>> getByCustomer(@PathVariable Long customerId) {
        log.info("Get notifications by customer request received: {}", customerId);
        List<NotificationResponse> response = notificationService.getByCustomer(customerId);
        return ApiResponse.<List<NotificationResponse>>builder()
                .success(true)
                .message("Notifications retrieved successfully")
                .data(response)
                .build();
    }

    @Operation(summary = "Get unread notifications by customer", description = "Retrieves all unread notifications for a specific customer")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notifications retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Customer not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE', 'CUSTOMER')")
    @GetMapping("/customer/{customerId}/unread")
    public ApiResponse<List<NotificationResponse>> getUnreadByCustomer(@PathVariable Long customerId) {
        log.info("Get unread notifications by customer request received: {}", customerId);
        List<NotificationResponse> response = notificationService.getUnreadByCustomer(customerId);
        return ApiResponse.<List<NotificationResponse>>builder()
                .success(true)
                .message("Unread notifications retrieved successfully")
                .data(response)
                .build();
    }

    @Operation(summary = "Count unread notifications by customer", description = "Counts unread notifications for a specific customer")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Count retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Customer not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE', 'CUSTOMER')")
    @GetMapping("/customer/{customerId}/unread/count")
    public ApiResponse<Long> countUnreadByCustomer(@PathVariable Long customerId) {
        log.info("Count unread notifications by customer request received: {}", customerId);
        Long count = notificationService.countUnreadByCustomer(customerId);
        return ApiResponse.<Long>builder()
                .success(true)
                .message("Unread notifications count retrieved successfully")
                .data(count)
                .build();
    }

    @Operation(summary = "Mark notification as read", description = "Marks a notification as read")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notification marked as read successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Notification not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE', 'CUSTOMER')")
    @PostMapping("/{id}/read")
    public ApiResponse<NotificationResponse> markAsRead(@PathVariable Long id) {
        log.info("Mark notification as read request received: {}", id);
        NotificationResponse response = notificationService.markAsRead(id);
        return ApiResponse.<NotificationResponse>builder()
                .success(true)
                .message("Notification marked as read successfully")
                .data(response)
                .build();
    }

    @Operation(summary = "Delete notification", description = "Deletes a notification by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notification deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Notification not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {
        log.info("Delete notification request received: {}", id);
        notificationService.delete(id);
        return ApiResponse.builder()
                .success(true)
                .message("Notification deleted successfully")
                .data(null)
                .build();
    }
}
