package com.ne.backend.service;

import com.ne.backend.dto.meter.CreateMeterRequest;
import com.ne.backend.dto.meter.MeterResponse;
import com.ne.backend.dto.meter.UpdateMeterRequest;
import com.ne.backend.entity.Customer;
import com.ne.backend.entity.Meter;
import com.ne.backend.enums.MeterStatus;
import com.ne.backend.enums.MeterType;
import com.ne.backend.exception.ResourceNotFoundException;
import com.ne.backend.repository.CustomerRepository;
import com.ne.backend.repository.MeterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MeterService {

    private final MeterRepository meterRepository;
    private final CustomerRepository customerRepository;

    public MeterResponse create(CreateMeterRequest request) {
        log.info("Creating meter with number: {}", request.getMeterNumber());

        if (meterRepository.existsByMeterNumber(request.getMeterNumber())) {
            throw new RuntimeException("Meter with this number already exists");
        }

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Meter meter = Meter.builder()
                .meterNumber(request.getMeterNumber())
                .meterType(request.getMeterType())
                .customer(customer)
                .installationDate(request.getInstallationDate())
                .status(request.getStatus())
                .build();

        Meter saved = meterRepository.save(meter);
        log.info("Meter created successfully with ID: {}", saved.getId());

        return mapToResponse(saved);
    }

    public MeterResponse getById(Long id) {
        log.info("Fetching meter by ID: {}", id);
        Meter meter = meterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meter not found"));
        return mapToResponse(meter);
    }

    public MeterResponse getByMeterNumber(String meterNumber) {
        log.info("Fetching meter by number: {}", meterNumber);
        Meter meter = meterRepository.findByMeterNumber(meterNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Meter not found"));
        return mapToResponse(meter);
    }

    public Page<MeterResponse> getAll(Pageable pageable) {
        log.info("Fetching all meters with pagination");
        return meterRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    public Page<MeterResponse> search(
            String meterNumber,
            MeterType meterType,
            MeterStatus status,
            Long customerId,
            Pageable pageable
    ) {
        log.info("Searching meters with filters");
        return meterRepository.searchMeters(meterNumber, meterType, status, customerId, pageable)
                .map(this::mapToResponse);
    }

    public Page<MeterResponse> getByCustomer(Long customerId, Pageable pageable) {
        log.info("Fetching meters for customer: {}", customerId);
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        
        List<Meter> meters = meterRepository.findByCustomer(customer);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), meters.size());
        
        List<MeterResponse> responses = meters.subList(start, end).stream()
                .map(this::mapToResponse)
                .collect(java.util.stream.Collectors.toList());
        
        return new org.springframework.data.domain.PageImpl<>(responses, pageable, meters.size());
    }

    public MeterResponse update(Long id, UpdateMeterRequest request) {
        log.info("Updating meter with ID: {}", id);
        Meter meter = meterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meter not found"));

        if (request.getMeterNumber() != null && !request.getMeterNumber().equals(meter.getMeterNumber())) {
            if (meterRepository.existsByMeterNumber(request.getMeterNumber())) {
                throw new RuntimeException("Meter with this number already exists");
            }
            meter.setMeterNumber(request.getMeterNumber());
        }
        if (request.getMeterType() != null) {
            meter.setMeterType(request.getMeterType());
        }
        if (request.getCustomerId() != null) {
            Customer customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
            meter.setCustomer(customer);
        }
        if (request.getInstallationDate() != null) {
            meter.setInstallationDate(request.getInstallationDate());
        }
        if (request.getStatus() != null) {
            meter.setStatus(request.getStatus());
        }

        Meter updated = meterRepository.save(meter);
        log.info("Meter updated successfully with ID: {}", id);

        return mapToResponse(updated);
    }

    public void delete(Long id) {
        log.info("Deleting meter with ID: {}", id);
        Meter meter = meterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meter not found"));

        meterRepository.delete(meter);
        log.info("Meter deleted successfully with ID: {}", id);
    }

    private MeterResponse mapToResponse(Meter meter) {
        return MeterResponse.builder()
                .id(meter.getId())
                .meterNumber(meter.getMeterNumber())
                .meterType(meter.getMeterType())
                .customerId(meter.getCustomer().getId())
                .customerName(meter.getCustomer().getFullName())
                .installationDate(meter.getInstallationDate())
                .status(meter.getStatus())
                .createdAt(meter.getCreatedAt())
                .updatedAt(meter.getUpdatedAt())
                .build();
    }
}
