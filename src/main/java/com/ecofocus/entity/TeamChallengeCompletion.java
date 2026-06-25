package com.ecofocus.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "team_challenge_completions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"team_challenge_id", "user_id"}))
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TeamChallengeCompletion {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_challenge_id", nullable = false)
    private TeamChallenge teamChallenge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    public void prePersist() { completedAt = LocalDateTime.now(); }
}
