package com.ne.backend.dto.customer;

import com.ne.backend.enums.CustomerStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new customer
 * Inherits user fields (firstName, lastName, email, mobile, password) from User
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCustomerRequest {

    // user first name
    @NotBlank(message = "First name is required")
    private String firstName;

    // Users last name (inherited from User)
    @NotBlank(message = "Last name is required")
    private String lastName;

    // User's email (inherited from User)
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    // User's phone number (inherited from User)
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone number must be valid")
    private String mobile;

    // User's password (inherited from User)
    @NotBlank(message = "Password is required")
    private String password;

    // Customer-specific: National ID
    @NotBlank(message = "National ID is required")
    @Pattern(regexp = "^\\d{16}$", message = "National ID must be a 16 digit string")
    private String nationalId;

    // Customer-specific: Address
    @NotBlank(message = "Address is required")
    private String address;

    // Customer-specific status
    @NotNull(message = "Status is required")
    private CustomerStatus customerStatus;
}
