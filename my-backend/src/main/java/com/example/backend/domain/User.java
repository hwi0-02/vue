package com.example.backend.domain;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(length = 20)
    private String phone;
    
    @Column(length = 500)
    private String address;
    
    @Column(length = 500)
    private String profileImageUrl;
    
    @Column(length = 100)
    private String providerId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Provider provider;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @CreationTimestamp
    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_on")
    private LocalDateTime updatedAt;

    // 연관관계
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Business business;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Reservation> reservations;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Review> reviews;

    public enum Role {
        USER, BUSINESS, ADMIN
    }
    
    public enum Provider {
        LOCAL, GOOGLE, NAVER, KAKAO
    }
    
    public enum Status {
        ACTIVE, INACTIVE, SUSPENDED
    }
    
    // 비즈니스 로직 메소드
    public void update(String name, String profileImageUrl) {
        this.name = name;
        this.profileImageUrl = profileImageUrl;
    }
    
    public boolean hasSocialProvider(Provider provider) {
        return this.provider == provider;
    }
    
    public void mergeSocialProvider(Provider newProvider, String newProviderId) {
        // 소셜 계정 통합 로직 (현재는 간단히 업데이트)
        this.provider = newProvider;
        this.providerId = newProviderId;
    }
    
    // Getter 메소드들 (Lombok으로 자동 생성되지만 호환성을 위해 명시적 추가)
    public String getPhone() {
        return phone;
    }
    
    public String getProviderId() {
        return providerId;
    }
    
    public LocalDateTime getCreatedOn() {
        return createdAt;
    }
}