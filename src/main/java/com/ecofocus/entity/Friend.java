package com.ecofocus.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "friends")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Friend {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "friend_id", nullable = false)
    private User friend;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING";

    @Column(nullable = false)
    @Builder.Default
    private int rejectedCount = 0;
}
