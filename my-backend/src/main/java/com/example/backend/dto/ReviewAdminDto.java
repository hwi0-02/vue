package com.example.backend.dto;

import com.example.backend.domain.Review;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReviewAdminDto {
    // 리뷰 정보
    private Long reviewId;
    private Integer rating;
    private String title;
    private String content;
    private String imageUrls;
    private Boolean isHidden;
    private Boolean isReported;
    private String adminReply;
    private LocalDateTime repliedAt;
    private LocalDateTime createdAt;
    
    // 사용자 정보
    private Long userId;
    private String userName;
    private String userEmail;
    
    // 호텔 정보
    private Long hotelId;
    private String hotelName;
    
    // 예약 정보
    private Long reservationId;
    private String reservationNumber;

    public static ReviewAdminDto from(Review review) {
        return ReviewAdminDto.builder()
                // 리뷰 정보
                .reviewId(review.getId())
                .rating(review.getRating())
                .title(review.getTitle())
                .content(review.getContent())
                .imageUrls(review.getImageUrls())
                .isHidden(review.getIsHidden())
                .isReported(review.getIsReported())
                .adminReply(review.getAdminReply())
                .repliedAt(review.getRepliedAt())
                .createdAt(review.getCreatedAt())
                
                // 사용자 정보
                .userId(review.getUser() != null ? review.getUser().getId() : null)
                .userName(review.getUser() != null ? review.getUser().getName() : null)
                .userEmail(review.getUser() != null ? review.getUser().getEmail() : null)
                
                // 호텔 정보
                .hotelId(review.getHotel() != null ? review.getHotel().getId() : null)
                .hotelName(review.getHotel() != null ? review.getHotel().getName() : null)
                
                // 예약 정보
                .reservationId(review.getReservation() != null ? review.getReservation().getId() : null)
                .reservationNumber(review.getReservation() != null ? review.getReservation().getReservationNumber() : null)
                
                .build();
    }
}