package com.ecofocus.service;

import com.ecofocus.entity.RefreshToken;
import com.ecofocus.entity.User;
import com.ecofocus.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository repository;

    // 30 gün geçerli
    private static final long REFRESH_TOKEN_DAYS = 30;

    @Transactional
    public String create(User user) {
        // Kullanıcının eski tokenlarını sil (bir kullanıcı = bir refresh token)
        repository.deleteByUserId(user.getId());
        String token = UUID.randomUUID().toString();
        repository.save(RefreshToken.builder()
                .token(token)
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(REFRESH_TOKEN_DAYS))
                .build());
        return token;
    }

    @Transactional
    public Optional<RefreshToken> findValid(String token) {
        return repository.findByToken(token)
                .filter(t -> t.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(t -> {
                    // Her kullanımda süreyi sıfırla — aktif kullanıcı hiç login ekranı görmez
                    t.setExpiresAt(LocalDateTime.now().plusDays(REFRESH_TOKEN_DAYS));
                    return repository.save(t);
                });
    }

    @Transactional
    public void deleteByUserId(Long userId) {
        repository.deleteByUserId(userId);
    }

    // Her gece süresi dolmuş tokenları temizle
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanup() {
        repository.deleteExpired(LocalDateTime.now());
    }
}
