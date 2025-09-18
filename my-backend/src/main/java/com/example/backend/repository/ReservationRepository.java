package com.example.backend.repository;

import com.example.backend.domain.Reservation;
import com.example.backend.domain.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
    @Query("SELECT r FROM Reservation r " +
           "LEFT JOIN FETCH r.payments p " +
           "LEFT JOIN FETCH r.room rm " +
           "LEFT JOIN FETCH rm.hotel h " +
           "LEFT JOIN FETCH r.user u " +
           "WHERE (:reservationStatus IS NULL OR r.status = :reservationStatus) " +
           "AND (:paymentStatus IS NULL OR EXISTS (SELECT 1 FROM r.payments p2 WHERE p2.status = :paymentStatus))")
    Page<Reservation> findReservationsWithPayments(
            @Param("reservationStatus") Reservation.ReservationStatus reservationStatus,
            @Param("paymentStatus") Payment.PaymentStatus paymentStatus,
            Pageable pageable);
    
    @Query("SELECT r FROM Reservation r " +
           "LEFT JOIN FETCH r.payments p " +
           "LEFT JOIN FETCH r.room rm " +
           "LEFT JOIN FETCH rm.hotel h " +
           "LEFT JOIN FETCH r.user u " +
           "WHERE (:hotelName IS NULL OR LOWER(h.name) LIKE LOWER(CONCAT('%', :hotelName, '%'))) " +
           "AND (:userName IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :userName, '%'))) " +
           "AND (:reservationStatus IS NULL OR r.status = :reservationStatus) " +
           "AND (:paymentStatus IS NULL OR EXISTS (SELECT 1 FROM r.payments p2 WHERE CAST(p2.status AS string) = :paymentStatus))")
    Page<Reservation> findReservationsWithFilters(
            @Param("hotelName") String hotelName,
            @Param("userName") String userName,
            @Param("reservationStatus") Reservation.ReservationStatus reservationStatus,
            @Param("paymentStatus") String paymentStatus,
            Pageable pageable);
    
    @Query("SELECT r FROM Reservation r " +
           "LEFT JOIN FETCH r.payments p " +
           "LEFT JOIN FETCH r.room rm " +
           "LEFT JOIN FETCH rm.hotel h " +
           "LEFT JOIN FETCH r.user u " +
           "WHERE r.id = :reservationId")
    Optional<Reservation> findByIdWithDetails(@Param("reservationId") Long reservationId);
    
    long countByStatus(Reservation.ReservationStatus status);

    Integer countByHotelId(Long hotelId);

    Integer countByRoomId(Long roomId);

    @Query("SELECT MONTH(r.checkInDate), COUNT(r) " +
           "FROM Reservation r " +
           "WHERE r.hotel.id = :hotelId AND YEAR(r.checkInDate) = YEAR(CURRENT_DATE) " +
           "GROUP BY MONTH(r.checkInDate) " +
           "ORDER BY MONTH(r.checkInDate)")
    List<Object[]> getMonthlyReservationsByHotel(@Param("hotelId") Long hotelId);
}