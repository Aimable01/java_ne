package com.ne.backend.repository;

import com.ne.backend.entity.Bill;
import com.ne.backend.entity.Customer;
import com.ne.backend.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByReferenceNumber(String referenceNumber);

    boolean existsByReferenceNumber(String referenceNumber);

    List<Payment> findByBill(Bill bill);

    @Query("SELECT p FROM Payment p WHERE p.bill.customer.id = :customerId")
    List<Payment> findByCustomer(@Param("customerId") Long customerId);

    @Query("SELECT p FROM Payment p WHERE " +
           "(:billId IS NULL OR p.bill.id = :billId) AND " +
           "(:paymentMethod IS NULL OR p.paymentMethod = :paymentMethod) AND " +
           "(:startDate IS NULL OR p.paymentDate >= :startDate) AND " +
           "(:endDate IS NULL OR p.paymentDate <= :endDate)")
    Page<Payment> searchPayments(
            @Param("billId") Long billId,
            @Param("paymentMethod") String paymentMethod,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    @Query("SELECT COALESCE(SUM(p.amountPaid), 0) FROM Payment p WHERE p.bill.id = :billId")
    java.math.BigDecimal getTotalPaidByBill(@Param("billId") Long billId);
}
