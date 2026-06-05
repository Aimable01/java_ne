package com.ne.backend.service;

import com.ne.backend.dto.tariff.CreateTariffRequest;
import com.ne.backend.dto.tariff.TariffResponse;
import com.ne.backend.entity.Tariff;
import com.ne.backend.enums.MeterType;
import com.ne.backend.exception.ResourceNotFoundException;
import com.ne.backend.repository.TariffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TariffService {

    private final TariffRepository tariffRepository;

    public TariffResponse create(CreateTariffRequest request) {
        log.info("Creating tariff for meter type: {}", request.getMeterType());

        List<Tariff> existingTariffs = tariffRepository.findActiveTariffsByMeterTypeAndDate(
                request.getMeterType(), 
                request.getEffectiveDate()
        );

        if (!existingTariffs.isEmpty()) {
            int maxVersion = existingTariffs.stream()
                    .mapToInt(Tariff::getVersion)
                    .max()
                    .orElse(0);
            if (request.getVersion() <= maxVersion) {
                throw new RuntimeException("Tariff version must be greater than existing versions");
            }
        }

        Tariff tariff = Tariff.builder()
                .meterType(request.getMeterType())
                .version(request.getVersion())
                .effectiveDate(request.getEffectiveDate())
                .ratePerUnit(request.getRatePerUnit())
                .fixedServiceCharge(request.getFixedServiceCharge())
                .vatRate(request.getVatRate())
                .latePaymentPenaltyRate(request.getLatePaymentPenaltyRate())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        Tariff saved = tariffRepository.save(tariff);
        log.info("Tariff created successfully with ID: {}", saved.getId());

        return mapToResponse(saved);
    }

    public TariffResponse getById(Long id) {
        log.info("Fetching tariff by ID: {}", id);
        Tariff tariff = tariffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tariff not found"));
        return mapToResponse(tariff);
    }

    public Page<TariffResponse> getAll(Pageable pageable) {
        log.info("Fetching all tariffs with pagination");
        return tariffRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    public Page<TariffResponse> search(
            MeterType meterType,
            Boolean active,
            LocalDate effectiveDate,
            Pageable pageable
    ) {
        log.info("Searching tariffs with filters");
        return tariffRepository.searchTariffs(meterType, active, effectiveDate, pageable)
                .map(this::mapToResponse);
    }

    public List<TariffResponse> getActiveTariffsByMeterType(MeterType meterType) {
        log.info("Fetching active tariffs for meter type: {}", meterType);
        return tariffRepository.findActiveTariffsByMeterTypeAndDate(meterType, LocalDate.now())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public TariffResponse getLatestActiveTariffByMeterType(MeterType meterType) {
        log.info("Fetching latest active tariff for meter type: {}", meterType);
        Tariff tariff = tariffRepository.findLatestActiveTariffByMeterTypeAndDate(meterType, LocalDate.now())
                .orElseThrow(() -> new ResourceNotFoundException("No active tariff found for this meter type"));
        return mapToResponse(tariff);
    }

    public void delete(Long id) {
        log.info("Deleting tariff with ID: {}", id);
        Tariff tariff = tariffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tariff not found"));

        tariffRepository.delete(tariff);
        log.info("Tariff deleted successfully with ID: {}", id);
    }

    private TariffResponse mapToResponse(Tariff tariff) {
        return TariffResponse.builder()
                .id(tariff.getId())
                .meterType(tariff.getMeterType())
                .version(tariff.getVersion())
                .effectiveDate(tariff.getEffectiveDate())
                .ratePerUnit(tariff.getRatePerUnit())
                .fixedServiceCharge(tariff.getFixedServiceCharge())
                .vatRate(tariff.getVatRate())
                .latePaymentPenaltyRate(tariff.getLatePaymentPenaltyRate())
                .active(tariff.getActive())
                .createdAt(tariff.getCreatedAt())
                .updatedAt(tariff.getUpdatedAt())
                .build();
    }
}
