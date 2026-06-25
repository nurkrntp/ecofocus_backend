package com.ecofocus.service;

import com.ecofocus.dto.*;
import com.ecofocus.entity.*;
import com.ecofocus.repository.*;
import com.ecofocus.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final WorldRepository worldRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final PendingRegistrationRepository pendingRepository;
    private final EmailService emailService;
    private final FriendRepository friendRepository;
    private final FocusChallengeRepository challengeRepository;
    private final FocusSessionRepository sessionRepository;
    private final UserItemRepository userItemRepository;
    private final TeamRepository teamRepository;
    private final TeamChallengeCompletionRepository teamChallengeCompletionRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RefreshTokenService refreshTokenService;

    /** 1. Adım: Bilgileri doğrula, kod gönder */
    @Transactional
    public void sendRegistrationCode(RegisterRequest req) {
        if (req.getUsername().contains(" "))
            throw new RuntimeException("Kullanıcı adında boşluk olamaz");
        _validatePassword(req.getPassword());
        if (userRepository.existsByEmail(req.getEmail()))
            throw new RuntimeException("Bu e-posta zaten kayıtlı");
        if (userRepository.existsByUsername(req.getUsername()))
            throw new RuntimeException("Bu kullanıcı adı zaten alınmış");

        pendingRepository.deleteAllByEmail(req.getEmail());

        String code = String.format("%06d", new Random().nextInt(999999));
        pendingRepository.save(PendingRegistration.builder()
                .email(req.getEmail())
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build());

        emailService.sendRegistrationCode(req.getEmail(), code);
    }

    /** 2. Adım: Kodu doğrula, hesabı oluştur */
    @Transactional
    public AuthResponse verifyAndRegister(String email, String code) {
        PendingRegistration pending = pendingRepository.findByEmailAndCode(email, code)
                .orElseThrow(() -> new RuntimeException("Kod geçersiz veya süresi dolmuş"));
        if (pending.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new RuntimeException("Kodun süresi dolmuş, tekrar kayıt ol");

        User user = User.builder()
                .username(pending.getUsername())
                .email(pending.getEmail())
                .password(pending.getPassword())
                .build();
        user = userRepository.save(user);

        World world = new World();
        world.setUser(user);
        WorldObject tree = WorldObject.builder()
                .world(world).type("tree").tileX(2).tileY(2).build();
        world.getObjects().add(tree);
        worldRepository.save(world);

        pendingRepository.deleteAllByEmail(email);

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail()))
            throw new RuntimeException("Bu e-posta zaten kayıtlı");
        if (userRepository.existsByUsername(req.getUsername()))
            throw new RuntimeException("Bu kullanıcı adı zaten alınmış");

        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .build();
        user = userRepository.save(user);

        World world = new World();
        world.setUser(user);
        WorldObject tree = WorldObject.builder()
                .world(world).type("tree").tileX(2).tileY(2).build();
        world.getObjects().add(tree);
        worldRepository.save(world);

        return buildAuthResponse(user);
    }

    @Transactional
    public boolean setVacationMode(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        user.setVacationMode(enabled);
        userRepository.save(user);
        return enabled;
    }

    @Transactional
    public UserDto updateAvatar(Long userId, Integer avatarId) {
        if (avatarId < 1 || avatarId > 26) throw new RuntimeException("Geçersiz avatar");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        user.setAvatarId(avatarId);
        user = userRepository.save(user);
        return new UserDto(user.getId(), user.getUsername(), user.getEmail(), user.getAvatarId(), user.getCreatedAt(), user.getVacationMode() != null ? user.getVacationMode() : false);
    }

    @Transactional
    public void deleteAccount(Long requestingUserId, Long targetUserId) {
        // Güvenlik: sadece kendi hesabını silebilir
        if (!requestingUserId.equals(targetUserId))
            throw new RuntimeException("Başkasının hesabını silemezsiniz");

        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        // 1. Şifre sıfırlama tokenları
        passwordResetTokenRepository.deleteAllByEmail(user.getEmail());
        // 2. Bekleyen kayıtlar
        pendingRepository.deleteAllByEmail(user.getEmail());
        // 3. Challenge'lar: tamamlanmışları tut (FK null yap), diğerlerini sil
        challengeRepository.findBySenderIdOrReceiverId(targetUserId, targetUserId).forEach(c -> {
            if ("COMPLETED".equals(c.getStatus())) {
                if (c.getSender() != null && c.getSender().getId().equals(targetUserId)) c.setSender(null);
                if (c.getReceiver() != null && c.getReceiver().getId().equals(targetUserId)) c.setReceiver(null);
                challengeRepository.save(c);
            } else {
                challengeRepository.delete(c);
            }
        });
        // 4. Seanslar
        sessionRepository.findByUserIdOrderByCompletedAtDesc(targetUserId)
                .forEach(sessionRepository::delete);
        // 5. Envanter
        userItemRepository.findByUserId(targetUserId)
                .forEach(userItemRepository::delete);
        // 6. Arkadaşlıklar
        friendRepository.findByUserId(targetUserId).forEach(friendRepository::delete);
        friendRepository.findByFriendId(targetUserId).forEach(friendRepository::delete);
        // 7. Ekip: kurucuysa sahipliği devret, değilse üyelikten çık
        teamRepository.findByCreatorId(targetUserId).forEach(t -> {
            List<com.ecofocus.entity.TeamMember> others = t.getMembers().stream()
                    .filter(m -> !m.getUser().getId().equals(targetUserId))
                    .collect(java.util.stream.Collectors.toList());
            if (others.isEmpty()) {
                teamRepository.delete(t);
            } else {
                t.setCreator(others.get(0).getUser());
                t.getMembers().removeIf(m -> m.getUser().getId().equals(targetUserId));
                teamRepository.save(t);
            }
        });
        teamRepository.findByMemberUserId(targetUserId).forEach(t ->
            t.getMembers().removeIf(m -> m.getUser().getId().equals(targetUserId)));
        // 8. Ekip challenge tamamlamaları
        teamChallengeCompletionRepository.deleteByUserId(targetUserId);
        // 9. Dünya (cascade ile world_objects da silinir)
        worldRepository.findByUserId(targetUserId).ifPresent(worldRepository::delete);
        // 9. Refresh tokenlar
        refreshTokenService.deleteByUserId(targetUserId);
        // 10. Kullanıcı
        userRepository.delete(user);
    }

    public Long getUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"))
                .getId();
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("E-posta veya şifre hatalı"));
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword()))
            throw new RuntimeException("E-posta veya şifre hatalı");

        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtUtil.generateToken(user.getEmail());
        String refreshToken = refreshTokenService.create(user);
        return new AuthResponse(token, refreshToken, new UserDto(
                user.getId(), user.getUsername(), user.getEmail(),
                user.getAvatarId() != null ? user.getAvatarId() : 1,
                user.getCreatedAt(),
                user.getVacationMode() != null ? user.getVacationMode() : false));
    }

    private void _validatePassword(String password) {
        if (password == null || password.length() < 8)
            throw new RuntimeException("Şifre en az 8 karakter olmalıdır");
        if (!password.chars().anyMatch(Character::isDigit))
            throw new RuntimeException("Şifre en az bir rakam içermelidir");
        if (!password.chars().anyMatch(c -> !Character.isLetterOrDigit(c)))
            throw new RuntimeException("Şifre en az bir özel karakter içermelidir (!@#$% vb.)");
    }
}
