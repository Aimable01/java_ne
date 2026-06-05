package com.ne.backend.dto.customer;

import com.ne.backend.enums.CustomerStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating an existing customer
 * Can update both User fields and Customer-specific fields
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCustomerRequest {

    // User's first name (inherited from User)
    private String firstName;

    // User's last name (inherited from User)
    private String lastName;

    // User's email (inherited from User)
    @Email(message = "Email must be valid")
    private String email;

    // User's phone number (inherited from User)
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone number must be valid")
    private String mobile;

    // Customer-specific: National ID
    @Pattern(regexp = "^\\d{16}$", message = "National ID must be a 16 digit string")
    private String nationalId;

    // Customer-specific: Address
    private String address;

    // Customer-specific status
    private CustomerStatus customerStatus;
}
