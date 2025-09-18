package com.example.backend.service;

import com.example.backend.domain.Room;
import com.example.backend.domain.RoomInventory;
import com.example.backend.dto.room.RoomAdminDto;
import com.example.backend.repository.PaymentRepository;
import com.example.backend.repository.ReservationRepository;
import com.example.backend.repository.RoomInventoryRepository;
import com.example.backend.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomInventoryRepository roomInventoryRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public Page<RoomAdminDto> getRoomsForAdmin(Long hotelId, String roomType, String status, Pageable pageable) {
        Page<Room> rooms = roomRepository.findRoomsForAdmin(hotelId, roomType, status, pageable);
        
        return rooms.map(room -> {
            Integer reservationCount = reservationRepository.countByRoomId(room.getId());
            Double occupancyRate = roomRepository.getOccupancyRate(room.getId());
            Double totalRevenue = paymentRepository.getTotalRevenueByRoom(room.getId());
            
            return RoomAdminDto.builder()
                    .id(room.getId())
                    .roomNumber(room.getRoomNumber())
                    .roomType(room.getRoomType())
                    .description(room.getDescription())
                    .capacity(room.getMaxOccupancy())
                    .basePrice(getBasePrice(room)) // 별도 메서드로 가격 조회
                    .status(room.getStatus().name())
                    .createdAt(room.getCreatedAt())
                    .updatedAt(room.getUpdatedAt())
                    .hotelId(room.getHotel().getId())
                    .hotelName(room.getHotel().getName())
                    .hotelStatus(room.getHotel().getStatus().name())
                    .reservationCount(reservationCount)
                    .occupancyRate(occupancyRate != null ? occupancyRate : 0.0)
                    .totalRevenue(totalRevenue != null ? totalRevenue : 0.0)
                    .build();
        });
    }

    @Transactional(readOnly = true)
    public RoomAdminDto getRoomDetailForAdmin(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        Integer reservationCount = reservationRepository.countByRoomId(room.getId());
        Double occupancyRate = roomRepository.getOccupancyRate(room.getId());
        Double totalRevenue = paymentRepository.getTotalRevenueByRoom(room.getId());

        List<RoomAdminDto.InventoryDto> inventory = roomInventoryRepository.findByRoomId(roomId)
                .stream()
                .map(inv -> RoomAdminDto.InventoryDto.builder()
                        .id(inv.getId())
                        .date(inv.getInventoryDate().toString())
                        .availableCount(inv.getAvailableQuantity())
                        .totalCount(inv.getTotalQuantity())
                        .price(getInventoryPrice(inv)) // 별도 메서드로 가격 조회
                        .build())
                .collect(Collectors.toList());

        return RoomAdminDto.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .roomType(room.getRoomType())
                .description(room.getDescription())
                .capacity(room.getMaxOccupancy())
                .basePrice(getBasePrice(room)) // 별도 메서드로 가격 조회
                .status(room.getStatus().name())
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .hotelId(room.getHotel().getId())
                .hotelName(room.getHotel().getName())
                .hotelStatus(room.getHotel().getStatus().name())
                .reservationCount(reservationCount)
                .occupancyRate(occupancyRate != null ? occupancyRate : 0.0)
                .totalRevenue(totalRevenue != null ? totalRevenue : 0.0)
                .inventory(inventory)
                .build();
    }

    public void updateRoomStatus(Long roomId, String status, String reason) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        room.setStatus(Room.RoomStatus.valueOf(status));
        roomRepository.save(room);
    }

    public void deleteRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        roomRepository.delete(room);
    }

    @Transactional(readOnly = true)
    public Object getRoomInventory(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        Map<String, Object> inventory = new HashMap<>();
        inventory.put("roomId", roomId);
        inventory.put("roomNumber", room.getRoomNumber());
        inventory.put("roomType", room.getRoomType());
        inventory.put("inventoryData", roomInventoryRepository.findByRoomIdOrderByInventoryDate(roomId));

        return inventory;
    }

    private Double getBasePrice(Room room) {
        // RoomPricePolicy에서 기본 가격 조회
        return room.getRoomPricePolicies().stream()
                .filter(policy -> "DEFAULT".equals(policy.getPolicyName()) || policy.getIsActive())
                .findFirst()
                .map(policy -> policy.getBasePrice().doubleValue())
                .orElse(0.0);
    }

    private Double getInventoryPrice(RoomInventory inventory) {
        // RoomInventory에는 가격 정보가 없으므로 Room의 기본 가격 사용
        return getBasePrice(inventory.getRoom());
    }
}