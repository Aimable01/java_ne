package com.ne.backend.dto.payment;

import com.ne.backend.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for payment response
 * Includes customerId for customer-specific access control
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private Long id;
    private Long customerId;
    private String customerName;
    private Long billId;
    private String referenceNumber;
    private BigDecimal amountPaid;
    private PaymentMethod paymentMethod;
    private String transactionId;
    private String notes;
    private LocalDate paymentDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
