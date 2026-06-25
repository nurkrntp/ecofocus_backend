package com.ecofocus.controller;

import com.ecofocus.dto.*;
import com.ecofocus.security.JwtUtil;
import com.ecofocus.service.AuthService;
import com.ecofocus.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
private Long extractUserId(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer "))
            throw new RuntimeException("Yetkisiz");
        return authService.getUserIdByEmail(jwtUtil.extractEmail(header.substring(7)));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        try { return ResponseEntity.ok(authService.register(req)); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("message", e.getMessage())); }
    }

    @PostMapping("/register/send-code")
    public ResponseEntity<?> registerSendCode(@RequestBody RegisterRequest req) {
        try { authService.sendRegistrationCode(req); return ResponseEntity.ok(Map.of("message", "Kod gönderildi")); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("message", e.getMessage())); }
    }

    @PostMapping("/register/verify")
    public ResponseEntity<?> registerVerify(@RequestBody Map<String, String> body) {
        try { return ResponseEntity.ok(authService.verifyAndRegister(body.get("email"), body.get("code"))); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("message", e.getMessage())); }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try { return ResponseEntity.ok(authService.login(req)); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("message", e.getMessage())); }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {
        try {
            String refreshToken = body.get("refreshToken");
            return refreshTokenService.findValid(refreshToken)
                    .map(rt -> {
                        String newAccessToken = jwtUtil.generateToken(rt.getUser().getEmail());
                        return ResponseEntity.ok(Map.of("token", newAccessToken));
                    })
                    .orElse(ResponseEntity.status(401).body(Map.of("message", "Geçersiz veya süresi dolmuş refresh token")));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/avatar")
    public ResponseEntity<?> updateAvatar(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            Long userId = Long.valueOf(body.get("userId").toString());
            if (!extractUserId(request).equals(userId))
                return ResponseEntity.status(403).body(Map.of("message", "Yetkisiz"));
            Integer avatarId = Integer.valueOf(body.get("avatarId").toString());
            return ResponseEntity.ok(authService.updateAvatar(userId, avatarId));
        } catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("message", e.getMessage())); }
    }
}
