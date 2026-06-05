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

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByNationalId(String nationalId);

    Optional<Customer> findByEmail(String email);

    boolean existsByNationalId(String nationalId);

    boolean existsByEmail(String email);

    Page<Customer> findByStatus(CustomerStatus status, Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE " +
           "(:fullName IS NULL OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', :fullName, '%'))) AND " +
           "(:nationalId IS NULL OR c.nationalId LIKE CONCAT('%', :nationalId, '%')) AND " +
           "(:email IS NULL OR LOWER(c.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:phoneNumber IS NULL OR c.phoneNumber LIKE CONCAT('%', :phoneNumber, '%')) AND " +
           "(:status IS NULL OR c.status = :status)")
    Page<Customer> searchCustomers(
            @Param("fullName") String fullName,
            @Param("nationalId") String nationalId,
            @Param("email") String email,
            @Param("phoneNumber") String phoneNumber,
            @Param("status") CustomerStatus status,
            Pageable pageable
    );
}
