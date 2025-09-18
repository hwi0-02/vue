package com.example.backend.service;

import com.example.backend.domain.Hotel;
import com.example.backend.domain.Room;
import com.example.backend.dto.hotel.HotelAdminDto;
import com.example.backend.repository.HotelRepository;
import com.example.backend.repository.PaymentRepository;
import com.example.backend.repository.ReservationRepository;
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
public class HotelService {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public Page<HotelAdminDto> getHotelsForAdmin(String name, String status, String city, Pageable pageable) {
        Page<Hotel> hotels = hotelRepository.findHotelsForAdmin(name, status, city, pageable);
        
        return hotels.map(hotel -> {
            Integer roomCount = roomRepository.countByHotelId(hotel.getId());
            Integer reservationCount = reservationRepository.countByHotelId(hotel.getId());
            Double averageRating = hotelRepository.getAverageRating(hotel.getId());
            Double totalRevenue = paymentRepository.getTotalRevenueByHotel(hotel.getId());
            
            return HotelAdminDto.builder()
                    .id(hotel.getId())
                    .name(hotel.getName())
                    .description(hotel.getDescription())
                    .address(hotel.getAddress())
                    .city(hotel.getAddress()) // address에서 도시 추출하거나 별도 로직 필요
                    .phone(hotel.getPhone())
                    .email(hotel.getBusiness().getUser().getEmail()) // business의 user 이메일 사용
                    .status(hotel.getStatus().name())
                    .rejectionReason(null) // Hotel 엔티티에 없음
                    .createdAt(hotel.getCreatedAt())
                    .updatedAt(hotel.getUpdatedAt())
                    .businessId(hotel.getBusiness().getId())
                    .businessName(hotel.getBusiness().getBusinessName())
                    .businessEmail(hotel.getBusiness().getUser().getEmail())
                    .roomCount(roomCount)
                    .reservationCount(reservationCount)
                    .averageRating(averageRating != null ? averageRating : 0.0)
                    .totalRevenue(totalRevenue != null ? totalRevenue : 0.0)
                    .build();
        });
    }

    @Transactional(readOnly = true)
    public HotelAdminDto getHotelDetailForAdmin(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel not found"));

        Integer roomCount = roomRepository.countByHotelId(hotel.getId());
        Integer reservationCount = reservationRepository.countByHotelId(hotel.getId());
        Double averageRating = hotelRepository.getAverageRating(hotel.getId());
        Double totalRevenue = paymentRepository.getTotalRevenueByHotel(hotel.getId());

        List<HotelAdminDto.RoomSummaryDto> rooms = roomRepository.findByHotelId(hotelId)
                .stream()
                .map(room -> HotelAdminDto.RoomSummaryDto.builder()
                        .id(room.getId())
                        .roomType(room.getRoomType())
                        .status(room.getStatus().name())
                        .capacity(room.getMaxOccupancy())
                        .basePrice(getBasePrice(room)) // 별도 메서드로 가격 조회
                        .build())
                .collect(Collectors.toList());

        return HotelAdminDto.builder()
                .id(hotel.getId())
                .name(hotel.getName())
                .description(hotel.getDescription())
                .address(hotel.getAddress())
                .city(hotel.getAddress()) // address에서 도시 추출하거나 별도 로직 필요
                .phone(hotel.getPhone())
                .email(hotel.getBusiness().getUser().getEmail()) // business의 user 이메일 사용
                .status(hotel.getStatus().name())
                .rejectionReason(null) // Hotel 엔티티에 없음
                .createdAt(hotel.getCreatedAt())
                .updatedAt(hotel.getUpdatedAt())
                .businessId(hotel.getBusiness().getId())
                .businessName(hotel.getBusiness().getBusinessName())
                .businessEmail(hotel.getBusiness().getUser().getEmail())
                .roomCount(roomCount)
                .reservationCount(reservationCount)
                .averageRating(averageRating != null ? averageRating : 0.0)
                .totalRevenue(totalRevenue != null ? totalRevenue : 0.0)
                .rooms(rooms)
                .build();
    }

    public void updateHotelStatus(Long hotelId, String status, String reason) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel not found"));

        hotel.setStatus(Hotel.HotelStatus.valueOf(status));
        // Note: Hotel 엔티티에 rejectionReason 필드가 없으므로 별도 처리 필요

        hotelRepository.save(hotel);
    }

    public void deleteHotel(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel not found"));

        hotelRepository.delete(hotel);
    }

    @Transactional(readOnly = true)
    public Object getHotelStats(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel not found"));

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRooms", roomRepository.countByHotelId(hotelId));
        stats.put("availableRooms", roomRepository.countByHotelIdAndStatus(hotelId, "AVAILABLE"));
        stats.put("totalReservations", reservationRepository.countByHotelId(hotelId));
        stats.put("totalRevenue", paymentRepository.getTotalRevenueByHotel(hotelId));
        stats.put("averageRating", hotelRepository.getAverageRating(hotelId));
        stats.put("monthlyReservations", reservationRepository.getMonthlyReservationsByHotel(hotelId));

        return stats;
    }

    private Double getBasePrice(Room room) {
        // RoomPricePolicy에서 기본 가격 조회
        return room.getRoomPricePolicies().stream()
                .filter(policy -> "DEFAULT".equals(policy.getPolicyName()) || policy.getIsActive())
                .findFirst()
                .map(policy -> policy.getBasePrice().doubleValue())
                .orElse(0.0);
    }
}