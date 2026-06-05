package com.ne.backend.repository;

import com.ne.backend.entity.Customer;
import com.ne.backend.entity.Notification;
import com.ne.backend.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByCustomerOrderByCreatedAtDesc(Customer customer);

    List<Notification> findByCustomerAndReadFalseOrderByCreatedAtDesc(Customer customer);

    Page<Notification> findByCustomerOrderByCreatedAtDesc(Customer customer, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE " +
           "(:customerId IS NULL OR n.customer.id = :customerId) AND " +
           "(:notificationType IS NULL OR n.notificationType = :notificationType) AND " +
           "(:read IS NULL OR n.read = :read)")
    Page<Notification> searchNotifications(
            @Param("customerId") Long customerId,
            @Param("notificationType") NotificationType notificationType,
            @Param("read") Boolean read,
            Pageable pageable
    );

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.customer.id = :customerId AND n.read = false")
    Long countUnreadByCustomer(@Param("customerId") Long customerId);
}
