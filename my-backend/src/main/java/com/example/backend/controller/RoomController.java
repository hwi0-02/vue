package com.example.backend.controller;

import com.example.backend.dto.room.RoomAdminDto;
import com.example.backend.dto.room.RoomStatusUpdateRequest;
import com.example.backend.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    public ResponseEntity<Page<RoomAdminDto>> getRooms(
            @RequestParam(required = false) Long hotelId,
            @RequestParam(required = false) String roomType,
            @RequestParam(required = false) String status,
            Pageable pageable
    ) {
        Page<RoomAdminDto> rooms = roomService.getRoomsForAdmin(hotelId, roomType, status, pageable);
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomAdminDto> getRoomDetail(@PathVariable Long roomId) {
        RoomAdminDto room = roomService.getRoomDetailForAdmin(roomId);
        return ResponseEntity.ok(room);
    }

    @PutMapping("/{roomId}/status")
    public ResponseEntity<Void> updateRoomStatus(
            @PathVariable Long roomId,
            @RequestBody RoomStatusUpdateRequest request
    ) {
        roomService.updateRoomStatus(roomId, request.getStatus(), request.getReason());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long roomId) {
        roomService.deleteRoom(roomId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{roomId}/inventory")
    public ResponseEntity<Object> getRoomInventory(@PathVariable Long roomId) {
        Object inventory = roomService.getRoomInventory(roomId);
        return ResponseEntity.ok(inventory);
    }
}