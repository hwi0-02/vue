package com.example.backend.dto.room;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomStatusUpdateRequest {
    private String status; // AVAILABLE, UNAVAILABLE, MAINTENANCE
    private String reason; // 상태 변경 사유
}