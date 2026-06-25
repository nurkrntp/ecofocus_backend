package com.ecofocus.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Builder.Default
    @Column(name = "avatar_id")
    private Integer avatarId = 1; // 1-13 arası

    @Builder.Default
    @Column(name = "vacation_mode")
    private Boolean vacationMode = false;

    /** Yapılar için harcanan ekip challenge dakikası (TEAM_HOURS satın almalarında düşülür) */
    @Builder.Default
    @Column(name = "team_minutes_spent")
    private int teamMinutesSpent = 0;

    /** Challenge dakikası harcayarak açılan itemlar için harcanan dakika (CHALLENGE_HOURS) */
    @Builder.Default
    @Column(name = "challenge_minutes_spent")
    private int challengeMinutesSpent = 0;

    @PrePersist
    public void prePersist() { createdAt = LocalDateTime.now(); }
}
