package com.ecofocus.service;

import com.ecofocus.dto.*;
import com.ecofocus.entity.*;
import com.ecofocus.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamChallengeRepository teamChallengeRepository;
    private final TeamChallengeCompletionRepository completionRepository;
    private final UserRepository userRepository;
    private final UserItemRepository userItemRepository;
    private final WorldRepository worldRepository;

    /**
     * Geçmiş için karma sorgu:
     * 1) Kullanıcının hâlâ üye olduğu ekiplerin TÜM COMPLETED görevleri
     *    (tatil nedeniyle katılmadıkları dahil)
     * 2) Kullanıcının ayrıldığı / silinmiş ekiplerde kendi tamamladığı görevler
     */
    public List<TeamChallengeDto> getMyCompletedChallenges(Long userId) {
        // Mevcut ekipler (üye veya kurucu)
        List<Team> currentTeams = teamRepository.findByMemberUserId(userId);
        List<Team> createdTeams = teamRepository.findByCreatorId(userId);
        Set<Long> currentTeamIds = new java.util.HashSet<>();
        currentTeams.forEach(t -> currentTeamIds.add(t.getId()));
        createdTeams.forEach(t -> currentTeamIds.add(t.getId()));

        // 1) Mevcut ekiplerin tüm tamamlanmış görevleri
        List<TeamChallenge> fromCurrentTeams = currentTeamIds.stream()
                .flatMap(teamId -> teamChallengeRepository.findByTeamId(teamId).stream())
                .filter(c -> "COMPLETED".equals(c.getStatus()))
                .collect(Collectors.toList());

        // 2) Kendi tamamladığım görevler (ayrıldığım / silinmiş ekipler için)
        List<TeamChallenge> fromMyCompletions = completionRepository.findChallengesByUserId(userId);

        // Birleştir, tekrarları at
        Set<Long> seen = new java.util.LinkedHashSet<>();
        List<TeamChallenge> merged = new java.util.ArrayList<>();
        for (TeamChallenge c : fromCurrentTeams) {
            if (seen.add(c.getId())) merged.add(c);
        }
        for (TeamChallenge c : fromMyCompletions) {
            if (seen.add(c.getId())) merged.add(c);
        }

        return merged.stream().map(c -> toDto(c, userId)).collect(Collectors.toList());
    }

    /** Kullanıcının üye olduğu tüm ekipleri döner */
    public List<TeamDto> getTeams(Long userId) {
        List<Team> teams = teamRepository.findByMemberUserId(userId);
        // Kurucu da üye sayılsın
        List<Team> created = teamRepository.findByCreatorId(userId);
        created.forEach(t -> { if (!teams.contains(t)) teams.add(t); });
        return teams.stream().map(this::toDto).collect(Collectors.toList());
    }

    /** Ekip oluştur — kurucu otomatik üye olur */
    @Transactional
    public TeamDto createTeam(Long creatorId, String name) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        Team team = Team.builder().name(name).creator(creator).build();
        team = teamRepository.save(team);
        // Kurucuyu üye yap
        TeamMember m = TeamMember.builder().team(team).user(creator).build();
        team.getMembers().add(m);
        team = teamRepository.save(team);
        return toDto(team);
    }

