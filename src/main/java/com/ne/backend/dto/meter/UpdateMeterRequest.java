package com.ne.backend.dto.meter;

import com.ne.backend.enums.MeterStatus;
import com.ne.backend.enums.MeterType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMeterRequest {

    private String meterNumber;

    private MeterType meterType;

    private Long customerId;

    private LocalDate installationDate;

    private MeterStatus status;
}
