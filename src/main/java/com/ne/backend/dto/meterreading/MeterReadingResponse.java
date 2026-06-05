package com.ne.backend.dto.meterreading;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeterReadingResponse {

    private Long id;
    private Long meterId;
    private String meterNumber;
    private String meterType;
    private Double previousReading;
    private Double currentReading;
    private Double consumption;
    private LocalDate readingDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
