package com.ne.backend.service;

import com.ne.backend.dto.notification.NotificationResponse;
import com.ne.backend.entity.Bill;
import com.ne.backend.entity.Customer;
import com.ne.backend.entity.Notification;
import com.ne.backend.entity.Payment;
import com.ne.backend.enums.BillStatus;
import com.ne.backend.enums.NotificationType;
import com.ne.backend.exception.ResourceNotFoundException;
import com.ne.backend.repository.CustomerRepository;
import com.ne.backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final CustomerRepository customerRepository;
    private final EmailService emailService;

    public NotificationResponse createBillNotification(Bill bill) {
        log.info("Creating bill notification for bill ID: {}", bill.getId());

        Customer customer = bill.getCustomer();
        YearMonth yearMonth = YearMonth.of(bill.getBillingYear(), bill.getBillingMonth());
        String monthYear = yearMonth.getMonth().name() + " " + yearMonth.getYear();

        String subject = "Your Utility Bill for " + monthYear;
        String message = String.format(
                "Dear %s,\n\nYour %s utility bill of %s FRW has been generated successfully.\n\n" +
                "Bill Details:\n" +
                "- Consumption: %.2f units\n" +
                "- Total Amount: %s FRW\n" +
                "- Due Date: %s\n\n" +
                "Please ensure payment is made before the due date to avoid late payment penalties.\n\n" +
                "Thank you for your business.",
                customer.getFullName(),
                monthYear,
                bill.getTotalAmount(),
                bill.getConsumption(),
                bill.getTotalAmount(),
                bill.getDueDate()
        );

        Notification notification = Notification.builder()
                .customer(customer)
                .notificationType(NotificationType.BILL_GENERATED)
                .subject(subject)
                .message(message)
                .relatedBillId(bill.getId())
                .build();

        Notification saved = notificationRepository.save(notification);
        
        sendEmailNotification(saved);

        return mapToResponse(saved);
    }

    public NotificationResponse createPaymentNotification(Payment payment) {
        log.info("Creating payment notification for payment ID: {}", payment.getId());

        Bill bill = payment.getBill();
        Customer customer = bill.getCustomer();
        YearMonth yearMonth = YearMonth.of(bill.getBillingYear(), bill.getBillingMonth());
        String monthYear = yearMonth.getMonth().name() + " " + yearMonth.getYear();

        String subject = "Payment Received for " + monthYear;
        String message = String.format(
                "Dear %s,\n\nYour payment of %s FRW for %s utility bill has been received successfully.\n\n" +
                "Payment Details:\n" +
                "- Amount Paid: %s FRW\n" +
                "- Payment Method: %s\n" +
                "- Payment Date: %s\n\n",
                customer.getFullName(),
                payment.getAmountPaid(),
                monthYear,
                payment.getAmountPaid(),
                payment.getPaymentMethod(),
                payment.getPaymentDate()
        );

        if (bill.getStatus() == BillStatus.PAID) {
            message += "Your bill has been fully paid. Thank you for your payment.\n\n";
        } else {
            message += String.format("Outstanding Balance: %s FRW\n\n", bill.getOutstandingBalance());
        }

        message += "Thank you for your business.";

        Notification notification = Notification.builder()
                .customer(customer)
                .notificationType(NotificationType.PAYMENT_RECEIVED)
                .subject(subject)
                .message(message)
                .relatedBillId(bill.getId())
                .relatedPaymentId(payment.getId())
                .build();

        Notification saved = notificationRepository.save(notification);
        
        sendEmailNotification(saved);

        return mapToResponse(saved);
    }

    public NotificationResponse createFullPaymentNotification(Bill bill) {
        log.info("Creating full payment notification for bill ID: {}", bill.getId());

        Customer customer = bill.getCustomer();
        YearMonth yearMonth = YearMonth.of(bill.getBillingYear(), bill.getBillingMonth());
        String monthYear = yearMonth.getMonth().name() + " " + yearMonth.getYear();

        String subject = "Bill Fully Paid - " + monthYear;
        String message = String.format(
                "Dear %s,\n\nYour %s utility bill of %s FRW has been successfully processed and fully paid.\n\n" +
                "Thank you for your prompt payment. We appreciate your business.",
                customer.getFullName(),
                monthYear,
                bill.getTotalAmount()
        );

        Notification notification = Notification.builder()
                .customer(customer)
                .notificationType(NotificationType.PAYMENT_CONFIRMED)
                .subject(subject)
                .message(message)
                .relatedBillId(bill.getId())
                .build();

        Notification saved = notificationRepository.save(notification);
        
        sendEmailNotification(saved);

        return mapToResponse(saved);
    }

    @Transactional
    public void sendEmailNotification(Notification notification) {
        try {
            emailService.sendEmail(
                    notification.getCustomer().getEmail(),
                    notification.getSubject(),
                    notification.getMessage()
            );
            
            notification.setEmailSent(true);
            notification.setEmailSentAt(java.time.LocalDateTime.now());
            notificationRepository.save(notification);
            
            log.info("Email notification sent successfully for notification ID: {}", notification.getId());
        } catch (Exception e) {
            log.error("Failed to send email notification for notification ID: {}", notification.getId(), e);
        }
    }

    public NotificationResponse getById(Long id) {
        log.info("Fetching notification by ID: {}", id);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        return mapToResponse(notification);
    }

    public Page<NotificationResponse> getAll(Pageable pageable) {
        log.info("Fetching all notifications with pagination");
        return notificationRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    public Page<NotificationResponse> search(
            Long customerId,
            NotificationType notificationType,
            Boolean read,
            Pageable pageable
    ) {
        log.info("Searching notifications with filters");
        return notificationRepository.searchNotifications(customerId, notificationType, read, pageable)
                .map(this::mapToResponse);
    }

    public List<NotificationResponse> getByCustomer(Long customerId) {
        log.info("Fetching notifications for customer: {}", customerId);
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        return notificationRepository.findByCustomerOrderByCreatedAtDesc(customer).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<NotificationResponse> getUnreadByCustomer(Long customerId) {
        log.info("Fetching unread notifications for customer: {}", customerId);
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        return notificationRepository.findByCustomerAndReadFalseOrderByCreatedAtDesc(customer).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public Long countUnreadByCustomer(Long customerId) {
        log.info("Counting unread notifications for customer: {}", customerId);
        return notificationRepository.countUnreadByCustomer(customerId);
    }

    public NotificationResponse markAsRead(Long id) {
        log.info("Marking notification as read: {}", id);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        notification.setRead(true);
        Notification updated = notificationRepository.save(notification);
        log.info("Notification marked as read: {}", id);

        return mapToResponse(updated);
    }

    public void delete(Long id) {
        log.info("Deleting notification with ID: {}", id);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        notificationRepository.delete(notification);
        log.info("Notification deleted successfully with ID: {}", id);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .customerId(notification.getCustomer().getId())
                .customerName(notification.getCustomer().getFullName())
                .notificationType(notification.getNotificationType())
                .subject(notification.getSubject())
                .message(notification.getMessage())
                .relatedBillId(notification.getRelatedBillId())
                .relatedPaymentId(notification.getRelatedPaymentId())
                .read(notification.getRead())
                .emailSent(notification.getEmailSent())
                .emailSentAt(notification.getEmailSentAt())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
