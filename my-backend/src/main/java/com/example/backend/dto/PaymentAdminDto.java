package com.example.backend.dto;

import com.example.backend.domain.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentAdminDto {
    private Long id;
    private String reservationNumber;
    private Long reservationId;
    private Long hotelId;
    private String hotelName;
    private Long userId;
    private String userName;
    private String orderId;
    private String paymentKey;
    private BigDecimal amount;
    private String method;
    private String status;
    private LocalDateTime paidAt;
    private LocalDateTime cancelledAt;

    public static PaymentAdminDto from(Payment p) {
        var r = p.getReservation();
        var h = r.getHotel();
        var u = r.getUser();
        return PaymentAdminDto.builder()
                .id(p.getId())
                .reservationId(r.getId())
                .reservationNumber(r.getReservationNumber())
                .hotelId(h.getId())
                .hotelName(h.getName())
                .userId(u.getId())
                .userName(u.getName())
                .orderId(p.getOrderId())
                .paymentKey(p.getPaymentKey())
                .amount(p.getAmount())
                .method(p.getMethod().name())
                .status(p.getStatus().name())
                .paidAt(p.getPaidAt())
                .cancelledAt(p.getCancelledAt())
                .build();
    }
}
