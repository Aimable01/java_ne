package com.ne.backend.service;

import com.ne.backend.dto.payment.CreatePaymentRequest;
import com.ne.backend.dto.payment.PaymentResponse;
import com.ne.backend.entity.Bill;
import com.ne.backend.entity.Customer;
import com.ne.backend.entity.Payment;
import com.ne.backend.enums.BillStatus;
import com.ne.backend.exception.ResourceNotFoundException;
import com.ne.backend.repository.BillRepository;
import com.ne.backend.repository.CustomerRepository;
import com.ne.backend.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BillRepository billRepository;
    private final CustomerRepository customerRepository;
    private final NotificationService notificationService;

    @Transactional
    public PaymentResponse create(CreatePaymentRequest request) {
        log.info("Creating payment for bill ID: {}", request.getBillId());

        if (paymentRepository.existsByReferenceNumber(request.getReferenceNumber())) {
            throw new RuntimeException("Payment with this reference number already exists");
        }

        Bill bill = billRepository.findById(request.getBillId())
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));

        if (bill.getStatus() == BillStatus.CANCELLED) {
            throw new RuntimeException("Cannot make payment for cancelled bills");
        }

        // Allow overpayments - surplus will be tracked on customer
        // if (request.getAmountPaid().compareTo(bill.getOutstandingBalance()) > 0) {
        //     throw new RuntimeException("Payment amount cannot exceed outstanding balance");
        // }

        Payment payment = Payment.builder()
                .bill(bill)
                .referenceNumber(request.getReferenceNumber())
                .amountPaid(request.getAmountPaid())
                .paymentMethod(request.getPaymentMethod())
                .transactionId(request.getTransactionId())
                .notes(request.getNotes())
                .paymentDate(request.getPaymentDate())
                .build();

        Payment saved = paymentRepository.save(payment);

        updateBillBalance(bill);

        notificationService.createPaymentNotification(saved);

        log.info("Payment created successfully with ID: {}", saved.getId());

        return mapToResponse(saved);
    }

    @Transactional
    public void updateBillBalance(Bill bill) {
        BigDecimal totalPaid = paymentRepository.getTotalPaidByBill(bill.getId());
        bill.setAmountPaid(totalPaid);
        bill.setOutstandingBalance(bill.getTotalAmount().subtract(totalPaid));

        Customer customer = bill.getCustomer();
        
        // Handle overpayment - add surplus to customer account
        if (totalPaid.compareTo(bill.getTotalAmount()) > 0) {
            BigDecimal overpayment = totalPaid.subtract(bill.getTotalAmount());
            customer.setSurplus(customer.getSurplus().add(overpayment));
            bill.setOutstandingBalance(BigDecimal.ZERO);
            bill.setStatus(BillStatus.PAID);
            log.info("Overpayment detected. Added {} to customer surplus for customer ID: {}", overpayment, customer.getId());
        } 
        // Handle partial payment - notify customer of remaining balance
        else if (bill.getOutstandingBalance().compareTo(BigDecimal.ZERO) > 0) {
            bill.setStatus(BillStatus.PARTIALLY_PAID);
            log.info("Partial payment. Outstanding balance: {} for bill ID: {}", bill.getOutstandingBalance(), bill.getId());
            // Send reminder notification for remaining balance
            notificationService.createPartialPaymentReminder(bill);
        }
        // Full payment
        else {
            bill.setStatus(BillStatus.PAID);
            bill.setOutstandingBalance(BigDecimal.ZERO);
            notificationService.createFullPaymentNotification(bill);
        }

        billRepository.save(bill);
        customerRepository.save(customer);
        log.info("Bill balance updated for bill ID: {}", bill.getId());
    }

    public PaymentResponse getById(Long id) {
        log.info("Fetching payment by ID: {}", id);
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        return mapToResponse(payment);
    }

    public PaymentResponse getByReferenceNumber(String referenceNumber) {
        log.info("Fetching payment by reference number: {}", referenceNumber);
        Payment payment = paymentRepository.findByReferenceNumber(referenceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        return mapToResponse(payment);
    }

    public Page<PaymentResponse> getAll(Pageable pageable) {
        log.info("Fetching all payments with pagination");
        return paymentRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    public Page<PaymentResponse> search(
            Long billId,
            String paymentMethod,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) {
        log.info("Searching payments with filters");
        return paymentRepository.searchPayments(billId, paymentMethod, startDate, endDate, pageable)
                .map(this::mapToResponse);
    }

    public List<PaymentResponse> getByBill(Long billId) {
        log.info("Fetching payments for bill: {}", billId);
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));
        return paymentRepository.findByBill(bill).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<PaymentResponse> getByCustomer(Long customerId) {
        log.info("Fetching payments for customer: {}", customerId);
        return paymentRepository.findByCustomer(customerId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public void delete(Long id) {
        log.info("Deleting payment with ID: {}", id);
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        Bill bill = payment.getBill();
        
        paymentRepository.delete(payment);
        
        updateBillBalance(bill);
        
        log.info("Payment deleted successfully with ID: {}", id);
    }

    private PaymentResponse mapToResponse(Payment payment) {
        Customer customer = payment.getBill().getCustomer();
        String customerName = customer.getFirstName() + " " + customer.getLastName();
        
        return PaymentResponse.builder()
                .id(payment.getId())
                .customerId(customer.getId())
                .customerName(customerName)
                .billId(payment.getBill().getId())
                .referenceNumber(payment.getReferenceNumber())
                .amountPaid(payment.getAmountPaid())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionId())
                .notes(payment.getNotes())
                .paymentDate(payment.getPaymentDate())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
