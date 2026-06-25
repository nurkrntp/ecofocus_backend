package com.ecofocus.controller;

import com.ecofocus.security.JwtUtil;
import com.ecofocus.service.AuthService;
import com.ecofocus.service.TeamService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;
    private final AuthService authService;
    private final JwtUtil jwtUtil;

    private Long extractUserId(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer "))
            throw new RuntimeException("Yetkisiz");
        return authService.getUserIdByEmail(jwtUtil.extractEmail(header.substring(7)));
    }

    private ResponseEntity<?> forbidden() {
        return ResponseEntity.status(403).body(Map.of("message", "Yetkisiz"));
    }

    @GetMapping
    public ResponseEntity<?> getTeams(@RequestParam Long userId) {
        try { return ResponseEntity.ok(teamService.getTeams(userId)); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("message", e.getMessage())); }
    }

    @PostMapping
    public ResponseEntity<?> createTeam(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            Long userId = Long.valueOf(body.get("userId").toString());
            if (!extractUserId(request).equals(userId)) return forbidden();
            return ResponseEntity.ok(teamService.createTeam(userId, body.get("name").toString()));
        } catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("message", e.getMessage())); }
    }

@PostMapping("/{teamId}/members")
    public ResponseEntity<?> addMember(@PathVariable Long teamId, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            Long userId = Long.valueOf(body.get("userId").toString());
            if (!extractUserId(request).equals(userId)) return forbidden();
            return ResponseEntity.ok(teamService.addMember(teamId, userId, body.get("username").toString()));
        } catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("message", e.getMessage())); }
    }

    @DeleteMapping("/{teamId}/members/{memberId}")
    public ResponseEntity<?> removeMember(@PathVariable Long teamId, @PathVariable Long memberId, @RequestParam Long requesterId, HttpServletRequest request) {
        try {
            if (!extractUserId(request).equals(requesterId)) return forbidden();
            return ResponseEntity.ok(teamService.removeMember(teamId, requesterId, memberId));
        } catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("message", e.getMessage())); }
    }

    @DeleteMapping("/{teamId}/leave")
    public ResponseEntity<?> leaveTeam(@PathVariable Long teamId, @RequestParam Long userId, HttpServletRequest request) {
        try {
            if (!extractUserId(request).equals(userId)) return forbidden();
            teamService.leaveTeam(teamId, userId);
            return ResponseEntity.ok(Map.of("message", "OK"));
        } catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("message", e.getMessage())); }
    }

    @DeleteMapping("/{teamId}")
    public ResponseEntity<?> deleteTeam(@PathVariable Long teamId, @RequestParam Long userId, HttpServletRequest request) {
        try {
            if (!extractUserId(request).equals(userId)) return forbidden();
            teamService.deleteTeam(teamId, userId);
            return ResponseEntity.ok(Map.of("message", "OK"));
        } catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("message", e.getMessage())); }
    }

    @GetMapping("/challenges/my-completions")
    public ResponseEntity<?> getMyCompletedChallenges(@RequestParam Long userId) {
        try { return ResponseEntity.ok(teamService.getMyCompletedChallenges(userId)); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("message", e.getMessage())); }
    }

    @GetMapping("/{teamId}/challenges")
    public ResponseEntity<?> getChallenges(@PathVariable Long teamId, @RequestParam Long userId) {
        try { return ResponseEntity.ok(teamService.getChallenges(teamId, userId)); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("message", e.getMessage())); }
    }

    @PostMapping("/{teamId}/challenges")
    public ResponseEntity<?> createChallenge(@PathVariable Long teamId, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            Long userId = Long.valueOf(body.get("userId").toString());
            if (!extractUserId(request).equals(userId)) return forbidden();
            return ResponseEntity.ok(teamService.createChallenge(teamId, userId,
                    body.get("challengeName").toString(),
                    Integer.parseInt(body.get("duration").toString()),
                    Integer.parseInt(body.get("validityDays").toString())));
        } catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("message", e.getMessage())); }
    }

    @DeleteMapping("/challenges/{challengeId}")
    public ResponseEntity<?> deleteTeamChallenge(@PathVariable Long challengeId, @RequestParam Long userId, HttpServletRequest request) {
        try {
            if (!extractUserId(request).equals(userId)) return forbidden();
            teamService.deleteTeamChallenge(challengeId, userId);
            return ResponseEntity.ok(Map.of("message", "OK"));
        } catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("message", e.getMessage())); }
    }

@PostMapping("/challenges/{challengeId}/complete")
    public ResponseEntity<?> completeChallenge(@PathVariable Long challengeId, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            Long userId = Long.valueOf(body.get("userId").toString());
            if (!extractUserId(request).equals(userId)) return forbidden();
            return ResponseEntity.ok(teamService.completeChallenge(challengeId, userId));
        } catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("message", e.getMessage())); }
    }
}
