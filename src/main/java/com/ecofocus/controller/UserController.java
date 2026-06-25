package com.ecofocus.controller;

import com.ecofocus.service.AuthService;
import com.ecofocus.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @PutMapping("/{userId}/vacation-mode")
    public ResponseEntity<?> toggleVacationMode(@PathVariable Long userId, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            final String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer "))
                return ResponseEntity.status(401).body(Map.of("message", "Yetkisiz"));
            final Long requestingUserId = authService.getUserIdByEmail(jwtUtil.extractEmail(authHeader.substring(7)));
            if (!requestingUserId.equals(userId))
                return ResponseEntity.status(403).body(Map.of("message", "Yetkisiz"));
            boolean enabled = Boolean.parseBoolean(body.get("enabled").toString());
            boolean result = authService.setVacationMode(userId, enabled);
            return ResponseEntity.ok(Map.of("vacationMode", result));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteAccount(@PathVariable Long userId, HttpServletRequest request) {
        try {
            // Token'dan gerçek userId'yi al
            final String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer "))
                return ResponseEntity.status(401).body(Map.of("message", "Yetkisiz"));
            final String token = authHeader.substring(7);
            final String email = jwtUtil.extractEmail(token);
            final Long requestingUserId = authService.getUserIdByEmail(email);

            authService.deleteAccount(requestingUserId, userId);
            return ResponseEntity.ok(Map.of("message", "Hesap silindi"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
