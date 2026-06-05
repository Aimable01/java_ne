package com.ne.backend.dto.meterreading;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMeterReadingRequest {

    @NotNull(message = "Meter ID is required")
    private Long meterId;

    @NotNull(message = "Previous reading is required")
    @Positive(message = "Previous reading must be positive")
    private Double previousReading;

    @NotNull(message = "Current reading is required")
    @Positive(message = "Current reading must be positive")
    private Double currentReading;

    @NotNull(message = "Reading date is required")
    private LocalDate readingDate;
}
