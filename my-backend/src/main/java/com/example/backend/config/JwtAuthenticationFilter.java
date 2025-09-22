package com.example.backend.config;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.example.backend.repository.LoginRepository;
import com.example.backend.domain.User;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final LoginRepository loginRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain)
            throws ServletException, IOException {

        final String requestTokenHeader = request.getHeader("Authorization");
        if (requestTokenHeader == null) {
            log.debug("JWT Filter: Authorization header missing for {} {}", request.getMethod(), request.getRequestURI());
        } else {
            log.debug("JWT Filter: Authorization header present: {}", requestTokenHeader.substring(0, Math.min(20, requestTokenHeader.length())) + "...");
        }

        String email = null;
        String jwtToken = null;

        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                email = jwtUtil.extractEmail(jwtToken);
                log.debug("JWT Filter: Extracted email {} from token", email);
            } catch (Exception e) {
                log.warn("JWT Filter: Unable to parse JWT token: {}", e.getMessage());
            }
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            boolean valid = jwtUtil.validateToken(jwtToken, email);
            log.debug("JWT Filter: Token valid for {}: {}", email, valid);
            if (valid) {
                User user = loginRepository.findByEmail(email).orElse(null);
                java.util.Collection<? extends GrantedAuthority> authorities;
                if (user == null) {
                    log.warn("JWT Filter: User not found for email {}", email);
                    authorities = java.util.Collections.emptyList();
                } else {
                    log.debug("JWT Filter: User {} has role {}", email, user.getRole());
                    authorities = java.util.List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
                }

                UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(email, null, authorities);
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
                log.debug("JWT Filter: SecurityContext set for {} with authorities {}", email, authorities);
            } else {
                log.debug("JWT Filter: Invalid token for {}", email);
            }
        }
        chain.doFilter(request, response);
    }
}