package com.ne.backend.dto.tariff;

import com.ne.backend.enums.MeterType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTariffRequest {

    @NotNull(message = "Meter type is required")
    private MeterType meterType;

    @NotNull(message = "Version is required")
    private Integer version;

    @NotNull(message = "Effective date is required")
    private LocalDate effectiveDate;

    @NotNull(message = "Rate per unit is required")
    @DecimalMin(value = "0.01", message = "Rate per unit must be greater than 0")
    private BigDecimal ratePerUnit;

    @PositiveOrZero(message = "Fixed service charge must be positive or zero")
    private BigDecimal fixedServiceCharge;

    @PositiveOrZero(message = "VAT rate must be positive or zero")
    private BigDecimal vatRate;

    @PositiveOrZero(message = "Late payment penalty rate must be positive or zero")
    private BigDecimal latePaymentPenaltyRate;

    private Boolean active;
}
