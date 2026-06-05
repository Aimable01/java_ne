package com.ne.backend.repository;

import com.ne.backend.entity.Bill;
import com.ne.backend.entity.Customer;
import com.ne.backend.enums.BillStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    List<Bill> findByCustomer(Customer customer);

    List<Bill> findByCustomerOrderByBillingYearDescBillingMonthDesc(Customer customer);

    Page<Bill> findByStatus(BillStatus status, Pageable pageable);

    @Query("SELECT b FROM Bill b WHERE " +
           "(:customerId IS NULL OR b.customer.id = :customerId) AND " +
           "(:meterId IS NULL OR b.meter.id = :meterId) AND " +
           "(:status IS NULL OR b.status = :status) AND " +
           "(:billingMonth IS NULL OR b.billingMonth = :billingMonth) AND " +
           "(:billingYear IS NULL OR b.billingYear = :billingYear)")
    Page<Bill> searchBills(
            @Param("customerId") Long customerId,
            @Param("meterId") Long meterId,
            @Param("status") BillStatus status,
            @Param("billingMonth") Integer billingMonth,
            @Param("billingYear") Integer billingYear,
            Pageable pageable
    );

    @Query("SELECT b FROM Bill b WHERE b.customer.id = :customerId AND b.billingMonth = :month AND b.billingYear = :year")
    List<Bill> findByCustomerAndMonthAndYear(
            @Param("customerId") Long customerId,
            @Param("month") int month,
            @Param("year") int year
    );

    @Query("SELECT b FROM Bill b WHERE b.dueDate < :date AND b.status != 'PAID'")
    List<Bill> findOverdueBills(@Param("date") LocalDate date);

    @Query("SELECT b FROM Bill b WHERE b.outstandingBalance > 0 AND b.status != 'CANCELLED'")
    List<Bill> findUnpaidBills();
}
