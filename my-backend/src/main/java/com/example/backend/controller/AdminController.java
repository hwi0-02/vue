package com.example.backend.controller;

import com.example.backend.domain.Business;
import com.example.backend.domain.Coupon;
import com.example.backend.domain.Reservation;
import com.example.backend.domain.Review;
import com.example.backend.domain.User;
import com.example.backend.dto.BusinessAdminDto;
import com.example.backend.dto.CouponAdminDto;
import com.example.backend.dto.CouponCreateRequest;
import com.example.backend.dto.CouponStatusUpdateRequest;
import com.example.backend.dto.CouponUpdateRequest;
import com.example.backend.dto.ReservationAdminDto;
import com.example.backend.dto.ReviewAdminDto;
import com.example.backend.dto.RoleUpdateRequest;
import com.example.backend.dto.SalesSummaryDto;
import com.example.backend.dto.StatusUpdateRequest;
import com.example.backend.dto.UserAdminDto;
import com.example.backend.repository.BusinessRepository;
import com.example.backend.repository.CouponRepository;
import com.example.backend.dto.PaymentAdminDto;
import com.example.backend.domain.Payment;
import com.example.backend.repository.PaymentRepository;
import com.example.backend.repository.ReservationRepository;
import com.example.backend.repository.ReviewRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {

    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final ReviewRepository reviewRepository;
    private final CouponRepository couponRepository;

    /**
     * 사용자 목록 조회 (검색 및 페이징)
     */
    @GetMapping("/users")
    public ResponseEntity<Page<UserAdminDto>> getUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) User.Role role,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("사용자 목록 조회 - name: {}, email: {}, role: {}, page: {}", 
                 name, email, role, pageable.getPageNumber());

        Page<User> users = userRepository.findUsersWithFilters(name, email, role, pageable);
        Page<UserAdminDto> userDtos = users.map(UserAdminDto::from);

        return ResponseEntity.ok(userDtos);
    }

    /**
     * 사용자 역할 변경
     */
    @PutMapping("/users/{userId}/role")
    public ResponseEntity<?> updateUserRole(
            @PathVariable Long userId,
            @RequestBody RoleUpdateRequest request,
            Authentication authentication) {
        
        log.info("사용자 역할 변경 요청 - userId: {}, newRole: {}", userId, request.getRole());

        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 현재 로그인한 관리자 정보 확인
        String currentUserEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("현재 사용자 정보를 찾을 수 없습니다."));

        // 자기 자신의 역할 변경 방지
        if (user.getId().equals(currentUser.getId())) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "자신의 역할은 변경할 수 없습니다.");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        // 역할 변경
        User.Role oldRole = user.getRole();
        user.setRole(request.getRole());
        userRepository.save(user);

        log.info("사용자 역할 변경 완료 - userId: {}, {} -> {}", 
                 userId, oldRole, request.getRole());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "역할이 성공적으로 변경되었습니다.");
        response.put("userId", userId);
        response.put("oldRole", oldRole);
        response.put("newRole", request.getRole());

        return ResponseEntity.ok(response);
    }

    /**
     * 사용자 상세 정보 조회
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserAdminDto> getUserDetail(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return ResponseEntity.ok(UserAdminDto.from(user));
    }

    // ============ 사업자 관리 API ============

    /**
     * 사업자 목록 조회 (상태별 필터링)
     */
    @GetMapping("/businesses")
    public ResponseEntity<Page<BusinessAdminDto>> getBusinesses(
            @RequestParam(required = false) Business.BusinessStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("사업자 목록 조회 - status: {}, page: {}", status, pageable.getPageNumber());

        Page<Business> businesses = businessRepository.findBusinessesWithFilters(status, pageable);
        Page<BusinessAdminDto> businessDtos = businesses.map(BusinessAdminDto::from);

        return ResponseEntity.ok(businessDtos);
    }

    /**
     * 사업자 상태 변경
     */
    @PutMapping("/businesses/{businessId}/status")
    public ResponseEntity<?> updateBusinessStatus(
            @PathVariable Long businessId,
            @RequestBody StatusUpdateRequest request) {
        
        log.info("사업자 상태 변경 요청 - businessId: {}, newStatus: {}", businessId, request.getStatus());

        // 사업자 존재 확인
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new RuntimeException("사업자를 찾을 수 없습니다."));

        // 상태 변경
        Business.BusinessStatus oldStatus = business.getStatus();
        business.setStatus(request.getStatus());
        businessRepository.save(business);

        log.info("사업자 상태 변경 완료 - businessId: {}, {} -> {}", 
                 businessId, oldStatus, request.getStatus());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "상태가 성공적으로 변경되었습니다.");
        response.put("businessId", businessId);
        response.put("oldStatus", oldStatus);
        response.put("newStatus", request.getStatus());

        return ResponseEntity.ok(response);
    }

    /**
     * 사업자 상세 정보 조회
     */
    @GetMapping("/businesses/{businessId}")
    public ResponseEntity<BusinessAdminDto> getBusinessDetail(@PathVariable Long businessId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new RuntimeException("사업자를 찾을 수 없습니다."));

        return ResponseEntity.ok(BusinessAdminDto.from(business));
    }

    /**
     * 예약 목록 조회 (검색 및 페이징)
     */
    @GetMapping("/reservations")
    public ResponseEntity<Page<ReservationAdminDto>> getReservations(
            @RequestParam(required = false) String hotelName,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) Reservation.ReservationStatus reservationStatus,
            @RequestParam(required = false) String paymentStatus,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("예약 목록 조회 - hotelName: {}, userName: {}, reservationStatus: {}, paymentStatus: {}, page: {}", 
                 hotelName, userName, reservationStatus, paymentStatus, pageable.getPageNumber());

        Page<Reservation> reservations = reservationRepository.findReservationsWithFilters(
                hotelName, userName, reservationStatus, paymentStatus, pageable);
        
        Page<ReservationAdminDto> reservationDtos = reservations.map(ReservationAdminDto::from);

        return ResponseEntity.ok(reservationDtos);
    }

    /**
     * 예약 상세 정보 조회
     */
    @GetMapping("/reservations/{reservationId}")
    public ResponseEntity<ReservationAdminDto> getReservationDetail(@PathVariable Long reservationId) {
        Reservation reservation = reservationRepository.findByIdWithDetails(reservationId)
                .orElseThrow(() -> new RuntimeException("예약을 찾을 수 없습니다."));

        return ResponseEntity.ok(ReservationAdminDto.from(reservation));
    }

    /**
     * 매출·수수료 관리 - 기간별 매출 요약 조회
     */
    @GetMapping("/sales")
    public ResponseEntity<SalesSummaryDto> getSalesSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        
        log.info("매출 요약 조회 - from: {}, to: {}", from, to);

        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.atTime(23, 59, 59);

        // 총 매출 조회
        BigDecimal totalRevenue = paymentRepository.getTotalRevenueByDateRange(fromDateTime, toDateTime);
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }

        // 플랫폼 수수료 (10%) 계산
        BigDecimal platformFeeAmount = totalRevenue.multiply(BigDecimal.valueOf(0.10))
                .setScale(2, RoundingMode.HALF_UP);

        // 호텔별 정산 데이터 조회
        List<Object[]> hotelData = paymentRepository.getHotelSettlementsByDateRange(fromDateTime, toDateTime);
        List<SalesSummaryDto.HotelSettlement> hotelSettlements = new ArrayList<>();

        for (Object[] data : hotelData) {
            Long hotelId = (Long) data[0];
            String hotelName = (String) data[1];
            BigDecimal hotelRevenue = (BigDecimal) data[2];
            Long reservationCount = (Long) data[3];

            // 호텔 정산 금액 (90%) 계산
            BigDecimal settlementAmount = hotelRevenue.multiply(BigDecimal.valueOf(0.90))
                    .setScale(2, RoundingMode.HALF_UP);

            hotelSettlements.add(SalesSummaryDto.HotelSettlement.builder()
                    .hotelId(hotelId)
                    .hotelName(hotelName)
                    .settlementAmount(settlementAmount)
                    .totalRevenue(hotelRevenue)
                    .reservationCount(reservationCount.intValue())
                    .build());
        }

        SalesSummaryDto summary = SalesSummaryDto.builder()
                .totalRevenue(totalRevenue)
                .platformFeeAmount(platformFeeAmount)
                .hotelSettlements(hotelSettlements)
                .build();

        return ResponseEntity.ok(summary);
    }

    // ============ 결제 관리 API ============

    /**
     * 결제 목록 조회 (검색 및 페이징)
     */
    @GetMapping("/payments")
    public ResponseEntity<Page<PaymentAdminDto>> getPayments(
            @RequestParam(required = false) String hotelName,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) Payment.PaymentStatus status,
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("결제 목록 조회 - hotelName: {}, userName: {}, status: {}, page: {}",
                hotelName, userName, status, pageable.getPageNumber());

        Page<Payment> payments = paymentRepository.findPaymentsForAdmin(hotelName, userName, status, pageable);
        Page<PaymentAdminDto> dtos = payments.map(PaymentAdminDto::from);
        return ResponseEntity.ok(dtos);
    }

    /**
     * 결제 환불 처리 (단순 상태 변경)
     */
    @PostMapping("/payments/{paymentId}/refund")
    public ResponseEntity<PaymentAdminDto> refundPayment(@PathVariable Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("결제를 찾을 수 없습니다."));

        if (payment.getStatus() != Payment.PaymentStatus.PAID) {
            return ResponseEntity.badRequest().build();
        }

        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        payment.setCancelledAt(LocalDateTime.now());
        paymentRepository.save(payment);

        return ResponseEntity.ok(PaymentAdminDto.from(payment));
    }

    /**
     * 리뷰 목록 조회 (검색 및 페이징)
     */
    @GetMapping("/reviews")
    public ResponseEntity<Page<ReviewAdminDto>> getReviews(
            @RequestParam(required = false) Boolean reported,
            @RequestParam(required = false) Boolean hidden,
            @RequestParam(required = false) String hotelName,
            @RequestParam(required = false) String userName,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("리뷰 목록 조회 - reported: {}, hidden: {}, hotelName: {}, userName: {}, page: {}", 
                 reported, hidden, hotelName, userName, pageable.getPageNumber());

        Page<Review> reviews = reviewRepository.findReviewsWithFilters(
                reported, hidden, hotelName, userName, pageable);
        
        Page<ReviewAdminDto> reviewDtos = reviews.map(ReviewAdminDto::from);

        return ResponseEntity.ok(reviewDtos);
    }

    /**
     * 리뷰 상세 정보 조회
     */
    @GetMapping("/reviews/{reviewId}")
    public ResponseEntity<ReviewAdminDto> getReviewDetail(@PathVariable Long reviewId) {
        Review review = reviewRepository.findByIdWithDetails(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));

        return ResponseEntity.ok(ReviewAdminDto.from(review));
    }

    /**
     * 리뷰 숨김 처리
     */
    @PutMapping("/reviews/{reviewId}/hide")
    public ResponseEntity<Map<String, Object>> hideReview(@PathVariable Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));

        Boolean oldHidden = review.getIsHidden();
        review.setIsHidden(true);
        reviewRepository.save(review);

        log.info("리뷰 숨김 처리 완료 - reviewId: {}, {} -> {}", 
                 reviewId, oldHidden, true);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "리뷰가 성공적으로 숨김 처리되었습니다.");
        response.put("reviewId", reviewId);
        response.put("oldHidden", oldHidden);
        response.put("newHidden", true);

        return ResponseEntity.ok(response);
    }

    /**
     * 리뷰 숨김 해제
     */
    @PutMapping("/reviews/{reviewId}/show")
    public ResponseEntity<Map<String, Object>> showReview(@PathVariable Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));

        Boolean oldHidden = review.getIsHidden();
        review.setIsHidden(false);
        reviewRepository.save(review);

        log.info("리뷰 숨김 해제 완료 - reviewId: {}, {} -> {}", 
                 reviewId, oldHidden, false);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "리뷰 숨김이 성공적으로 해제되었습니다.");
        response.put("reviewId", reviewId);
        response.put("oldHidden", oldHidden);
        response.put("newHidden", false);

        return ResponseEntity.ok(response);
    }

    /**
     * 리뷰 신고 처리
     */
    @PutMapping("/reviews/{reviewId}/report")
    public ResponseEntity<Map<String, Object>> reportReview(@PathVariable Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));

        Boolean oldReported = review.getIsReported();
        review.setIsReported(true);
        reviewRepository.save(review);

        log.info("리뷰 신고 처리 완료 - reviewId: {}, {} -> {}", 
                 reviewId, oldReported, true);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "리뷰가 성공적으로 신고 처리되었습니다.");
        response.put("reviewId", reviewId);
        response.put("oldReported", oldReported);
        response.put("newReported", true);

        return ResponseEntity.ok(response);
    }

    // ===== 쿠폰 관리 API =====

    /**
     * 쿠폰 목록 조회 (관리자)
     */
    @GetMapping("/coupons")
    public ResponseEntity<Page<CouponAdminDto>> getCoupons(
            @RequestParam(required = false) Coupon.CouponStatus status,
            @RequestParam(required = false) Coupon.DiscountType discountType,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {

        log.info("관리자 쿠폰 목록 조회 요청 - 사용자: {}, 상태: {}, 할인타입: {}, 코드: {}, 이름: {}", 
                 authentication.getName(), status, discountType, code, name);

        Page<Coupon> coupons = couponRepository.findCouponsWithFilters(
                status, discountType, code, name, pageable);

        Page<CouponAdminDto> couponDtos = coupons.map(CouponAdminDto::from);

        log.info("쿠폰 목록 조회 완료 - 총 개수: {}, 페이지: {}/{}", 
                 coupons.getTotalElements(), 
                 coupons.getNumber() + 1, 
                 coupons.getTotalPages());

        return ResponseEntity.ok(couponDtos);
    }

    /**
     * 쿠폰 생성 (관리자)
     */
    @PostMapping("/coupons")
    public ResponseEntity<CouponAdminDto> createCoupon(
            @RequestBody CouponCreateRequest request,
            Authentication authentication) {

        log.info("관리자 쿠폰 생성 요청 - 사용자: {}, 코드: {}, 이름: {}", 
                 authentication.getName(), request.getCode(), request.getName());

        // 쿠폰 코드 중복 검사
        if (couponRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("이미 사용 중인 쿠폰 코드입니다: " + request.getCode());
        }

        Coupon coupon = request.toEntity();
        coupon = couponRepository.save(coupon);

        log.info("쿠폰 생성 완료 - ID: {}, 코드: {}", coupon.getId(), coupon.getCode());

        return ResponseEntity.ok(CouponAdminDto.from(coupon));
    }

    /**
     * 쿠폰 수정 (관리자)
     */
    @PutMapping("/coupons/{couponId}")
    public ResponseEntity<CouponAdminDto> updateCoupon(
            @PathVariable Long couponId,
            @RequestBody CouponUpdateRequest request,
            Authentication authentication) {

        log.info("관리자 쿠폰 수정 요청 - 사용자: {}, 쿠폰ID: {}", 
                 authentication.getName(), couponId);

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다: " + couponId));

        // 코드 변경 시 중복 검사
        if (request.getCode() != null && !request.getCode().equals(coupon.getCode())) {
            if (couponRepository.existsByCode(request.getCode())) {
                throw new IllegalArgumentException("이미 사용 중인 쿠폰 코드입니다: " + request.getCode());
            }
        }

        request.updateEntity(coupon);
        coupon = couponRepository.save(coupon);

        log.info("쿠폰 수정 완료 - ID: {}, 코드: {}", coupon.getId(), coupon.getCode());

        return ResponseEntity.ok(CouponAdminDto.from(coupon));
    }

    /**
     * 쿠폰 삭제 (관리자)
     */
    @DeleteMapping("/coupons/{couponId}")
    public ResponseEntity<Map<String, Object>> deleteCoupon(
            @PathVariable Long couponId,
            Authentication authentication) {

        log.info("관리자 쿠폰 삭제 요청 - 사용자: {}, 쿠폰ID: {}", 
                 authentication.getName(), couponId);

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다: " + couponId));

        // 사용된 쿠폰인 경우 삭제 불가
        if (coupon.getUsedCount() > 0) {
            throw new IllegalArgumentException("이미 사용된 쿠폰은 삭제할 수 없습니다.");
        }

        String deletedCode = coupon.getCode();
        couponRepository.delete(coupon);

        log.info("쿠폰 삭제 완료 - ID: {}, 코드: {}", couponId, deletedCode);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "쿠폰이 성공적으로 삭제되었습니다.");
        response.put("couponId", couponId);
        response.put("code", deletedCode);

        return ResponseEntity.ok(response);
    }

    /**
     * 쿠폰 상태 변경 (관리자)
     */
    @PutMapping("/coupons/{couponId}/status")
    public ResponseEntity<Map<String, Object>> updateCouponStatus(
            @PathVariable Long couponId,
            @RequestBody CouponStatusUpdateRequest request,
            Authentication authentication) {

        log.info("관리자 쿠폰 상태 변경 요청 - 사용자: {}, 쿠폰ID: {}, 상태: {}", 
                 authentication.getName(), couponId, request.getStatus());

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다: " + couponId));

        Coupon.CouponStatus oldStatus = coupon.getStatus();
        Coupon.CouponStatus newStatus = Coupon.CouponStatus.valueOf(request.getStatus());
        
        coupon.setStatus(newStatus);
        couponRepository.save(coupon);

        log.info("쿠폰 상태 변경 완료 - 쿠폰ID: {}, {} -> {}", 
                 couponId, oldStatus, newStatus);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "쿠폰 상태가 성공적으로 변경되었습니다.");
        response.put("couponId", couponId);
        response.put("oldStatus", oldStatus);
        response.put("newStatus", newStatus);

        return ResponseEntity.ok(response);
    }

    /**
     * 쿠폰 통계 조회 (관리자)
     */
    @GetMapping("/coupons/stats")
    public ResponseEntity<Map<String, Object>> getCouponStats(
            Authentication authentication) {

        log.info("관리자 쿠폰 통계 조회 요청 - 사용자: {}", authentication.getName());

        Map<String, Object> stats = new HashMap<>();
        
        // 상태별 쿠폰 개수
        stats.put("totalCoupons", couponRepository.count());
        stats.put("activeCoupons", couponRepository.countByStatus(Coupon.CouponStatus.ACTIVE));
        stats.put("inactiveCoupons", couponRepository.countByStatus(Coupon.CouponStatus.INACTIVE));
        stats.put("expiredCoupons", couponRepository.countExpiredCoupons(LocalDateTime.now()));
        stats.put("usedUpCoupons", couponRepository.countUsedUpCoupons());

        log.info("쿠폰 통계 조회 완료 - 총 쿠폰: {}, 활성: {}, 비활성: {}", 
                 stats.get("totalCoupons"), stats.get("activeCoupons"), stats.get("inactiveCoupons"));

        return ResponseEntity.ok(stats);
    }
}