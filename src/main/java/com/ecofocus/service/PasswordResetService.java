package com.ecofocus.service;

import com.ecofocus.entity.PasswordResetToken;
import com.ecofocus.repository.PasswordResetTokenRepository;
import com.ecofocus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public void sendCode(String email) {
        if (!userRepository.existsByEmail(email))
            throw new RuntimeException("Bu e-posta kayıtlı değil");

        // Önceki kodları temizle
        tokenRepository.deleteAllByEmail(email);

        String code = String.format("%06d", new Random().nextInt(999999));

        PasswordResetToken token = PasswordResetToken.builder()
                .email(email)
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();
        tokenRepository.save(token);

        emailService.sendPasswordResetCode(email, code);
    }

    @Transactional
    public void verifyCode(String email, String code) {
        PasswordResetToken token = tokenRepository
                .findByEmailAndCodeAndUsedFalse(email, code)
                .orElseThrow(() -> new RuntimeException("Kod geçersiz veya süresi dolmuş"));

        if (token.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new RuntimeException("Kodun süresi dolmuş, yeni kod talep edin");
    }

    @Transactional
    public void resetPassword(String email, String code, String newPassword) {
        PasswordResetToken token = tokenRepository
                .findByEmailAndCodeAndUsedFalse(email, code)
                .orElseThrow(() -> new RuntimeException("Kod geçersiz veya süresi dolmuş"));

        if (token.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new RuntimeException("Kodun süresi dolmuş, yeni kod talep edin");

        if (newPassword == null || newPassword.length() < 8)
            throw new RuntimeException("Şifre en az 8 karakter olmalıdır");
        if (!newPassword.chars().anyMatch(Character::isDigit))
            throw new RuntimeException("Şifre en az bir rakam içermelidir");
        if (!newPassword.chars().anyMatch(c -> !Character.isLetterOrDigit(c)))
            throw new RuntimeException("Şifre en az bir özel karakter içermelidir (!@#$% vb.)");

        userRepository.findByEmail(email).ifPresent(user -> {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            // Şifre değişince tüm refresh tokenları geçersiz kıl
            refreshTokenService.deleteByUserId(user.getId());
        });

        token.setUsed(true);
        tokenRepository.save(token);
    }
}
