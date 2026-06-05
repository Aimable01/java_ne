package com.ne.backend.repository;

import com.ne.backend.entity.Customer;
import com.ne.backend.entity.Meter;
import com.ne.backend.enums.MeterStatus;
import com.ne.backend.enums.MeterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MeterRepository extends JpaRepository<Meter, Long> {

    Optional<Meter> findByMeterNumber(String meterNumber);

    boolean existsByMeterNumber(String meterNumber);

    List<Meter> findByCustomer(Customer customer);

    List<Meter> findByCustomerAndStatus(Customer customer, MeterStatus status);

    List<Meter> findByMeterType(MeterType meterType);

    Page<Meter> findByStatus(MeterStatus status, Pageable pageable);

    @Query("SELECT m FROM Meter m WHERE " +
           "(:meterNumber IS NULL OR m.meterNumber LIKE CONCAT('%', :meterNumber, '%')) AND " +
           "(:meterType IS NULL OR m.meterType = :meterType) AND " +
           "(:status IS NULL OR m.status = :status) AND " +
           "(:customerId IS NULL OR m.customer.id = :customerId)")
    Page<Meter> searchMeters(
            @Param("meterNumber") String meterNumber,
            @Param("meterType") MeterType meterType,
            @Param("status") MeterStatus status,
            @Param("customerId") Long customerId,
            Pageable pageable
    );
}
