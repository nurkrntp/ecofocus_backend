package com.ecofocus.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_items",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "item_type"}))
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class UserItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "item_type", nullable = false, length = 50)
    private String itemType;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 1;
}
