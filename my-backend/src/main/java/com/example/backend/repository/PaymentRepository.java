package com.example.backend.repository;

import com.example.backend.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    long countByStatus(Payment.PaymentStatus status);
    
    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :from AND :to")
    List<Payment> findPaymentsByDateRange(@Param("from") LocalDateTime from, 
                                        @Param("to") LocalDateTime to);
    
    @Query("SELECT SUM(p.amount) FROM Payment p " +
           "WHERE p.status = 'PAID' AND p.paidAt BETWEEN :from AND :to")
    BigDecimal getTotalRevenueByDateRange(@Param("from") LocalDateTime from,
                                          @Param("to") LocalDateTime to);
    
    @Query("SELECT p.reservation.hotel.id, p.reservation.hotel.name, " +
           "SUM(p.amount), COUNT(p.reservation) " +
           "FROM Payment p " +
           "WHERE p.status = 'PAID' AND p.paidAt BETWEEN :from AND :to " +
           "GROUP BY p.reservation.hotel.id, p.reservation.hotel.name " +
           "ORDER BY SUM(p.amount) DESC")
    List<Object[]> getHotelSettlementsByDateRange(@Param("from") LocalDateTime from,
                                                  @Param("to") LocalDateTime to);
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'PAID'")
    BigDecimal getTotalRevenue();
    
    @Query("SELECT DATE(p.paidAt), SUM(p.amount) " +
           "FROM Payment p " +
           "WHERE p.status = 'PAID' AND p.paidAt BETWEEN :from AND :to " +
           "GROUP BY DATE(p.paidAt) " +
           "ORDER BY DATE(p.paidAt)")
    List<Object[]> getDailyRevenueByDateRange(@Param("from") LocalDateTime from,
                                             @Param("to") LocalDateTime to);

    @Query("SELECT SUM(p.amount) FROM Payment p " +
           "WHERE p.status = 'PAID' AND p.reservation.hotel.id = :hotelId")
    Double getTotalRevenueByHotel(@Param("hotelId") Long hotelId);

    @Query("SELECT SUM(p.amount) FROM Payment p " +
           "WHERE p.status = 'PAID' AND p.reservation.room.id = :roomId")
    Double getTotalRevenueByRoom(@Param("roomId") Long roomId);
}