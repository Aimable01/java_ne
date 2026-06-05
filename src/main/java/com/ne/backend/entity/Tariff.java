package com.ne.backend.entity;

import com.ne.backend.enums.MeterType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
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
@Table(name = "tariffs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tariff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Meter type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MeterType meterType;

    @NotNull(message = "Version is required")
    @Column(nullable = false)
    private Integer version;

    @NotNull(message = "Effective date is required")
    @Column(nullable = false)
    private LocalDate effectiveDate;

    @NotNull(message = "Rate per unit is required")
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal ratePerUnit;

    @DecimalMin(value = "0.0", inclusive = true)
    @Column(precision = 10, scale = 2)
    private BigDecimal fixedServiceCharge;

    @DecimalMin(value = "0.0", inclusive = true)
    @Column(precision = 5, scale = 2)
    private BigDecimal vatRate;

    @DecimalMin(value = "0.0", inclusive = true)
    @Column(precision = 5, scale = 2)
    private BigDecimal latePaymentPenaltyRate;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

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
