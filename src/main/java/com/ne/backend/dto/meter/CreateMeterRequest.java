package com.ne.backend.dto.meter;

import com.ne.backend.enums.MeterStatus;
import com.ne.backend.enums.MeterType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMeterRequest {

    @NotBlank(message = "Meter number is required")
    private String meterNumber;

    @NotNull(message = "Meter type is required")
    private MeterType meterType;

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Installation date is required")
    private LocalDate installationDate;

    @NotNull(message = "Status is required")
    private MeterStatus status;
}
