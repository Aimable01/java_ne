package com.ne.backend.dto.customer;

import com.ne.backend.enums.CustomerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for customer response
 * Includes both User fields and Customer-specific fields
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {

    private Long id;
    
    // User fields (inherited)
    private String firstName;
    private String lastName;
    private String email;
    private String mobile;
    
    // Customer-specific fields
    private String nationalId;
    private String address;
    private CustomerStatus customerStatus;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
