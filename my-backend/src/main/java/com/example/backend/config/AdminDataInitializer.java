package com.example.backend.config;

import com.example.backend.domain.User;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        createAdminUserIfNotExists();
    }

    private void createAdminUserIfNotExists() {
        String adminEmail = "admin@hotel.com";
        
        if (!userRepository.existsByEmail(adminEmail)) {
            User adminUser = User.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode("admin123!"))
                    .name("시스템 관리자")
                    .phone("010-0000-0000")
                    .provider(User.Provider.LOCAL)
                    .role(User.Role.ADMIN)
                    .status(User.Status.ACTIVE)
                    .build();
            
            userRepository.save(adminUser);
            log.info("관리자 계정이 생성되었습니다. 이메일: {}", adminEmail);
        } else {
            log.info("관리자 계정이 이미 존재합니다.");
        }
    }
}