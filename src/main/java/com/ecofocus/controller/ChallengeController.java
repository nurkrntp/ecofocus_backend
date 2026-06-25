package com.ecofocus.controller;

import com.ecofocus.dto.*;
import com.ecofocus.service.AuthService;
import com.ecofocus.service.ChallengeService;
import com.ecofocus.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/challenges")
@RequiredArgsConstructor
public class ChallengeController {
    private final ChallengeService challengeService;
    private final AuthService authService;
    private final JwtUtil jwtUtil;

    private Long extractUserId(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer "))
            throw new RuntimeException("Yetkisiz");
        return authService.getUserIdByEmail(jwtUtil.extractEmail(header.substring(7)));
    }

    @GetMapping
    public ResponseEntity<?> getChallenges(@RequestParam Long userId) {
        try { return ResponseEntity.ok(challengeService.getChallenges(userId)); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("message", e.getMessage())); }
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendChallenge(@RequestBody ChallengeRequest req, HttpServletRequest request) {
        try {
            Long tokenUserId = extractUserId(request);
            if (!tokenUserId.equals(req.getSenderId()))
                return ResponseEntity.status(403).body(Map.of("message", "Yetkisiz"));
            return ResponseEntity.ok(challengeService.sendChallenge(req));
        } catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("message", e.getMessage())); }
    }

    @PatchMapping("/{challengeId}/accept")
    public ResponseEntity<?> accept(@PathVariable Long challengeId, @RequestParam Long userId, HttpServletRequest request) {
        try {
            if (!extractUserId(request).equals(userId))
                return ResponseEntity.status(403).body(Map.of("message", "Yetkisiz"));
            challengeService.acceptChallenge(challengeId, userId);
            return ResponseEntity.ok(Map.of("message", "OK"));
        } catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("message", e.getMessage())); }
    }

    @DeleteMapping("/{challengeId}/reject")
    public ResponseEntity<?> reject(@PathVariable Long challengeId, @RequestParam Long userId, HttpServletRequest request) {
        try {
            if (!extractUserId(request).equals(userId))
                return ResponseEntity.status(403).body(Map.of("message", "Yetkisiz"));
            challengeService.rejectChallenge(challengeId, userId);
            return ResponseEntity.ok(Map.of("message", "OK"));
        } catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("message", e.getMessage())); }
    }

    @DeleteMapping("/{challengeId}")
    public ResponseEntity<?> cancel(@PathVariable Long challengeId, @RequestParam Long userId, HttpServletRequest request) {
        try {
            if (!extractUserId(request).equals(userId))
                return ResponseEntity.status(403).body(Map.of("message", "Yetkisiz"));
            challengeService.cancelChallenge(challengeId, userId);
            return ResponseEntity.ok(Map.of("message", "OK"));
        } catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("message", e.getMessage())); }
    }

    @PatchMapping("/{challengeId}/complete")
    public ResponseEntity<?> complete(@PathVariable Long challengeId, @RequestParam Long userId, HttpServletRequest request) {
        try {
            if (!extractUserId(request).equals(userId))
                return ResponseEntity.status(403).body(Map.of("message", "Yetkisiz"));
            challengeService.completeChallenge(challengeId, userId);
            return ResponseEntity.ok(Map.of("message", "OK"));
        } catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("message", e.getMessage())); }
    }
}
