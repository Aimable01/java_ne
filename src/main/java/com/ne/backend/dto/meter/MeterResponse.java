package com.ne.backend.dto.meter;

import com.ne.backend.enums.MeterStatus;
import com.ne.backend.enums.MeterType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for meter response
 * Customer name is computed from firstName and lastName (inherited from User)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeterResponse {

    private Long id;
    private String meterNumber;
    private MeterType meterType;
    private Long customerId;
    private String customerName;
    private LocalDate installationDate;
    private MeterStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
