package com.ne.backend.dto.customer;

import com.ne.backend.enums.CustomerStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCustomerRequest {

    private String fullName;

    @Pattern(regexp = "^1[0-9]{15}$", message = "National ID must be 16 digits starting with 1")
    private String nationalId;

    @Email(message = "Email must be valid")
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone number must be valid")
    private String phoneNumber;

    private String address;

    private CustomerStatus status;
}
