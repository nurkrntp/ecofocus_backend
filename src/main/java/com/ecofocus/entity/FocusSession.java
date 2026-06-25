package com.ecofocus.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "focus_sessions")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class FocusSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer duration;

    @Column(nullable = false)
    private Integer points;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(length = 200)
    private String name;

    @PrePersist
    public void prePersist() { completedAt = LocalDateTime.now(); }
}
