package com.ne.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "meter_readings", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"meter_id", "reading_date"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeterReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Meter is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meter_id", nullable = false)
    private Meter meter;

    @NotNull(message = "Previous reading is required")
    @Column(nullable = false)
    private Double previousReading;

    @NotNull(message = "Current reading is required")
    @Column(nullable = false)
    private Double currentReading;

    @NotNull(message = "Reading date is required")
    @Column(nullable = false)
    private LocalDate readingDate;

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
