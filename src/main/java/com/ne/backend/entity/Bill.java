package com.ne.backend.entity;

import com.ne.backend.enums.BillStatus;
import jakarta.persistence.*;
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
@Table(name = "bills")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Customer is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @NotNull(message = "Meter is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meter_id", nullable = false)
    private Meter meter;

    @NotNull(message = "Meter reading is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meter_reading_id", nullable = false)
    private MeterReading meterReading;

    @NotNull(message = "Tariff is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tariff_id", nullable = false)
    private Tariff tariff;

    @NotNull(message = "Billing month is required")
    @Column(nullable = false)
    private Integer billingMonth;

    @NotNull(message = "Billing year is required")
    @Column(nullable = false)
    private Integer billingYear;

    @NotNull(message = "Previous reading is required")
    @Column(nullable = false)
    private Double previousReading;

    @NotNull(message = "Current reading is required")
    @Column(nullable = false)
    private Double currentReading;

    @NotNull(message = "Consumption is required")
    @Column(nullable = false)
    private Double consumption;

    @NotNull(message = "Consumption charge is required")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal consumptionCharge;

    @Column(precision = 12, scale = 2)
    private BigDecimal fixedServiceCharge;

    @Column(precision = 12, scale = 2)
    private BigDecimal vatAmount;

    @Column(precision = 12, scale = 2)
    private BigDecimal penaltyAmount;

    @NotNull(message = "Total amount is required")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(precision = 12, scale = 2)
    private BigDecimal amountPaid;

    @Column(precision = 12, scale = 2)
    private BigDecimal outstandingBalance;

    @NotNull(message = "Due date is required")
    @Column(nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BillStatus status = BillStatus.PENDING;

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
