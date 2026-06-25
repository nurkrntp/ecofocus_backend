package com.ecofocus.service;

import com.ecofocus.dto.*;
import com.ecofocus.entity.*;
import com.ecofocus.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChallengeService {
    private final FocusChallengeRepository challengeRepository;
    private final UserRepository userRepository;
    private final WorldRepository worldRepository;

    @Transactional
    public List<ChallengeDto> getChallenges(Long userId) {
        // 3 günden eski PENDING challengeları DB'den sil
        challengeRepository.deleteExpired(LocalDateTime.now().minusDays(3));
        // 24 saat içinde tamamlanmamış ACCEPTED challengeları sil
        challengeRepository.deleteExpiredAccepted(LocalDateTime.now().minusHours(24));

        return challengeRepository.findBySenderIdOrReceiverId(userId, userId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public ChallengeDto sendChallenge(ChallengeRequest req) {
        User sender = userRepository.findById(req.getSenderId())
                .orElseThrow(() -> new RuntimeException("Gönderen bulunamadı"));
        User receiver = userRepository.findById(req.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Alıcı bulunamadı"));
        if (Boolean.TRUE.equals(sender.getVacationMode()))
            throw new RuntimeException("Tatil modundayken challenge gönderemezsiniz");
        if (Boolean.TRUE.equals(receiver.getVacationMode()))
            throw new RuntimeException("Bu kullanıcı tatil modunda, challenge gönderilemiyor");

        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();

        if (challengeRepository.countSentSince(sender.getId(), startOfDay) >= 3)
            throw new RuntimeException("1 günde en fazla 3 challenge gönderebilirsin");

        if (challengeRepository.countSentToReceiverWithDurationSince(
                sender.getId(), receiver.getId(), req.getDuration(), startOfDay) >= 1)
            throw new RuntimeException("Bu sürede bugün zaten bu kişiye challenge gönderdin");

        FocusChallenge c = FocusChallenge.builder()
                .sender(sender).receiver(receiver).duration(req.getDuration())
                .name(req.getName()).status("PENDING").build();
        c = challengeRepository.save(c);
        return toDto(c);
    }

    /** Alıcı "Kabul Et" bastığında → status ACCEPTED olur, 24 saat içinde tamamlanmalı */
    @Transactional
    public void acceptChallenge(Long challengeId, Long userId) {
        FocusChallenge c = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge bulunamadı"));
        if (!c.getReceiver().getId().equals(userId))
            throw new RuntimeException("Bu challenge'ı sadece alıcı kabul edebilir");
        if (!"PENDING".equals(c.getStatus()))
            throw new RuntimeException("Challenge zaten kabul edilmiş veya tamamlanmış");
        c.setStatus("ACCEPTED");
        c.setAcceptedAt(LocalDateTime.now());
        challengeRepository.save(c);
    }

    /**
     * Kullanıcı seansını tamamlayınca çağrılır.
     * İki taraf da tamamlarsa status → COMPLETED ve her ikisine bonus verilir.
     */
    @Transactional
    public void completeChallenge(Long challengeId, Long userId) {
        FocusChallenge c = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge bulunamadı"));

        // Sadece ACCEPTED challenge tamamlanabilir — PENDING iken tamamlatma
        if (!"ACCEPTED".equals(c.getStatus()))
            throw new RuntimeException("Challenge henüz kabul edilmedi veya zaten tamamlandı");

        if (c.getSender().getId().equals(userId)) {
            if (Boolean.TRUE.equals(c.getSenderCompleted())) return; // zaten tamamlandı
            c.setSenderCompleted(Boolean.TRUE);
        } else if (c.getReceiver().getId().equals(userId)) {
            if (Boolean.TRUE.equals(c.getReceiverCompleted())) return; // zaten tamamlandı
            c.setReceiverCompleted(Boolean.TRUE);
        }

        // Her iki taraf da tamamladıysa bonus puan ver ve kapat
        if (Boolean.TRUE.equals(c.getSenderCompleted()) && Boolean.TRUE.equals(c.getReceiverCompleted())) {
            c.setStatus("COMPLETED");
            int bonus = c.getDuration();
            addBonus(c.getSender().getId(), bonus);
            addBonus(c.getReceiver().getId(), bonus);
        }

        challengeRepository.save(c);
    }

    /** Alıcı "Reddet" bastığında → challenge silinir */
    @Transactional
    public void rejectChallenge(Long challengeId, Long userId) {
        FocusChallenge c = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge bulunamadı"));
        if (!c.getReceiver().getId().equals(userId))
            throw new RuntimeException("Bu challenge'ı sadece alıcı reddedebilir");
        if (!"PENDING".equals(c.getStatus()))
            throw new RuntimeException("Kabul edilmiş challenge reddedilemez");
        challengeRepository.delete(c);
    }

    @Transactional
    public void cancelChallenge(Long challengeId, Long userId) {
        FocusChallenge c = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge bulunamadı"));
        if (!c.getSender().getId().equals(userId))
            throw new RuntimeException("Sadece gönderen iptal edebilir");
        if (!"PENDING".equals(c.getStatus()))
            throw new RuntimeException("Kabul edilmiş challenge iptal edilemez");
        challengeRepository.delete(c);
    }

    private void addBonus(Long userId, int bonus) {
        worldRepository.findByUserId(userId).ifPresent(w -> {
            w.addPoints(bonus); worldRepository.save(w);
        });
    }

    private ChallengeDto toDto(FocusChallenge c) {
        return new ChallengeDto(
                c.getId(),
                c.getSender() != null ? c.getSender().getId() : null,
                c.getReceiver() != null ? c.getReceiver().getId() : null,
                c.getSender() != null ? c.getSender().getUsername() : "Silinmiş Kullanıcı",
                c.getReceiver() != null ? c.getReceiver().getUsername() : "Silinmiş Kullanıcı",
                c.getStatus(),
                c.getCreatedAt() != null ? c.getCreatedAt().toString() : null,
                c.getDuration(),
                Boolean.TRUE.equals(c.getSenderCompleted()),
                Boolean.TRUE.equals(c.getReceiverCompleted()),
                c.getName());
    }
}
