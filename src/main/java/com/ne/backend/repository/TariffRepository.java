package com.ne.backend.repository;

import com.ne.backend.entity.Tariff;
import com.ne.backend.enums.MeterType;
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
public interface TariffRepository extends JpaRepository<Tariff, Long> {

    List<Tariff> findByMeterTypeOrderByEffectiveDateDescVersionDesc(MeterType meterType);

    @Query("SELECT t FROM Tariff t WHERE t.meterType = :meterType AND t.effectiveDate <= :date ORDER BY t.effectiveDate DESC, t.version DESC")
    List<Tariff> findActiveTariffsByMeterTypeAndDate(
            @Param("meterType") MeterType meterType,
            @Param("date") LocalDate date
    );

    @Query("SELECT t FROM Tariff t WHERE t.meterType = :meterType AND t.effectiveDate <= :date AND t.active = true ORDER BY t.effectiveDate DESC, t.version DESC")
    Optional<Tariff> findLatestActiveTariffByMeterTypeAndDate(
            @Param("meterType") MeterType meterType,
            @Param("date") LocalDate date
    );

    Page<Tariff> findByActiveTrue(Pageable pageable);

    @Query("SELECT t FROM Tariff t WHERE " +
           "(:meterType IS NULL OR t.meterType = :meterType) AND " +
           "(:active IS NULL OR t.active = :active) AND " +
           "(:effectiveDate IS NULL OR t.effectiveDate >= :effectiveDate)")
    Page<Tariff> searchTariffs(
            @Param("meterType") MeterType meterType,
            @Param("active") Boolean active,
            @Param("effectiveDate") LocalDate effectiveDate,
            Pageable pageable
    );
}
