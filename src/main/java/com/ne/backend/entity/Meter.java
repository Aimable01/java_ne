package com.ne.backend.entity;

import com.ne.backend.enums.MeterStatus;
import com.ne.backend.enums.MeterType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "meters")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Meter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Meter number is required")
    @Column(nullable = false, unique = true)
    private String meterNumber;

    @NotNull(message = "Meter type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MeterType meterType;

    @NotNull(message = "Customer is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @NotNull(message = "Installation date is required")
    @Column(nullable = false)
    private LocalDate installationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MeterStatus status = MeterStatus.ACTIVE;

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
