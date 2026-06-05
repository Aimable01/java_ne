package com.ne.backend.service;

import com.ne.backend.dto.customer.CreateCustomerRequest;
import com.ne.backend.dto.customer.CustomerResponse;
import com.ne.backend.dto.customer.UpdateCustomerRequest;
import com.ne.backend.entity.Customer;
import com.ne.backend.enums.CustomerStatus;
import com.ne.backend.exception.ResourceNotFoundException;
import com.ne.backend.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service for managing customers
 * Customer extends User, so it inherits firstName, lastName, email, mobile, password
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    // Create a new customer with user fields and customer-specific fields
    public CustomerResponse create(CreateCustomerRequest request) {
        log.info("Creating customer with national ID: {}", request.getNationalId());

        if (customerRepository.existsByNationalId(request.getNationalId())) {
            throw new RuntimeException("Customer with this national ID already exists");
        }

        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Customer with this email already exists");
        }

        // Build customer with inherited User fields and customer-specific fields
        Customer customer = new Customer();
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setMobile(request.getMobile());
        customer.setPassword(passwordEncoder.encode(request.getPassword()));
        customer.setNationalId(request.getNationalId());
        customer.setAddress(request.getAddress());
        customer.setCustomerStatus(request.getCustomerStatus());

        Customer saved = customerRepository.save(customer);
        log.info("Customer created successfully with ID: {}", saved.getId());

        return mapToResponse(saved);
    }

    // Get customer by ID
    public CustomerResponse getById(Long id) {
        log.info("Fetching customer by ID: {}", id);
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        return mapToResponse(customer);
    }

    // Get customer by national ID
    public CustomerResponse getByNationalId(String nationalId) {
        log.info("Fetching customer by national ID: {}", nationalId);
        Customer customer = customerRepository.findByNationalId(nationalId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        return mapToResponse(customer);
    }

    // Get all customers with pagination
    public Page<CustomerResponse> getAll(Pageable pageable) {
        log.info("Fetching all customers with pagination");
        return customerRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    // Search customers with filters
    public Page<CustomerResponse> search(
            String firstName,
            String lastName,
            String nationalId,
            String email,
            String mobile,
            CustomerStatus customerStatus,
            Pageable pageable
    ) {
        log.info("Searching customers with filters");
        return customerRepository.searchCustomers(firstName, lastName, nationalId, email, mobile, customerStatus, pageable)
                .map(this::mapToResponse);
    }

    // Update customer
    public CustomerResponse update(Long id, UpdateCustomerRequest request) {
        log.info("Updating customer with ID: {}", id);
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        // Update User fields (inherited)
        if (request.getFirstName() != null) {
            customer.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            customer.setLastName(request.getLastName());
        }
        if (request.getEmail() != null && !request.getEmail().equals(customer.getEmail())) {
            if (customerRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Customer with this email already exists");
            }
            customer.setEmail(request.getEmail());
        }
        if (request.getMobile() != null) {
            customer.setMobile(request.getMobile());
        }

        // Update Customer-specific fields
        if (request.getNationalId() != null && !request.getNationalId().equals(customer.getNationalId())) {
            if (customerRepository.existsByNationalId(request.getNationalId())) {
                throw new RuntimeException("Customer with this national ID already exists");
            }
            customer.setNationalId(request.getNationalId());
        }
        if (request.getAddress() != null) {
            customer.setAddress(request.getAddress());
        }
        if (request.getCustomerStatus() != null) {
            customer.setCustomerStatus(request.getCustomerStatus());
        }

        Customer updated = customerRepository.save(customer);
        log.info("Customer updated successfully with ID: {}", id);

        return mapToResponse(updated);
    }

    // Delete customer
    public void delete(Long id) {
        log.info("Deleting customer with ID: {}", id);
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        customerRepository.delete(customer);
        log.info("Customer deleted successfully with ID: {}", id);
    }

    // Map Customer entity to CustomerResponse DTO
    private CustomerResponse mapToResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail())
                .mobile(customer.getMobile())
                .nationalId(customer.getNationalId())
                .address(customer.getAddress())
                .customerStatus(customer.getCustomerStatus())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }
}
