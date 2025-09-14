package com.example.backend;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class LoginController {
    
    private final LoginService loginService;
    private final JwtUtil jwtUtil;
    private final LoginRepository loginRepository;
    
    // 회원가입
    @PostMapping("/users/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        log.info("User registration attempt for email: {}", user.getEmail());
        User savedUser = loginService.register(user);
        return ResponseEntity.ok(savedUser);
    }

    // 로그인
    @PostMapping("/users/login")
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String password) {
        log.info("Login attempt for email: {}", email);
        Optional<User> user = loginService.login(email, password);
        
        if (user.isPresent()) {
            String token = jwtUtil.generateToken(user.get());
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", getUserInfo(user.get()));
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body("로그인 실패: 이메일 또는 비밀번호 불일치");
        }
    }

    // 사용자 정보 조회 (JWT 토큰 기반)
    @GetMapping("/user/info")
    public ResponseEntity<?> getUserInfo(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body("Invalid authorization header");
            }

            String token = authHeader.substring(7);
            String email = jwtUtil.extractEmail(token);
            
            if (!jwtUtil.validateToken(token, email)) {
                return ResponseEntity.status(401).body("Token validation failed");
            }

            User user = loginRepository.findByEmail(email)
                    .orElse(null);

            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(getUserInfo(user));
            
        } catch (Exception e) {
            log.error("Error getting user info: {}", e.getMessage());
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    // 사용자 ID로 조회
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return loginService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private Map<String, Object> getUserInfo(User user) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("name", user.getName());
        userInfo.put("email", user.getEmail());
        userInfo.put("provider", user.getProvider().toString());
        userInfo.put("profileImageUrl", user.getProfileImageUrl());
        userInfo.put("createdOn", user.getCreatedOn());
        return userInfo;
    }
}