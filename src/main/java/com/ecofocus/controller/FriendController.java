package com.ecofocus.controller;

import com.ecofocus.dto.*;
import com.ecofocus.security.JwtUtil;
import com.ecofocus.service.AuthService;
import com.ecofocus.service.FriendService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {
    private final FriendService friendService;
    private final AuthService authService;
    private final JwtUtil jwtUtil;

    private Long extractUserId(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer "))
            throw new RuntimeException("Yetkisiz");
        return authService.getUserIdByEmail(jwtUtil.extractEmail(header.substring(7)));
    }

    @GetMapping
    public ResponseEntity<?> getFriends(@RequestParam Long userId) {
        try { return ResponseEntity.ok(friendService.getFriends(userId)); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("message", e.getMessage())); }
    }

    @PostMapping("/request")
    public ResponseEntity<?> sendRequest(@RequestBody FriendRequest req, HttpServletRequest request) {
        try {
            if (!extractUserId(request).equals(req.getUserId()))
                return ResponseEntity.status(403).body(Map.of("message", "Yetkisiz"));
            return ResponseEntity.ok(friendService.sendRequest(req));
        } catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("message", e.getMessage())); }
    }

    @DeleteMapping("/{friendshipId}")
    public ResponseEntity<?> removeFriend(@PathVariable Long friendshipId, HttpServletRequest request) {
        try {
            Long jwtUserId = extractUserId(request);
            friendService.removeFriend(friendshipId, jwtUserId);
            return ResponseEntity.ok(Map.of("message", "Arkadaşlık silindi"));
        } catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("message", e.getMessage())); }
    }

    @DeleteMapping("/{friendshipId}/cancel")
    public ResponseEntity<?> cancelRequest(@PathVariable Long friendshipId, HttpServletRequest request) {
        try {
            Long jwtUserId = extractUserId(request);
            friendService.cancelRequest(friendshipId, jwtUserId);
            return ResponseEntity.ok(Map.of("message", "İstek geri alındı"));
        } catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("message", e.getMessage())); }
    }

    @PatchMapping("/{friendshipId}")
    public ResponseEntity<?> respond(@PathVariable Long friendshipId, @RequestBody FriendStatusRequest req, HttpServletRequest request) {
        try {
            Long jwtUserId = extractUserId(request);
            return ResponseEntity.ok(friendService.respond(friendshipId, req.getStatus(), jwtUserId));
        } catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("message", e.getMessage())); }
    }
}
