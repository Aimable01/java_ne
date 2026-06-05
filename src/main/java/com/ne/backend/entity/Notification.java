package com.ne.backend.entity;

import com.ne.backend.enums.NotificationType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Customer is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @NotNull(message = "Notification type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType notificationType;

    @NotBlank(message = "Subject is required")
    @Column(nullable = false)
    private String subject;

    @NotBlank(message = "Message is required")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column
    private Long relatedBillId;

    @Column
    private Long relatedPaymentId;

    @Column(nullable = false)
    @Builder.Default
    private Boolean read = false;

    @Column
    @Builder.Default
    private Boolean emailSent = false;

    @Column
    private LocalDateTime emailSentAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
