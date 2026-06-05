package com.ne.backend.entity;

import com.ne.backend.enums.CustomerStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Customer entity extending User
 * Contains customer-specific information for utility billing
 * Uses TABLE_PER_CLASS inheritance strategy - each class has its own table
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Customer extends User {

    // National ID (16 digits starting with 1)
    @NotBlank(message = "National ID is required")
    @Column(nullable = false, unique = true)
    @Pattern(regexp = "^1[0-9]{15}$", message = "National ID must be 16 digits starting with 1")
    private String nationalId;

    // Customer's physical address
    @NotBlank(message = "Address is required")
    @Column(nullable = false)
    private String address;

    // Customer-specific status (ACTIVE/INACTIVE)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerStatus customerStatus = CustomerStatus.ACTIVE;

    // Timestamp when customer was created
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Timestamp when customer was last updated
    @Column
    private LocalDateTime updatedAt;

    // Update timestamp before saving
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
