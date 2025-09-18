package com.example.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "hotels")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(length = 20)
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "star_rating")
    private Integer starRating;

    @Column(name = "check_in_time", length = 10)
    private String checkInTime;

    @Column(name = "check_out_time", length = 10)
    private String checkOutTime;

    @Column(name = "total_rooms")
    private Integer totalRooms;

    @Column(name = "image_urls", columnDefinition = "TEXT")
    private String imageUrls;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HotelStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 연관관계
    @OneToMany(mappedBy = "hotel", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Room> rooms;

    @OneToMany(mappedBy = "hotel", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<HotelAmenity> hotelAmenities;

    @OneToMany(mappedBy = "hotel", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Reservation> reservations;

    @OneToMany(mappedBy = "hotel", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Review> reviews;

    public enum HotelStatus {
        ACTIVE, INACTIVE, SUSPENDED
    }
}