package com.example.backend.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class SalesSummaryDto {
    private BigDecimal totalRevenue;
    private BigDecimal platformFeeAmount; // 플랫폼 수수료 (10%)
    private List<HotelSettlement> hotelSettlements;

    @Getter
    @Builder
    public static class HotelSettlement {
        private Long hotelId;
        private String hotelName;
        private BigDecimal settlementAmount; // 호텔 정산 금액 (90%)
        private BigDecimal totalRevenue; // 해당 호텔의 총 매출
        private Integer reservationCount; // 예약 건수
    }
}