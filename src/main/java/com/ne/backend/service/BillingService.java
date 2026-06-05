package com.ne.backend.service;

import com.ne.backend.dto.bill.BillResponse;
import com.ne.backend.entity.Bill;
import com.ne.backend.entity.Customer;
import com.ne.backend.entity.Meter;
import com.ne.backend.entity.MeterReading;
import com.ne.backend.entity.Tariff;
import com.ne.backend.enums.BillStatus;
import com.ne.backend.enums.CustomerStatus;
import com.ne.backend.exception.ResourceNotFoundException;
import com.ne.backend.repository.BillRepository;
import com.ne.backend.repository.CustomerRepository;
import com.ne.backend.repository.MeterReadingRepository;
import com.ne.backend.repository.TariffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Service for managing bills
 * Customer extends User, so customer has firstName, lastName, email, mobile
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BillingService {

    private final BillRepository billRepository;
    private final CustomerRepository customerRepository;
    private final MeterReadingRepository meterReadingRepository;
    private final TariffRepository tariffRepository;
    private final NotificationService notificationService;

    // Generate a bill for a meter reading
    @Transactional
    public BillResponse generateBill(Long meterReadingId) {
        log.info("Generating bill for meter reading ID: {}", meterReadingId);

        MeterReading meterReading = meterReadingRepository.findById(meterReadingId)
                .orElseThrow(() -> new ResourceNotFoundException("Meter reading not found"));

        Meter meter = meterReading.getMeter();
        Customer customer = meter.getCustomer();

        // Check customer-specific status (not User status)
        if (customer.getCustomerStatus() != CustomerStatus.ACTIVE) {
            throw new RuntimeException("Cannot generate bill for inactive customer");
        }

        Tariff tariff = tariffRepository.findLatestActiveTariffByMeterTypeAndDate(
                meter.getMeterType(),
                meterReading.getReadingDate()
        ).orElseThrow(() -> new ResourceNotFoundException("No active tariff found for this meter type"));

        int month = meterReading.getReadingDate().getMonthValue();
        int year = meterReading.getReadingDate().getYear();

        List<Bill> existingBills = billRepository.findByCustomerAndMonthAndYear(
                customer.getId(), month, year
        );

        if (!existingBills.isEmpty()) {
            throw new RuntimeException("Bill already exists for this customer in the specified month/year");
        }

        double consumption = meterReading.getCurrentReading() - meterReading.getPreviousReading();
        BigDecimal consumptionCharge = tariff.getRatePerUnit()
                .multiply(BigDecimal.valueOf(consumption))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal fixedServiceCharge = tariff.getFixedServiceCharge() != null 
                ? tariff.getFixedServiceCharge() 
                : BigDecimal.ZERO;

        BigDecimal subTotal = consumptionCharge.add(fixedServiceCharge);

        BigDecimal vatAmount = tariff.getVatRate() != null 
                ? subTotal.multiply(tariff.getVatRate().divide(BigDecimal.valueOf(100)))
                        .setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal totalAmount = subTotal.add(vatAmount);

        // Apply customer surplus to reduce bill amount
        BigDecimal surplus = customer.getSurplus() != null ? customer.getSurplus() : BigDecimal.ZERO;
        BigDecimal finalAmount = totalAmount;
        BigDecimal appliedSurplus = BigDecimal.ZERO;
        
        if (surplus.compareTo(BigDecimal.ZERO) > 0) {
            if (surplus.compareTo(totalAmount) >= 0) {
                // Surplus covers entire bill
                appliedSurplus = totalAmount;
                finalAmount = BigDecimal.ZERO;
                customer.setSurplus(surplus.subtract(totalAmount));
                log.info("Customer surplus covers entire bill. Applied {} from surplus for customer ID: {}", appliedSurplus, customer.getId());
            } else {
                // Partial surplus application
                appliedSurplus = surplus;
                finalAmount = totalAmount.subtract(surplus);
                customer.setSurplus(BigDecimal.ZERO);
                log.info("Applied {} from customer surplus for customer ID: {}. Remaining bill amount: {}", appliedSurplus, customer.getId(), finalAmount);
            }
            customerRepository.save(customer);
        }

        LocalDate dueDate = LocalDate.of(year, month, 15).plusMonths(1);

        Bill bill = Bill.builder()
                .customer(customer)
                .meter(meter)
                .meterReading(meterReading)
                .tariff(tariff)
                .billingMonth(month)
                .billingYear(year)
                .previousReading(meterReading.getPreviousReading())
                .currentReading(meterReading.getCurrentReading())
                .consumption(consumption)
                .consumptionCharge(consumptionCharge)
                .fixedServiceCharge(fixedServiceCharge)
                .vatAmount(vatAmount)
                .penaltyAmount(BigDecimal.ZERO)
                .totalAmount(totalAmount)
                .amountPaid(appliedSurplus)
                .outstandingBalance(finalAmount)
                .dueDate(dueDate)
                .status(finalAmount.compareTo(BigDecimal.ZERO) == 0 ? BillStatus.PAID : BillStatus.PENDING)
                .build();

        Bill saved = billRepository.save(bill);
        log.info("Bill generated successfully with ID: {}", saved.getId());

        notificationService.createBillNotification(saved);

        return mapToResponse(saved);
    }

    // Get bill by ID
    public BillResponse getById(Long id) {
        log.info("Fetching bill by ID: {}", id);
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));
        return mapToResponse(bill);
    }

    // Get all bills with pagination
    public Page<BillResponse> getAll(Pageable pageable) {
        log.info("Fetching all bills with pagination");
        return billRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    // Search bills with filters
    public Page<BillResponse> search(
            Long customerId,
            Long meterId,
            BillStatus status,
            Integer billingMonth,
            Integer billingYear,
            Pageable pageable
    ) {
        log.info("Searching bills with filters");
        return billRepository.searchBills(customerId, meterId, status, billingMonth, billingYear, pageable)
                .map(this::mapToResponse);
    }

    // Get bills by customer
    public List<BillResponse> getByCustomer(Long customerId) {
        log.info("Fetching bills for customer: {}", customerId);
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        return billRepository.findByCustomerOrderByBillingYearDescBillingMonthDesc(customer).stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Approve a pending bill
    public BillResponse approveBill(Long id) {
        log.info("Approving bill with ID: {}", id);
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));

        if (bill.getStatus() != BillStatus.PENDING) {
            throw new RuntimeException("Only pending bills can be approved");
        }

        bill.setStatus(BillStatus.APPROVED);
        Bill updated = billRepository.save(bill);
        log.info("Bill approved successfully with ID: {}", id);

        return mapToResponse(updated);
    }

    // Delete a bill
    public void delete(Long id) {
        log.info("Deleting bill with ID: {}", id);
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));

        if (bill.getStatus() == BillStatus.PAID) {
            throw new RuntimeException("Cannot delete paid bills");
        }

        billRepository.delete(bill);
        log.info("Bill deleted successfully with ID: {}", id);
    }

    // Map Bill entity to BillResponse DTO
    private BillResponse mapToResponse(Bill bill) {
        Customer customer = bill.getCustomer();
        String customerName = customer.getFirstName() + " " + customer.getLastName();
        
        return BillResponse.builder()
                .id(bill.getId())
                .customerId(customer.getId())
                .customerName(customerName)
                .meterId(bill.getMeter().getId())
                .meterNumber(bill.getMeter().getMeterNumber())
                .meterType(bill.getMeter().getMeterType())
                .meterReadingId(bill.getMeterReading().getId())
                .billingMonth(bill.getBillingMonth())
                .billingYear(bill.getBillingYear())
                .previousReading(bill.getPreviousReading())
                .currentReading(bill.getCurrentReading())
                .consumption(bill.getConsumption())
                .consumptionCharge(bill.getConsumptionCharge())
                .fixedServiceCharge(bill.getFixedServiceCharge())
                .vatAmount(bill.getVatAmount())
                .penaltyAmount(bill.getPenaltyAmount())
                .totalAmount(bill.getTotalAmount())
                .amountPaid(bill.getAmountPaid())
                .outstandingBalance(bill.getOutstandingBalance())
                .dueDate(bill.getDueDate())
                .status(bill.getStatus())
                .createdAt(bill.getCreatedAt())
                .updatedAt(bill.getUpdatedAt())
                .build();
    }
}
