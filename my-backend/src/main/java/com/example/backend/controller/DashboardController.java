package com.example.backend.controller;

import com.example.backend.dto.DashboardSummaryDto;
import com.example.backend.repository.BusinessRepository;
import com.example.backend.repository.CouponRepository;
import com.example.backend.repository.HotelRepository;
import com.example.backend.repository.PaymentRepository;
import com.example.backend.repository.ReservationRepository;
import com.example.backend.repository.ReviewRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final ReviewRepository reviewRepository;
    private final CouponRepository couponRepository;
    private final HotelRepository hotelRepository;

    /**
     * 대시보드 요약 데이터 조회
     */
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDto> getDashboardSummary(Authentication authentication) {
        
        log.info("대시보드 요약 데이터 조회 요청 - 사용자: {}", authentication.getName());

        // 핵심 지표 요약
        Long totalUsers = userRepository.count();
        Long totalBusinesses = businessRepository.count();
        Long totalReservations = reservationRepository.count();
        BigDecimal totalRevenue = paymentRepository.getTotalRevenue();
        Long totalReviews = reviewRepository.count();
        Long totalCoupons = couponRepository.count();

        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }

        // 최근 30일간 일별 매출액
        List<DashboardSummaryDto.DailyRevenue> dailyRevenues = getDailyRevenues();

        // 최근 12개월간 월별 신규 가입자 수
        List<DashboardSummaryDto.MonthlySignups> monthlySignups = getMonthlySignups();

        // 최근 30일간 예약 수가 가장 많은 상위 5개 호텔
        List<DashboardSummaryDto.TopHotel> topHotels = getTopHotels();

        DashboardSummaryDto summary = DashboardSummaryDto.builder()
                .totalUsers(totalUsers)
                .totalBusinesses(totalBusinesses)
                .totalReservations(totalReservations)
                .totalRevenue(totalRevenue)
                .totalReviews(totalReviews)
                .totalCoupons(totalCoupons)
                .dailyRevenues(dailyRevenues)
                .monthlySignups(monthlySignups)
                .topHotels(topHotels)
                .build();

        log.info("대시보드 요약 데이터 조회 완료 - 사용자: {}, 총매출: {}", 
                 authentication.getName(), totalRevenue);

        return ResponseEntity.ok(summary);
    }

    /**
     * 최근 30일간 일별 매출액 조회
     */
    private List<DashboardSummaryDto.DailyRevenue> getDailyRevenues() {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(30);

        List<Object[]> results = paymentRepository.getDailyRevenueByDateRange(startDate, endDate);
        List<DashboardSummaryDto.DailyRevenue> dailyRevenues = new ArrayList<>();

        // 결과를 Map으로 변환하여 빠른 조회
        var revenueMap = new java.util.HashMap<LocalDate, BigDecimal>();
        for (Object[] result : results) {
            LocalDate date = ((java.sql.Date) result[0]).toLocalDate();
            BigDecimal revenue = (BigDecimal) result[1];
            revenueMap.put(date, revenue);
        }

        // 30일간 모든 날짜에 대해 데이터 생성 (빈 날짜는 0으로)
        for (int i = 29; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            BigDecimal revenue = revenueMap.getOrDefault(date, BigDecimal.ZERO);
            
            dailyRevenues.add(DashboardSummaryDto.DailyRevenue.builder()
                    .date(date)
                    .revenue(revenue)
                    .build());
        }

        return dailyRevenues;
    }

    /**
     * 최근 12개월간 월별 신규 가입자 수 조회
     */
    private List<DashboardSummaryDto.MonthlySignups> getMonthlySignups() {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusMonths(12);

        List<Object[]> userResults = userRepository.getMonthlySignupsByDateRange(startDate, endDate);
        List<Object[]> businessResults = businessRepository.getMonthlySignupsByDateRange(startDate, endDate);

        // 결과를 Map으로 변환
        var userMap = new java.util.HashMap<String, Long>();
        for (Object[] result : userResults) {
            String month = (String) result[0];
            Long count = (Long) result[1];
            userMap.put(month, count);
        }

        var businessMap = new java.util.HashMap<String, Long>();
        for (Object[] result : businessResults) {
            String month = (String) result[0];
            Long count = (Long) result[1];
            businessMap.put(month, count);
        }

        List<DashboardSummaryDto.MonthlySignups> monthlySignups = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        // 12개월간 모든 월에 대해 데이터 생성
        for (int i = 11; i >= 0; i--) {
            String month = LocalDate.now().minusMonths(i).format(formatter);
            Long userCount = userMap.getOrDefault(month, 0L);
            Long businessCount = businessMap.getOrDefault(month, 0L);

            monthlySignups.add(DashboardSummaryDto.MonthlySignups.builder()
                    .month(month)
                    .userCount(userCount)
                    .businessCount(businessCount)
                    .build());
        }

        return monthlySignups;
    }

    /**
     * 최근 30일간 예약 수가 가장 많은 상위 5개 호텔 조회
     */
    private List<DashboardSummaryDto.TopHotel> getTopHotels() {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(30);

        List<Object[]> results = hotelRepository.getTopHotelsByReservations(startDate, endDate, 5);
        List<DashboardSummaryDto.TopHotel> topHotels = new ArrayList<>();

        for (Object[] result : results) {
            Long hotelId = (Long) result[0];
            String hotelName = (String) result[1];
            String businessName = (String) result[2];
            Long reservationCount = (Long) result[3];
            BigDecimal revenue = (BigDecimal) result[4];
            Double averageRating = (Double) result[5];

            if (revenue == null) revenue = BigDecimal.ZERO;
            if (averageRating == null) averageRating = 0.0;

            topHotels.add(DashboardSummaryDto.TopHotel.builder()
                    .hotelId(hotelId)
                    .hotelName(hotelName)
                    .businessName(businessName)
                    .reservationCount(reservationCount)
                    .revenue(revenue)
                    .averageRating(averageRating)
                    .build());
        }

        return topHotels;
    }
}