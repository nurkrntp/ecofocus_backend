package com.ecofocus.controller;

import com.ecofocus.dto.*;
import com.ecofocus.repository.UserRepository;
import com.ecofocus.security.JwtUtil;
import com.ecofocus.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {
    private final SessionService sessionService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<?> completeSession(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> body) {
        try {
            String email = jwtUtil.extractEmail(authHeader.replace("Bearer ", ""));
            Long userId = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"))
                    .getId();

            // Flutter planned_duration veya duration gönderebilir
            int duration = 0;
            if (body.containsKey("planned_duration"))
                duration = ((Number) body.get("planned_duration")).intValue();
            else if (body.containsKey("duration"))
                duration = ((Number) body.get("duration")).intValue();

            String name = body.containsKey("name") ? (String) body.get("name") : null;

            SessionRequest req = new SessionRequest();
            req.setUserId(userId);
            req.setDuration(duration);
            req.setName(name != null && !name.isBlank() ? name : null);

            return ResponseEntity.ok(sessionService.completeSession(req));
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getSessions(
            @RequestHeader("Authorization") String authHeader) {
        try {
            String email = jwtUtil.extractEmail(authHeader.replace("Bearer ", ""));
            Long userId = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"))
                    .getId();
            return ResponseEntity.ok(sessionService.getSessions(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}