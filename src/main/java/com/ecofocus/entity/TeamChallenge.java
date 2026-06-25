package com.ecofocus.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "team_challenges")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TeamChallenge {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = true)
    private Team team;

    /** Ekip silinse bile görev adını korumak için */
    @Column(name = "team_name", length = 100)
    private String teamName;

    @Column(nullable = false, length = 200)
    private String challengeName;

    /** Saniye cinsinden süre */
    @Column(nullable = false)
    private Integer duration;

    /** ACTIVE | COMPLETED | EXPIRED */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "validity_days")
    @Builder.Default
    private Integer validityDays = 1;

    /** Challenge oluşturulurken tatilde olmayan aktif üye sayısı — sonradan değişmez */
    @Column(name = "total_members")
    @Builder.Default
    private Integer totalMembers = 0;

    @OneToMany(mappedBy = "teamChallenge", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TeamChallengeCompletion> completions = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() { createdAt = LocalDateTime.now(); }
}
