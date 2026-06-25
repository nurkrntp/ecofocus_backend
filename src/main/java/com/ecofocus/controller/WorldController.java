package com.ecofocus.controller;

import com.ecofocus.service.WorldService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/worlds")
@RequiredArgsConstructor
public class WorldController {
    private final WorldService worldService;

    @GetMapping("/{userId}")
    public ResponseEntity<?> getWorld(@PathVariable Long userId) {
        try { return ResponseEntity.ok(worldService.getWorld(userId)); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("message", e.getMessage())); }
    }
}
