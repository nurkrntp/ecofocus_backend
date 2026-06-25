package com.ecofocus.service;

import com.ecofocus.dto.*;
import com.ecofocus.entity.*;
import com.ecofocus.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SessionService {
    private final FocusSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final WorldRepository worldRepository;

    @Transactional
    public SessionDto completeSession(SessionRequest req) {
        if (req.getDuration() < 1 || req.getDuration() > 120)
            throw new RuntimeException("Geçersiz süre");

        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        int points = req.getDuration();

        FocusSession session = FocusSession.builder()
                .user(user).duration(req.getDuration()).points(points)
                .name(req.getName()).build();
        session = sessionRepository.save(session);

        // Dünyaya puan ekle — artık otomatik unlock yok, satın alma shop üzerinden
        worldRepository.findByUserId(user.getId()).ifPresent(world -> {
            world.addPoints(points);
            worldRepository.save(world);
        });

        return new SessionDto(session.getId(), user.getId(),
                session.getDuration(), session.getPoints(),
                session.getCompletedAt() != null ? session.getCompletedAt().toString() : null,
                session.getName());
    }

    public List<SessionDto> getSessions(Long userId) {
        return sessionRepository.findByUserIdOrderByCompletedAtDesc(userId).stream()
                .map(s -> new SessionDto(s.getId(), s.getUser().getId(),
                        s.getDuration(), s.getPoints(),
                        s.getCompletedAt() != null ? s.getCompletedAt().toString() : null,
                        s.getName()))
                .collect(Collectors.toList());
    }
}
