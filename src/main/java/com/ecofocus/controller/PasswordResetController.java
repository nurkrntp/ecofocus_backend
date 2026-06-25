package com.ecofocus.controller;

import com.ecofocus.service.PasswordResetService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/password-reset")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    /** 1. Adım: Kodu e-postaya gönder */
    @PostMapping("/send-code")
    public ResponseEntity<?> sendCode(@RequestBody Map<String, String> body) {
        try {
            passwordResetService.sendCode(body.get("email"));
            return ResponseEntity.ok(Map.of("message", "Kod gönderildi"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /** 2. Adım: Kodu doğrula */
    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> body) {
        try {
            passwordResetService.verifyCode(body.get("email"), body.get("code"));
            return ResponseEntity.ok(Map.of("message", "Kod doğrulandı"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /** 3. Adım: Yeni şifreyi kaydet */
    @PostMapping("/reset")
    public ResponseEntity<?> reset(@RequestBody Map<String, String> body) {
        try {
            passwordResetService.resetPassword(body.get("email"), body.get("code"), body.get("newPassword"));
            return ResponseEntity.ok(Map.of("message", "Şifre güncellendi"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
