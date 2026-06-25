package com.ecofocus.controller;

import com.ecofocus.dto.BuyRequest;
import com.ecofocus.dto.MoveRequest;
import com.ecofocus.dto.PlaceRequest;
import com.ecofocus.security.JwtUtil;
import com.ecofocus.service.AuthService;
import com.ecofocus.service.ShopService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;
    private final AuthService authService;
    private final JwtUtil jwtUtil;

    private Long extractUserId(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer "))
            throw new RuntimeException("Yetkisiz");
        return authService.getUserIdByEmail(jwtUtil.extractEmail(header.substring(7)));
    }

    @GetMapping
    public ResponseEntity<?> getItems(@RequestParam Long userId) {
        try { return ResponseEntity.ok(shopService.getShopItems(userId)); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("message", e.getMessage())); }
    }

    @PostMapping("/buy")
    public ResponseEntity<?> buy(@RequestBody BuyRequest req, HttpServletRequest request) {
        try {
            if (!extractUserId(request).equals(req.getUserId()))
                return ResponseEntity.status(403).body(Map.of("message", "Yetkisiz"));
            return ResponseEntity.ok(shopService.buy(req));
        } catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("message", e.getMessage())); }
    }

    @PostMapping("/place")
    public ResponseEntity<?> place(@RequestBody PlaceRequest req, HttpServletRequest request) {
        try {
            if (!extractUserId(request).equals(req.getUserId()))
                return ResponseEntity.status(403).body(Map.of("message", "Yetkisiz"));
            return ResponseEntity.ok(shopService.place(req));
        } catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("message", e.getMessage())); }
    }

    @PatchMapping("/world-objects/{objectId}/move")
    public ResponseEntity<?> moveObject(@PathVariable Long objectId, @RequestBody MoveRequest req, HttpServletRequest request) {
        try {
            if (!extractUserId(request).equals(req.getUserId()))
                return ResponseEntity.status(403).body(Map.of("message", "Yetkisiz"));
            shopService.moveWorldObject(objectId, req);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/world-objects/{objectId}")
    public ResponseEntity<?> removeFromWorld(@PathVariable Long objectId, @RequestParam Long userId, HttpServletRequest request) {
        try {
            if (!extractUserId(request).equals(userId))
                return ResponseEntity.status(403).body(Map.of("message", "Yetkisiz"));
            return ResponseEntity.ok(shopService.removeFromWorld(objectId, userId));
        } catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("message", e.getMessage())); }
    }
}
