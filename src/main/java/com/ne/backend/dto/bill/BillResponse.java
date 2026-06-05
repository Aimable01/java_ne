package com.ne.backend.dto.bill;

import com.ne.backend.enums.BillStatus;
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
public class BillResponse {

    private Long id;
    private Long customerId;
    private String customerName;
    private Long meterId;
    private String meterNumber;
    private MeterType meterType;
    private Long meterReadingId;
    private Integer billingMonth;
    private Integer billingYear;
    private Double previousReading;
    private Double currentReading;
    private Double consumption;
    private BigDecimal consumptionCharge;
    private BigDecimal fixedServiceCharge;
    private BigDecimal vatAmount;
    private BigDecimal penaltyAmount;
    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private BigDecimal outstandingBalance;
    private LocalDate dueDate;
    private BillStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
