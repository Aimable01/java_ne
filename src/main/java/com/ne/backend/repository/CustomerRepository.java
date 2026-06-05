package com.ne.backend.repository;

import com.ne.backend.entity.Customer;
import com.ne.backend.enums.CustomerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Customer entity
 * Customer extends User, so it has access to User fields (firstName, lastName, email, mobile)
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // Find customer by national ID
    Optional<Customer> findByNationalId(String nationalId);

    // Find customer by email (inherited from User)
    Optional<Customer> findByEmail(String email);

    // Check if national ID exists
    boolean existsByNationalId(String nationalId);

    // Check if email exists (inherited from User)
    boolean existsByEmail(String email);

    // Find customers by customer-specific status
    Page<Customer> findByCustomerStatus(CustomerStatus customerStatus, Pageable pageable);

    // Search customers with filters on both User and Customer fields
    @Query("SELECT c FROM Customer c WHERE " +
           "(:firstName IS NULL OR LOWER(c.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))) AND " +
           "(:lastName IS NULL OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))) AND " +
           "(:nationalId IS NULL OR c.nationalId LIKE CONCAT('%', :nationalId, '%')) AND " +
           "(:email IS NULL OR LOWER(c.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:mobile IS NULL OR c.mobile LIKE CONCAT('%', :mobile, '%')) AND " +
           "(:customerStatus IS NULL OR c.customerStatus = :customerStatus)")
    Page<Customer> searchCustomers(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("nationalId") String nationalId,
            @Param("email") String email,
            @Param("mobile") String mobile,
            @Param("customerStatus") CustomerStatus customerStatus,
            Pageable pageable
    );
}
