package com.example.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "room_price_policies")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomPricePolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "policy_name", nullable = false, length = 100)
    private String policyName;

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "pricing_type", nullable = false)
    private PricingType pricingType;

    @Column(name = "day_of_week")
    private Integer dayOfWeek; // 0=Sunday, 6=Saturday

    @Column(name = "season_start_month")
    private Integer seasonStartMonth;

    @Column(name = "season_end_month")
    private Integer seasonEndMonth;

    @Column(name = "extra_guest_fee", precision = 10, scale = 2)
    private BigDecimal extraGuestFee;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum PricingType {
        STANDARD, WEEKEND, HOLIDAY, PEAK_SEASON, OFF_SEASON
    }
}