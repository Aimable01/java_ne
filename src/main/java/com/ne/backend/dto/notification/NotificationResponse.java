package com.ne.backend.dto.notification;

import com.ne.backend.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private Long customerId;
    private String customerName;
    private NotificationType notificationType;
    private String subject;
    private String message;
    private Long relatedBillId;
    private Long relatedPaymentId;
    private Boolean read;
    private Boolean emailSent;
    private LocalDateTime emailSentAt;
    private LocalDateTime createdAt;
}