/** Kurucu bir üyeyi ekipten çıkarır */
    @Transactional
    public TeamDto removeMember(Long teamId, Long requesterId, Long memberId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Ekip bulunamadı"));
        if (!team.getCreator().getId().equals(requesterId))
            throw new RuntimeException("Sadece kurucu üye çıkarabilir");
        if (team.getCreator().getId().equals(memberId))
            throw new RuntimeException("Kurucu kendini çıkaramaz");
        team.getMembers().removeIf(m -> m.getUser().getId().equals(memberId));
        return toDto(teamRepository.save(team));
    }

    /** Üye ekipten ayrılır — kurucu ayrılamaz */
    @Transactional
    public void leaveTeam(Long teamId, Long userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Ekip bulunamadı"));
        if (team.getCreator().getId().equals(userId))
            throw new RuntimeException("Ekip kurucusu ayrılamaz, ekibi silebilirsin");
        team.getMembers().removeIf(m -> m.getUser().getId().equals(userId));
        teamRepository.save(team);
    }

    /** Ekibi sil — sadece kurucu yapabilir */
    @Transactional
    public void deleteTeam(Long teamId, Long userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Ekip bulunamadı"));
        if (!team.getCreator().getId().equals(userId))
            throw new RuntimeException("Sadece kurucu ekibi silebilir");
        List<TeamChallenge> challenges = teamChallengeRepository.findByTeamId(teamId);
        for (TeamChallenge c : challenges) {
            if ("COMPLETED".equals(c.getStatus())) {
                // Tamamlanmış görevleri koru — ekip adını sakla, bağlantıyı kopar
                if (c.getTeamName() == null) c.setTeamName(team.getName());
                c.setTeam(null);
                teamChallengeRepository.save(c);
            } else {
                // Tamamlanmamış görevleri completion'larıyla sil
                completionRepository.deleteByTeamChallengeId(c.getId());
                teamChallengeRepository.delete(c);
            }
        }
        // FK güncellemelerinin DB'ye yazılmasını garanti et, sonra ekibi sil
        teamChallengeRepository.flush();
        teamRepository.delete(team);
    }

    /** Ekibe arkadaş ekle (kullanıcı adıyla) */
    @Transactional
    public TeamDto addMember(Long teamId, Long requesterId, String username) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Ekip bulunamadı"));
        if (!team.getCreator().getId().equals(requesterId))
            throw new RuntimeException("Sadece kurucu üye ekleyebilir");
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + username));
        if (team.getMembers().size() >= 7)
            throw new RuntimeException("Ekip en fazla 7 üye içerebilir");
        boolean alreadyMember = team.getMembers().stream()
                .anyMatch(m -> m.getUser().getId().equals(user.getId()));
        if (alreadyMember) throw new RuntimeException("Zaten ekip üyesi");
        TeamMember member = TeamMember.builder().team(team).user(user).build();
        team.getMembers().add(member);
        return toDto(teamRepository.save(team));
    }

    /** Ekip challenge'ı oluştur */
    @Transactional
    public TeamChallengeDto createChallenge(Long teamId, Long userId, String challengeName, int duration, int validityDays) {
        if (validityDays < 1 || validityDays > 7) throw new RuntimeException("Geçerlilik süresi 1-7 gün arasında olmalı");
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Ekip bulunamadı"));
        boolean isMember = team.getCreator().getId().equals(userId) ||
                team.getMembers().stream().anyMatch(m -> m.getUser().getId().equals(userId));
        if (!isMember) throw new RuntimeException("Ekip üyesi değilsiniz");
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        if (Boolean.TRUE.equals(creator.getVacationMode()))
            throw new RuntimeException("Tatil modundayken görev oluşturamazsınız");
        // Oluşturulurken tatilde olmayan üye sayısını kaydet — sonradan değişmez
        int activeMembers = (int) team.getMembers().stream()
                .filter(m -> !Boolean.TRUE.equals(m.getUser().getVacationMode()))
                .count();
        // Kurucu üye listesinde yoksa ve tatilde değilse +1
        boolean creatorInMembers = team.getMembers().stream()
                .anyMatch(m -> m.getUser().getId().equals(userId));
        if (!creatorInMembers) activeMembers += 1;
        if (activeMembers < 2) throw new RuntimeException("Görev oluşturmak için ekipte en az 2 aktif üye olmalı");

        TeamChallenge challenge = TeamChallenge.builder()
                .team(team).teamName(team.getName()).challengeName(challengeName).duration(duration)
                .expiresAt(LocalDateTime.now().plusDays(validityDays))
                .validityDays(validityDays)
                .totalMembers(activeMembers).build();
        challenge = teamChallengeRepository.save(challenge);
        return toDto(challenge, userId);
    }

    /** Ekip challenge'ını sil — tamamlanmamışsa herhangi bir üye silebilir */
    @Transactional
    public void deleteTeamChallenge(Long challengeId, Long userId) {
        TeamChallenge challenge = teamChallengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge bulunamadı"));
        boolean isMember = challenge.getTeam().getCreator().getId().equals(userId) ||
                challenge.getTeam().getMembers().stream().anyMatch(m -> m.getUser().getId().equals(userId));
        if (!isMember) throw new RuntimeException("Ekip üyesi değilsiniz");
        User deleter = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        if (Boolean.TRUE.equals(deleter.getVacationMode()))
            throw new RuntimeException("Tatil modundayken görev silemezsiniz");
        if ("COMPLETED".equals(challenge.getStatus()))
            throw new RuntimeException("Tamamlanmış challenge silinemez");
        teamChallengeRepository.delete(challenge);
    }

