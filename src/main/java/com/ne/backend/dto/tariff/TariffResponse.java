package com.ne.backend.dto.tariff;

import com.ne.backend.enums.MeterType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TariffResponse {

    private Long id;
    private MeterType meterType;
    private Integer version;
    private LocalDate effectiveDate;
    private BigDecimal ratePerUnit;
    private BigDecimal fixedServiceCharge;
    private BigDecimal vatRate;
    private BigDecimal latePaymentPenaltyRate;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
