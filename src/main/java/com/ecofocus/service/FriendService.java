package com.ecofocus.service;

import com.ecofocus.dto.*;
import com.ecofocus.entity.*;
import com.ecofocus.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendService {
    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    private final WorldRepository worldRepository;

    public List<FriendDto> getFriends(Long userId) {
        List<FriendDto> result = new ArrayList<>();
        // Kullanıcının gönderdiği istekler (incoming = false)
        for (Friend f : friendRepository.findByUserId(userId)) {
            Integer pts = worldRepository.findByUserId(f.getFriend().getId())
                    .map(World::getTotalPoints).orElse(0);
            Integer avatarId = f.getFriend().getAvatarId() != null ? f.getFriend().getAvatarId() : 1;
            boolean vacation = Boolean.TRUE.equals(f.getFriend().getVacationMode());
            result.add(new FriendDto(f.getId(), f.getUser().getId(), f.getFriend().getId(),
                    f.getFriend().getUsername(), f.getStatus(), pts, false, avatarId, vacation));
        }
        // Kullanıcıya gelen istekler (incoming = true)
        for (Friend f : friendRepository.findByFriendId(userId)) {
            Integer pts = worldRepository.findByUserId(f.getUser().getId())
                    .map(World::getTotalPoints).orElse(0);
            Integer avatarId = f.getUser().getAvatarId() != null ? f.getUser().getAvatarId() : 1;
            boolean vacation = Boolean.TRUE.equals(f.getUser().getVacationMode());
            result.add(new FriendDto(f.getId(), userId, f.getUser().getId(),
                    f.getUser().getUsername(), f.getStatus(), pts, true, avatarId, vacation));
        }
        return result;
    }

    @Transactional
    public FriendDto sendRequest(FriendRequest req) {
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        User target = userRepository.findByUsername(req.getTargetUsername())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + req.getTargetUsername()));
        if (user.getId().equals(target.getId()))
            throw new RuntimeException("Kendinize istek gönderemezsiniz");

        // Mevcut kayıt varsa kontrol et
        friendRepository.findByUserIdAndFriendId(user.getId(), target.getId()).ifPresent(existing -> {
            if ("PENDING".equals(existing.getStatus()) || "ACCEPTED".equals(existing.getStatus()))
                throw new RuntimeException("Zaten arkadaşlık isteği mevcut");
            if (existing.getRejectedCount() >= 3)
                throw new RuntimeException("Bu kullanıcıya artık arkadaşlık isteği gönderemezsiniz");
        });

        // Karşı yönde REJECTED kayıt var mı? (hedef daha önce göndermişti, biz reddetmiştik)
        friendRepository.findByUserIdAndFriendId(target.getId(), user.getId()).ifPresent(existing -> {
            if ("PENDING".equals(existing.getStatus()) || "ACCEPTED".equals(existing.getStatus()))
                throw new RuntimeException("Zaten arkadaşlık isteği mevcut");
            if (existing.getRejectedCount() >= 3)
                throw new RuntimeException("Bu kullanıcıya artık arkadaşlık isteği gönderemezsiniz");
        });

        // Eski REJECTED kaydı varsa üzerine yaz, yoksa yeni oluştur
        Friend f = friendRepository.findByUserIdAndFriendId(user.getId(), target.getId())
                .orElseGet(() -> Friend.builder().user(user).friend(target).build());
        f.setStatus("PENDING");
        f = friendRepository.save(f);
        return new FriendDto(f.getId(), user.getId(), target.getId(),
                target.getUsername(), f.getStatus(), 0, false,
                target.getAvatarId() != null ? target.getAvatarId() : 1,
                Boolean.TRUE.equals(target.getVacationMode()));
    }

    @Transactional
    public void cancelRequest(Long friendshipId, Long requestingUserId) {
        Friend f = friendRepository.findById(friendshipId)
                .orElseThrow(() -> new RuntimeException("Arkadaşlık isteği bulunamadı"));
        if (!f.getUser().getId().equals(requestingUserId))
            throw new RuntimeException("Sadece gönderen iptal edebilir");
        if (!"PENDING".equals(f.getStatus()))
            throw new RuntimeException("Sadece bekleyen istekler iptal edilebilir");
        friendRepository.delete(f);
    }

    @Transactional
    public void removeFriend(Long friendshipId, Long requestingUserId) {
        Friend f = friendRepository.findById(friendshipId)
                .orElseThrow(() -> new RuntimeException("Arkadaşlık bulunamadı"));
        if (!f.getUser().getId().equals(requestingUserId) && !f.getFriend().getId().equals(requestingUserId))
            throw new RuntimeException("Yetkisiz");
        if (!"ACCEPTED".equals(f.getStatus()))
            throw new RuntimeException("Sadece kabul edilmiş arkadaşlıklar silinebilir");
        friendRepository.delete(f);
    }

    @Transactional
    public FriendDto respond(Long friendshipId, String accept, Long requestingUserId) {
        Friend f = friendRepository.findById(friendshipId)
                .orElseThrow(() -> new RuntimeException("Arkadaşlık isteği bulunamadı"));
        if (!f.getFriend().getId().equals(requestingUserId))
            throw new RuntimeException("Sadece alıcı yanıt verebilir");
        if (!"PENDING".equals(f.getStatus()))
            throw new RuntimeException("Sadece bekleyen isteklere yanıt verilebilir");

        if ("ACCEPTED".equals(accept)) {
            f.setStatus("ACCEPTED");
        } else {
            f.setRejectedCount(f.getRejectedCount() + 1);
            if (f.getRejectedCount() >= 3) {
                f.setStatus("BLOCKED");
            } else {
                f.setStatus("REJECTED");
            }
        }
        f = friendRepository.save(f);
        return new FriendDto(f.getId(), f.getUser().getId(), f.getFriend().getId(),
                f.getFriend().getUsername(), f.getStatus(), 0, false,
                f.getFriend().getAvatarId() != null ? f.getFriend().getAvatarId() : 1,
                Boolean.TRUE.equals(f.getFriend().getVacationMode()));
    }
}
