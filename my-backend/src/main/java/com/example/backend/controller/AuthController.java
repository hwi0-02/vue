package com.example.backend.controller;

import com.example.backend.domain.User;
import com.example.backend.repository.LoginRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final LoginRepository loginRepository;

    @GetMapping("/whoami")
    public ResponseEntity<?> whoami(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        String email = authentication.getName();
        List<String> authorities = authentication.getAuthorities()
                .stream().map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        User user = loginRepository.findByEmail(email).orElse(null);

        Map<String, Object> resp = new HashMap<>();
        resp.put("authenticated", true);
        resp.put("email", email);
        resp.put("authorities", authorities);
        if (user != null) {
            resp.put("role", user.getRole().toString());
            resp.put("provider", user.getProvider().toString());
            resp.put("id", user.getId());
        }
        return ResponseEntity.ok(resp);
    }
}
