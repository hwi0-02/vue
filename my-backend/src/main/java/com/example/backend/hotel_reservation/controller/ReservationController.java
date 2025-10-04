// src/main/java/com/example/backend/hotel_reservation/controller/ReservationController.java
package com.example.backend.hotel_reservation.controller;

import com.example.backend.hotel_reservation.dto.ReservationDtos;
import com.example.backend.hotel_reservation.dto.ReservationDtos.*;
import com.example.backend.hotel_reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.example.backend.authlogin.config.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService service;
    private final JwtUtil jwtUtil;

    // ✅ 토큰에서 userId 추출해서 강제로 주입
    @PostMapping("/hold")
    public HoldResponse hold(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody HoldRequest req
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing Bearer token");
        }
        final String token = authHeader.substring(7);
        Long userId = jwtUtil.extractClaim(token, claims -> {
            Object v = claims.get("userId");
            if (v == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No userId in token");
            if (v instanceof Number n) return n.longValue();
            try { return Long.parseLong(v.toString()); }
            catch (Exception e) { throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid userId in token"); }
        });

        // 프런트가 뭘 보내든 덮어쓰기
        req.setUserId(userId);
        return service.hold(req);
    }

    @PostMapping("/{id}/confirm")
    public void confirm(@PathVariable Long id) {
        service.confirm(id);
    }

    @PostMapping("/{id}/cancel")
    public void cancel(@PathVariable Long id) {
        service.cancel(id);
    }

        @PostMapping("/{id}/expire")
    public void expire(@PathVariable Long id) {
        service.expire(id);
    }

    @GetMapping("/{id}")
    public ReservationDetail get(@PathVariable Long id) {
        return service.get(id);
    }

    // (관리자/마이그레이션용) 특정 userId로 조회
    @GetMapping("/user/{userId}")
    public java.util.List<ReservationDtos.ReservationSummary> getByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return service.getByUserId(userId, page, size);
    }

    // ✅ 내 예약: JWT의 userId 사용
    @GetMapping("/my")
    public java.util.List<ReservationDtos.ReservationSummary> getMy(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing Bearer token");
        }
        final String token = authHeader.substring(7);
        Long userId = jwtUtil.extractClaim(token, claims -> {
            Object v = claims.get("userId");
            if (v == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No userId in token");
            if (v instanceof Number n) return n.longValue();
            try { return Long.parseLong(v.toString()); }
            catch (Exception e) { throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid userId in token"); }
        });

        return service.getByUserId(userId, page, size);
    }
}
