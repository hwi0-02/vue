package com.example.backend.dto;

import com.example.backend.domain.Reservation;
import com.example.backend.domain.Payment;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class ReservationAdminDto {
    // 예약 정보
    private Long reservationId;
    private Reservation.ReservationStatus reservationStatus;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer guestCount;
    private BigDecimal totalAmount;
    private String specialRequests;
    private LocalDateTime reservationCreatedAt;
    
    // 결제 정보
    private Long paymentId;
    private Payment.PaymentStatus paymentStatus;
    private Payment.PaymentMethod paymentMethod;
    private BigDecimal paidAmount;
    private LocalDateTime paymentCreatedAt;
    
    // 사용자 정보
    private Long userId;
    private String userName;
    private String userEmail;
    
    // 호텔/객실 정보
    private Long hotelId;
    private String hotelName;
    private Long roomId;
    private String roomName;
    private String roomType;

    public static ReservationAdminDto from(Reservation reservation) {
        // 첫 번째 결제 정보 가져오기 (일반적으로 예약당 하나의 결제)
        Payment payment = null;
        if (reservation.getPayments() != null && !reservation.getPayments().isEmpty()) {
            payment = reservation.getPayments().get(0);
        }
        
        return ReservationAdminDto.builder()
                // 예약 정보
                .reservationId(reservation.getId())
                .reservationStatus(reservation.getStatus())
                .checkInDate(reservation.getCheckInDate())
                .checkOutDate(reservation.getCheckOutDate())
                .guestCount(reservation.getGuestCount())
                .totalAmount(reservation.getTotalAmount())
                .specialRequests(reservation.getSpecialRequests())
                .reservationCreatedAt(reservation.getCreatedAt())
                
                // 결제 정보
                .paymentId(payment != null ? payment.getId() : null)
                .paymentStatus(payment != null ? payment.getStatus() : null)
                .paymentMethod(payment != null ? payment.getMethod() : null)
                .paidAmount(payment != null ? payment.getAmount() : null)
                .paymentCreatedAt(payment != null ? payment.getCreatedAt() : null)
                
                // 사용자 정보
                .userId(reservation.getUser() != null ? reservation.getUser().getId() : null)
                .userName(reservation.getUser() != null ? reservation.getUser().getName() : null)
                .userEmail(reservation.getUser() != null ? reservation.getUser().getEmail() : null)
                
                // 호텔/객실 정보
                .hotelId(reservation.getRoom() != null && reservation.getRoom().getHotel() != null ? 
                        reservation.getRoom().getHotel().getId() : null)
                .hotelName(reservation.getRoom() != null && reservation.getRoom().getHotel() != null ? 
                          reservation.getRoom().getHotel().getName() : null)
                .roomId(reservation.getRoom() != null ? reservation.getRoom().getId() : null)
                .roomName(reservation.getRoom() != null ? reservation.getRoom().getRoomNumber() : null)
                .roomType(reservation.getRoom() != null ? reservation.getRoom().getRoomType() : null)
                
                .build();
    }
}