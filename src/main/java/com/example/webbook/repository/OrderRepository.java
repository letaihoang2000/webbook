package com.example.webbook.repository;

import com.example.webbook.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    // Find all orders by user ID
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
    List<Order> findByUserId(@Param("userId") UUID userId);

    // Find order by PayPal order ID
    @Query("SELECT o FROM Order o WHERE o.paypalOrderId = :paypalOrderId")
    Optional<Order> findByPaypalOrderId(@Param("paypalOrderId") String paypalOrderId);

    // Find orders by status
    @Query("SELECT o FROM Order o WHERE o.status = :status ORDER BY o.createdAt DESC")
    List<Order> findByStatus(@Param("status") String status);

    // Count orders by user
    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId")
    long countByUserId(@Param("userId") UUID userId);

    // Get total spent by user
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.user.id = :userId AND o.status = 'COMPLETED'")
    double getTotalSpentByUser(@Param("userId") UUID userId);
}