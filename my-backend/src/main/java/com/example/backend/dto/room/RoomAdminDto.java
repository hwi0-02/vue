package com.example.backend.dto.room;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomAdminDto {
    private Long id;
    private String roomNumber;
    private String roomType;
    private String description;
    private Integer capacity;
    private Double basePrice;
    private String status; // AVAILABLE, UNAVAILABLE, MAINTENANCE
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Hotel 정보
    private Long hotelId;
    private String hotelName;
    private String hotelStatus;
    
    // 통계 정보
    private Integer reservationCount;
    private Double occupancyRate;
    private Double totalRevenue;
    
    // 재고 정보
    private List<InventoryDto> inventory;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryDto {
        private Long id;
        private String date;
        private Integer availableCount;
        private Integer totalCount;
        private Double price;
    }
}