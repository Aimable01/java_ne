package com.ne.backend.service;

import com.ne.backend.dto.meterreading.CreateMeterReadingRequest;
import com.ne.backend.dto.meterreading.MeterReadingResponse;
import com.ne.backend.entity.Meter;
import com.ne.backend.entity.MeterReading;
import com.ne.backend.enums.MeterStatus;
import com.ne.backend.exception.ResourceNotFoundException;
import com.ne.backend.repository.MeterReadingRepository;
import com.ne.backend.repository.MeterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MeterReadingService {

    private final MeterReadingRepository meterReadingRepository;
    private final MeterRepository meterRepository;

    @Transactional
    public MeterReadingResponse create(CreateMeterReadingRequest request) {
        log.info("Creating meter reading for meter ID: {}", request.getMeterId());

        Meter meter = meterRepository.findById(request.getMeterId())
                .orElseThrow(() -> new ResourceNotFoundException("Meter not found"));

        if (meter.getStatus() != MeterStatus.ACTIVE) {
            throw new RuntimeException("Cannot create reading for inactive meter");
        }

        if (request.getCurrentReading() <= request.getPreviousReading()) {
            throw new RuntimeException("Current reading must be greater than previous reading");
        }

        int month = request.getReadingDate().getMonthValue();
        int year = request.getReadingDate().getYear();

        if (meterReadingRepository.findByMeterAndMonthAndYear(request.getMeterId(), month, year).isPresent()) {
            throw new RuntimeException("Reading already exists for this meter in the specified month/year");
        }

        MeterReading reading = MeterReading.builder()
                .meter(meter)
                .previousReading(request.getPreviousReading())
                .currentReading(request.getCurrentReading())
                .readingDate(request.getReadingDate())
                .build();

        MeterReading saved = meterReadingRepository.save(reading);
        log.info("Meter reading created successfully with ID: {}", saved.getId());

        return mapToResponse(saved);
    }

    public MeterReadingResponse getById(Long id) {
        log.info("Fetching meter reading by ID: {}", id);
        MeterReading reading = meterReadingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meter reading not found"));
        return mapToResponse(reading);
    }

    public Page<MeterReadingResponse> getAll(Pageable pageable) {
        log.info("Fetching all meter readings with pagination");
        return meterReadingRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    public Page<MeterReadingResponse> search(
            Long meterId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) {
        log.info("Searching meter readings with filters");
        return meterReadingRepository.searchMeterReadings(meterId, startDate, endDate, pageable)
                .map(this::mapToResponse);
    }

    public List<MeterReadingResponse> getByMeter(Long meterId) {
        log.info("Fetching meter readings for meter: {}", meterId);
        Meter meter = meterRepository.findById(meterId)
                .orElseThrow(() -> new ResourceNotFoundException("Meter not found"));
        return meterReadingRepository.findByMeterOrderByReadingDateDesc(meter).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public MeterReadingResponse getLatestByMeter(Long meterId) {
        log.info("Fetching latest meter reading for meter: {}", meterId);
        Meter meter = meterRepository.findById(meterId)
                .orElseThrow(() -> new ResourceNotFoundException("Meter not found"));
        MeterReading reading = meterReadingRepository.findFirstByMeterOrderByReadingDateDesc(meter)
                .orElseThrow(() -> new ResourceNotFoundException("No readings found for this meter"));
        return mapToResponse(reading);
    }

    public void delete(Long id) {
        log.info("Deleting meter reading with ID: {}", id);
        MeterReading reading = meterReadingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meter reading not found"));

        meterReadingRepository.delete(reading);
        log.info("Meter reading deleted successfully with ID: {}", id);
    }

    private MeterReadingResponse mapToResponse(MeterReading reading) {
        return MeterReadingResponse.builder()
                .id(reading.getId())
                .meterId(reading.getMeter().getId())
                .meterNumber(reading.getMeter().getMeterNumber())
                .meterType(reading.getMeter().getMeterType().name())
                .previousReading(reading.getPreviousReading())
                .currentReading(reading.getCurrentReading())
                .consumption(reading.getCurrentReading() - reading.getPreviousReading())
                .readingDate(reading.getReadingDate())
                .createdAt(reading.getCreatedAt())
                .updatedAt(reading.getUpdatedAt())
                .build();
    }
}
