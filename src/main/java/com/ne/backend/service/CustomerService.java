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
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerResponse create(CreateCustomerRequest request) {
        log.info("Creating customer with national ID: {}", request.getNationalId());

        if (customerRepository.existsByNationalId(request.getNationalId())) {
            throw new RuntimeException("Customer with this national ID already exists");
        }

        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Customer with this email already exists");
        }

        Customer customer = Customer.builder()
                .fullName(request.getFullName())
                .nationalId(request.getNationalId())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .status(request.getStatus())
                .build();

        Customer saved = customerRepository.save(customer);
        log.info("Customer created successfully with ID: {}", saved.getId());

        return mapToResponse(saved);
    }

    public CustomerResponse getById(Long id) {
        log.info("Fetching customer by ID: {}", id);
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        return mapToResponse(customer);
    }

    public CustomerResponse getByNationalId(String nationalId) {
        log.info("Fetching customer by national ID: {}", nationalId);
        Customer customer = customerRepository.findByNationalId(nationalId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        return mapToResponse(customer);
    }

    public Page<CustomerResponse> getAll(Pageable pageable) {
        log.info("Fetching all customers with pagination");
        return customerRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    public Page<CustomerResponse> search(
            String fullName,
            String nationalId,
            String email,
            String phoneNumber,
            CustomerStatus status,
            Pageable pageable
    ) {
        log.info("Searching customers with filters");
        return customerRepository.searchCustomers(fullName, nationalId, email, phoneNumber, status, pageable)
                .map(this::mapToResponse);
    }

    public CustomerResponse update(Long id, UpdateCustomerRequest request) {
        log.info("Updating customer with ID: {}", id);
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        if (request.getFullName() != null) {
            customer.setFullName(request.getFullName());
        }
        if (request.getNationalId() != null && !request.getNationalId().equals(customer.getNationalId())) {
            if (customerRepository.existsByNationalId(request.getNationalId())) {
                throw new RuntimeException("Customer with this national ID already exists");
            }
            customer.setNationalId(request.getNationalId());
        }
        if (request.getEmail() != null && !request.getEmail().equals(customer.getEmail())) {
            if (customerRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Customer with this email already exists");
            }
            customer.setEmail(request.getEmail());
        }
        if (request.getPhoneNumber() != null) {
            customer.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getAddress() != null) {
            customer.setAddress(request.getAddress());
        }
        if (request.getStatus() != null) {
            customer.setStatus(request.getStatus());
        }

        Customer updated = customerRepository.save(customer);
        log.info("Customer updated successfully with ID: {}", id);

        return mapToResponse(updated);
    }

    public void delete(Long id) {
        log.info("Deleting customer with ID: {}", id);
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        customerRepository.delete(customer);
        log.info("Customer deleted successfully with ID: {}", id);
    }

    private CustomerResponse mapToResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .fullName(customer.getFullName())
                .nationalId(customer.getNationalId())
                .email(customer.getEmail())
                .phoneNumber(customer.getPhoneNumber())
                .address(customer.getAddress())
                .status(customer.getStatus())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }
}