/** Ekibin challenge listesi */
    @Transactional
    public List<TeamChallengeDto> getChallenges(Long teamId, Long userId) {
        return teamChallengeRepository.findByTeamId(teamId).stream()
                .map(c -> {
                    if ("ACTIVE".equals(c.getStatus()) && c.getExpiresAt() != null
                            && LocalDateTime.now().isAfter(c.getExpiresAt())) {
                        c.setStatus("EXPIRED");
                        teamChallengeRepository.save(c);
                        _applyExpiryPenalties(c);
                    }
                    return toDto(c, userId);
                })
                .collect(Collectors.toList());
    }

    /** Kullanıcı challenge'ı tamamladı */
    @Transactional
    public TeamChallengeDto completeChallenge(Long challengeId, Long userId) {
        TeamChallenge challenge = teamChallengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge bulunamadı"));

        // Zaten tamamladıysa tekrar kaydetme
        if (completionRepository.findByTeamChallengeIdAndUserId(challengeId, userId).isPresent())
            return toDto(challenge, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        if (Boolean.TRUE.equals(user.getVacationMode()))
            throw new RuntimeException("Tatil modundayken ekip görevi tamamlayamazsınız");

        TeamChallengeCompletion completion = TeamChallengeCompletion.builder()
                .teamChallenge(challenge).user(user).build();
        completionRepository.save(completion);

        int totalMembers = challenge.getTotalMembers() != null ? challenge.getTotalMembers() : 1;
        int completedCount = completionRepository.countByTeamChallengeId(challengeId);

        if (completedCount >= totalMembers) {
            challenge.setStatus("COMPLETED");
            teamChallengeRepository.save(challenge);
            // Tüm üyeler tamamladı → görevi yapan herkese puan ver (1 dakika = 1 puan)
            challenge.getCompletions().forEach(cp ->
                worldRepository.findByUserId(cp.getUser().getId()).ifPresent(w -> {
                    w.addPoints(challenge.getDuration());
                    worldRepository.save(w);
                })
            );
        }

        return toDto(challenge, userId);
    }


    /** Süresi dolan görevde tamamlamayan tatil-dışı üyelerin puanından ceza keser */
    private void _applyExpiryPenalties(TeamChallenge challenge) {
        int penalty = Math.max(1, challenge.getDuration() / 5);
        Set<Long> completedIds = challenge.getCompletions().stream()
                .map(c -> c.getUser().getId())
                .collect(Collectors.toSet());

        // Üyeler
        challenge.getTeam().getMembers().stream()
                .filter(m -> !Boolean.TRUE.equals(m.getUser().getVacationMode()))
                .filter(m -> !completedIds.contains(m.getUser().getId()))
                .forEach(m -> worldRepository.findByUserId(m.getUser().getId()).ifPresent(world -> {
                    world.setTotalPoints(Math.max(0, world.getTotalPoints() - penalty));
                    worldRepository.save(world);
                }));

        // Kurucu (üye listesinde yoksa)
        Long creatorId = challenge.getTeam().getCreator().getId();
        boolean creatorInMembers = challenge.getTeam().getMembers().stream()
                .anyMatch(m -> m.getUser().getId().equals(creatorId));
        if (!creatorInMembers
                && !Boolean.TRUE.equals(challenge.getTeam().getCreator().getVacationMode())
                && !completedIds.contains(creatorId)) {
            worldRepository.findByUserId(creatorId).ifPresent(world -> {
                world.setTotalPoints(Math.max(0, world.getTotalPoints() - penalty));
                worldRepository.save(world);
            });
        }
    }

    private TeamDto toDto(Team team) {
        List<TeamDto.TeamMemberDto> members = team.getMembers().stream()
                .map(m -> new TeamDto.TeamMemberDto(
                        m.getUser().getId(),
                        m.getUser().getUsername(),
                        m.getUser().getAvatarId() != null ? m.getUser().getAvatarId() : 1,
                        Boolean.TRUE.equals(m.getUser().getVacationMode())))
                .collect(Collectors.toList());
        return new TeamDto(team.getId(), team.getName(),
                team.getCreator().getId(), team.getCreator().getUsername(), members);
    }

    private TeamChallengeDto toDto(TeamChallenge c, Long userId) {
        List<String> completedUsernames = c.getCompletions().stream()
                .map(cp -> cp.getUser().getUsername()).collect(Collectors.toList());
        boolean completedByMe = c.getCompletions().stream()
                .anyMatch(cp -> cp.getUser().getId().equals(userId));
        int total = c.getTotalMembers() != null ? c.getTotalMembers() : 1;
        String createdAt = c.getCreatedAt() != null
                ? c.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
        String expiresAt = c.getExpiresAt() != null
                ? c.getExpiresAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
        Long teamId = c.getTeam() != null ? c.getTeam().getId() : null;
        String teamName = c.getTeam() != null ? c.getTeam().getName()
                : (c.getTeamName() != null ? c.getTeamName() : "Silinmiş Ekip");
        return new TeamChallengeDto(c.getId(), teamId, teamName,
                c.getChallengeName(), c.getDuration(), c.getStatus(),
                c.getCompletions().size(), total, completedByMe, completedUsernames, createdAt, expiresAt,
                c.getValidityDays());
    }
}
