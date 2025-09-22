package com.example.backend.config;

import com.example.backend.domain.Business;
import com.example.backend.domain.User;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BusinessRoleReconciler implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        // 1) 승인된 사업자의 사용자들은 BUSINESS 역할을 유지
        // 2) 그 외 BUSINESS 역할인데 승인된 Business가 없는 사용자는 USER로 다운그레이드
        List<User> users = userRepository.findAll();
        int fixed = 0;
        for (User u : users) {
            if (u.getRole() == User.Role.BUSINESS) {
                Business b = u.getBusiness();
                boolean approved = b != null && b.getStatus() == Business.BusinessStatus.APPROVED;
                if (!approved) {
                    u.setRole(User.Role.USER);
                    fixed++;
                }
            }
        }
        if (fixed > 0) {
            userRepository.saveAll(users);
            log.info("Reconciled BUSINESS roles without approved Business: {} user(s) downgraded to USER.", fixed);
        }
    }
}
