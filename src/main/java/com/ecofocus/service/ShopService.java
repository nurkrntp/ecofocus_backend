package com.ecofocus.service;

import com.ecofocus.dto.BuyRequest;
import com.ecofocus.dto.MoveRequest;
import com.ecofocus.dto.PlaceRequest;
import com.ecofocus.dto.ShopItemDto;
import com.ecofocus.dto.WorldDto;
import com.ecofocus.entity.User;
import com.ecofocus.entity.UserItem;
import com.ecofocus.entity.World;
import com.ecofocus.entity.WorldObject;
import com.ecofocus.repository.FocusChallengeRepository;
import com.ecofocus.repository.TeamChallengeCompletionRepository;
import com.ecofocus.repository.UserItemRepository;
import com.ecofocus.repository.UserRepository;
import com.ecofocus.repository.WorldObjectRepository;
import com.ecofocus.repository.WorldRepository;
import com.ecofocus.shop.ShopItemRegistry;
import com.ecofocus.shop.ShopItemRegistry.ShopItemDef;
import com.ecofocus.shop.ShopItemRegistry.UnlockType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final UserRepository userRepository;
    private final WorldRepository worldRepository;
    private final WorldObjectRepository worldObjectRepository;
    private final UserItemRepository userItemRepository;
    private final TeamChallengeCompletionRepository teamCompletionRepository;
    private final FocusChallengeRepository focusChallengeRepository;
    private final WorldService worldService;

    // ── Mağaza listesini döner ────────────────────────────────────────────────
    public List<ShopItemDto> getShopItems(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        World world = worldRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Dünya bulunamadı"));

        int totalPoints = world.getTotalPoints();
        int completedChallenges = userItemRepository.countCompletedChallenges(userId);
        int completedTeamChallenges = teamCompletionRepository.countByUserId(userId);

        // Ekip dakika bakiyesi
        long teamEarnedSec = teamCompletionRepository.sumDurationByUserId(userId);
        int teamMinutesEarned = (int)(teamEarnedSec / 60);
        int teamHoursBalance = Math.max(0, teamMinutesEarned - user.getTeamMinutesSpent());

        // Challenge dakika bakiyesi (tamamlanan arkadaş challengelarından)
        long challengeEarnedSec = focusChallengeRepository.sumCompletedDurationByUserId(userId);
        int challengeMinutesEarned = (int)(challengeEarnedSec / 60);
        int challengeHoursBalance = Math.max(0, challengeMinutesEarned - user.getChallengeMinutesSpent());

        Map<String, Integer> owned = userItemRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(UserItem::getItemType, UserItem::getQuantity));

        return ShopItemRegistry.all().stream()
                .map(def -> toDto(def, totalPoints, completedChallenges, completedTeamChallenges,
                        teamHoursBalance, challengeHoursBalance, owned))
                .collect(Collectors.toList());
    }

    // ── Satın alma — sadece koleksiyona ekler, haritaya KOYMAZ ───────────────
    @Transactional
    public List<ShopItemDto> buy(BuyRequest req) {
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        ShopItemDef def = ShopItemRegistry.find(req.getItemType())
                .orElseThrow(() -> new RuntimeException("Geçersiz item: " + req.getItemType()));

        World world = worldRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Dünya bulunamadı"));

        if (def.unlockType() == UnlockType.POINTS) {
            if (world.getTotalPoints() < def.price()) {
                throw new RuntimeException("Yetersiz puan. Gerekli: " + def.price()
                        + ", Mevcut: " + world.getTotalPoints());
            }
            world.setTotalPoints(Math.max(0, world.getTotalPoints() - def.price()));
            worldRepository.save(world);
        } else if (def.unlockType() == UnlockType.CHALLENGE_HOURS) {
            boolean alreadyOwned = userItemRepository
                    .findByUserIdAndItemType(user.getId(), def.type()).isPresent();
            if (alreadyOwned) throw new RuntimeException("Bu item zaten sende var.");
            long focusEarnedSec = focusChallengeRepository.sumCompletedDurationByUserId(user.getId());
            int focusMinutes = (int)(focusEarnedSec / 60);
            int balance = Math.max(0, focusMinutes - user.getChallengeMinutesSpent());
            if (balance < def.price()) {
                int remaining = def.price() - balance;
                throw new RuntimeException("Yetersiz odak süresi. Gerekli: "
                        + _fmtMinutes(def.price()) + ", Kalan: " + _fmtMinutes(remaining));
            }
            user.setChallengeMinutesSpent(user.getChallengeMinutesSpent() + def.price());
            userRepository.save(user);
        } else if (def.unlockType() == UnlockType.TEAM_HOURS) {
            // Ekip saat harcama sistemi — her yapı ayrı ayrı satın alınır
            boolean alreadyOwned = userItemRepository
                    .findByUserIdAndItemType(user.getId(), def.type()).isPresent();
            if (alreadyOwned) throw new RuntimeException("Bu yapı zaten sende var.");
            long earnedSeconds = teamCompletionRepository.sumDurationByUserId(user.getId());
            int earnedMinutes = (int)(earnedSeconds / 60);
            int balance = Math.max(0, earnedMinutes - user.getTeamMinutesSpent());
            if (balance < def.price()) {
                int remaining = def.price() - balance;
                throw new RuntimeException("Yetersiz ekip süresi. Gerekli: "
                        + _fmtMinutes(def.price()) + ", Kalan: " + _fmtMinutes(remaining));
            }
            user.setTeamMinutesSpent(user.getTeamMinutesSpent() + def.price());
            userRepository.save(user);
        } else {
            int completed = def.unlockType() == UnlockType.TEAM_CHALLENGE
                    ? teamCompletionRepository.countByUserId(user.getId())
                    : userItemRepository.countCompletedChallenges(user.getId());
            if (completed < def.challengeThreshold()) {
                throw new RuntimeException("Yeterli görev yok. Gerekli: "
                        + def.challengeThreshold() + ", Tamamlanan: " + completed);
            }
            boolean alreadyOwned = userItemRepository
                    .findByUserIdAndItemType(user.getId(), def.type()).isPresent();
            if (alreadyOwned) {
                throw new RuntimeException("Bu özel karakter zaten sende var.");
            }
        }

        // Koleksiyona ekle (quantity artır)
        UserItem item = userItemRepository
                .findByUserIdAndItemType(user.getId(), def.type())
                .orElse(UserItem.builder().user(user).itemType(def.type()).quantity(0).build());
        item.setQuantity(item.getQuantity() + 1);
        userItemRepository.save(item);

        // Güncel mağaza listesini döndür (UI'ın puan ve quantity'yi güncellemesi için)
        return getShopItems(user.getId());
    }

    // ── Koleksiyondan haritaya koy ────────────────────────────────────────────
    @Transactional
    public WorldDto place(PlaceRequest req) {
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        // Koleksiyonda var mı?
        UserItem item = userItemRepository
                .findByUserIdAndItemType(user.getId(), req.getItemType())
                .orElseThrow(() -> new RuntimeException("Bu karakter koleksiyonunda yok."));

        if (item.getQuantity() <= 0) {
            throw new RuntimeException("Koleksiyonda bu karakterden kalmadı.");
        }

        World world = worldRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Dünya bulunamadı"));

        // Haritaya ekle
        world.getObjects().add(
                WorldObject.builder()
                        .world(world)
                        .type(req.getItemType())
                        .tileX(req.getTileX())
                        .tileY(req.getTileY())
                        .build()
        );
        worldRepository.save(world);

        // Koleksiyondan düş
        item.setQuantity(item.getQuantity() - 1);
        if (item.getQuantity() == 0) {
            userItemRepository.delete(item);
        } else {
            userItemRepository.save(item);
        }

        return worldService.toDto(world);
    }

    // ── Haritadan sil — koleksiyona geri döner ────────────────────────────────
    @Transactional
    public WorldDto removeFromWorld(Long objectId, Long userId) {
        WorldObject obj = worldObjectRepository.findById(objectId)
                .orElseThrow(() -> new RuntimeException("Nesne bulunamadı."));

        // Güvenlik: nesne bu kullanıcıya mı ait?
        World world = obj.getWorld();
        if (!world.getUser().getId().equals(userId)) {
            throw new RuntimeException("Bu nesne sana ait değil.");
        }

        String itemType = obj.getType();

        // Haritadan sil
        world.getObjects().remove(obj);
        worldObjectRepository.delete(obj);

        // Koleksiyona geri ekle
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        UserItem item = userItemRepository
                .findByUserIdAndItemType(userId, itemType)
                .orElse(UserItem.builder().user(user).itemType(itemType).quantity(0).build());
        item.setQuantity(item.getQuantity() + 1);
        userItemRepository.save(item);

        worldRepository.save(world);
        return worldService.toDto(world);
    }

    // ── Haritadaki nesneyi taşı ───────────────────────────────────────────────
    @Transactional
    public void moveWorldObject(Long objectId, MoveRequest req) {
        WorldObject obj = worldObjectRepository.findById(objectId)
                .orElseThrow(() -> new RuntimeException("Nesne bulunamadı."));

        if (!obj.getWorld().getUser().getId().equals(req.getUserId())) {
            throw new RuntimeException("Bu nesne sana ait değil.");
        }

        obj.setTileX(req.getTileX());
        obj.setTileY(req.getTileY());
        worldObjectRepository.save(obj);
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private static String _fmtMinutes(int minutes) {
        int h = minutes / 60;
        int m = minutes % 60;
        if (h > 0 && m > 0) return h + "sa " + m + "dk";
        if (h > 0) return h + "sa";
        return m + "dk";
    }

    private ShopItemDto toDto(ShopItemDef def, int points, int completedChallenges,
                               int completedTeamChallenges, int teamHoursBalance,
                               int challengeHoursBalance, Map<String, Integer> owned) {
        boolean isOwned = owned.containsKey(def.type());
        int quantity    = owned.getOrDefault(def.type(), 0);

        boolean canUnlock;
        int remaining = 0;

        if (def.unlockType() == UnlockType.POINTS) {
            canUnlock = points >= def.price();
        } else if (def.unlockType() == UnlockType.CHALLENGE_HOURS) {
            remaining = Math.max(0, def.price() - challengeHoursBalance);
            canUnlock = remaining == 0 && !isOwned;
        } else if (def.unlockType() == UnlockType.TEAM_HOURS) {
            remaining = Math.max(0, def.price() - teamHoursBalance);
            canUnlock = remaining == 0 && !isOwned;
        } else if (def.unlockType() == UnlockType.TEAM_CHALLENGE) {
            remaining = Math.max(0, def.challengeThreshold() - completedTeamChallenges);
            canUnlock = remaining == 0;
        } else {
            remaining = Math.max(0, def.challengeThreshold() - completedChallenges);
            canUnlock = remaining == 0;
        }

        return new ShopItemDto(
                def.type(), def.emoji(), def.name(),
                def.unlockType().name(),
                def.price(), def.challengeThreshold(),
                isOwned, quantity, canUnlock, remaining
        );
    }
}
