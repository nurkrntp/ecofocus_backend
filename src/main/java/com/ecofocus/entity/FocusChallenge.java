package com.ecofocus.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "focus_challenges")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class FocusChallenge {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = true)
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = true)
    private User receiver;

    @Column(nullable = false)
    private Integer duration;

    @Column(length = 100)
    private String name;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING";

    /** Gönderenin seansını tamamlayıp tamamlamadığı */
    @Builder.Default
    @Column(name = "sender_completed")
    private Boolean senderCompleted = Boolean.FALSE;

    /** Alıcının seansını tamamlayıp tamamlamadığı */
    @Builder.Default
    @Column(name = "receiver_completed")
    private Boolean receiverCompleted = Boolean.FALSE;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @PrePersist
    public void prePersist() { createdAt = LocalDateTime.now(); }
}
