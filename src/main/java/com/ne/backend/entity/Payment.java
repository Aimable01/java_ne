package com.ne.backend.entity;

import com.ne.backend.enums.PaymentMethod;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Bill is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false)
    private Bill bill;

    @NotBlank(message = "Reference number is required")
    @Column(nullable = false, unique = true)
    private String referenceNumber;

    @NotNull(message = "Amount paid is required")
    @DecimalMin(value = "0.01", message = "Amount paid must be greater than 0")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amountPaid;

    @NotNull(message = "Payment method is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Column
    private String transactionId;

    @Column
    private String notes;

    @NotNull(message = "Payment date is required")
    @Column(nullable = false)
    private LocalDate paymentDate;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
