package com.example.backend.repository;

import com.example.backend.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    @Query("SELECT r FROM Review r " +
           "LEFT JOIN FETCH r.user u " +
           "LEFT JOIN FETCH r.hotel h " +
           "LEFT JOIN FETCH r.reservation res " +
           "WHERE (:reported IS NULL OR r.isReported = :reported) " +
           "AND (:hidden IS NULL OR r.isHidden = :hidden) " +
           "AND (:hotelName IS NULL OR LOWER(h.name) LIKE LOWER(CONCAT('%', :hotelName, '%'))) " +
           "AND (:userName IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :userName, '%')))")
    Page<Review> findReviewsWithFilters(
            @Param("reported") Boolean reported,
            @Param("hidden") Boolean hidden,
            @Param("hotelName") String hotelName,
            @Param("userName") String userName,
            Pageable pageable);
    
    @Query("SELECT r FROM Review r " +
           "LEFT JOIN FETCH r.user u " +
           "LEFT JOIN FETCH r.hotel h " +
           "LEFT JOIN FETCH r.reservation res " +
           "WHERE r.id = :reviewId")
    Optional<Review> findByIdWithDetails(@Param("reviewId") Long reviewId);
    
    long countByIsReported(Boolean isReported);
    
    long countByIsHidden(Boolean isHidden);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.isHidden = false")
    Double getAverageRating();
}