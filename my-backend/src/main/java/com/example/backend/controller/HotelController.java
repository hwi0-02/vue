package com.example.backend.controller;

import com.example.backend.dto.hotel.HotelAdminDto;
import com.example.backend.dto.hotel.HotelStatusUpdateRequest;
import com.example.backend.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    @GetMapping
    public ResponseEntity<Page<HotelAdminDto>> getHotels(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String city,
            Pageable pageable
    ) {
        Page<HotelAdminDto> hotels = hotelService.getHotelsForAdmin(name, status, city, pageable);
        return ResponseEntity.ok(hotels);
    }

    @GetMapping("/{hotelId}")
    public ResponseEntity<HotelAdminDto> getHotelDetail(@PathVariable Long hotelId) {
        HotelAdminDto hotel = hotelService.getHotelDetailForAdmin(hotelId);
        return ResponseEntity.ok(hotel);
    }

    @PutMapping("/{hotelId}/status")
    public ResponseEntity<Void> updateHotelStatus(
            @PathVariable Long hotelId,
            @RequestBody HotelStatusUpdateRequest request
    ) {
        hotelService.updateHotelStatus(hotelId, request.getStatus(), request.getReason());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{hotelId}")
    public ResponseEntity<Void> deleteHotel(@PathVariable Long hotelId) {
        hotelService.deleteHotel(hotelId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{hotelId}/stats")
    public ResponseEntity<Object> getHotelStats(@PathVariable Long hotelId) {
        Object stats = hotelService.getHotelStats(hotelId);
        return ResponseEntity.ok(stats);
    }
}