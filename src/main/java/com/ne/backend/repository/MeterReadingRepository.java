package com.ne.backend.repository;

import com.ne.backend.entity.Meter;
import com.ne.backend.entity.MeterReading;
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
public interface MeterReadingRepository extends JpaRepository<MeterReading, Long> {

    Optional<MeterReading> findByMeterAndReadingDate(Meter meter, LocalDate readingDate);

    List<MeterReading> findByMeterOrderByReadingDateDesc(Meter meter);

    Optional<MeterReading> findFirstByMeterOrderByReadingDateDesc(Meter meter);

    @Query("SELECT mr FROM MeterReading mr WHERE " +
           "(:meterId IS NULL OR mr.meter.id = :meterId) AND " +
           "(:startDate IS NULL OR mr.readingDate >= :startDate) AND " +
           "(:endDate IS NULL OR mr.readingDate <= :endDate)")
    Page<MeterReading> searchMeterReadings(
            @Param("meterId") Long meterId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    @Query("SELECT mr FROM MeterReading mr WHERE " +
           "mr.meter.id = :meterId AND " +
           "EXTRACT(MONTH FROM mr.readingDate) = :month AND " +
           "EXTRACT(YEAR FROM mr.readingDate) = :year")
    Optional<MeterReading> findByMeterAndMonthAndYear(
            @Param("meterId") Long meterId,
            @Param("month") int month,
            @Param("year") int year
    );
}
