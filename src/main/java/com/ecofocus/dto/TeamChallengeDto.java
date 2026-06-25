package com.ecofocus.dto;

import lombok.*;
import java.util.List;

@Data @AllArgsConstructor
public class TeamChallengeDto {
    private Long id;
    private Long teamId;
    private String teamName;
    private String challengeName;
    private Integer duration;
    private String status;          // ACTIVE | COMPLETED
    private int completedCount;     // kaç kişi tamamladı
    private int totalCount;         // toplam kaç kişi
    private boolean completedByMe;  // ben tamamladım mı
    private List<String> completedUsernames;
    private String createdAt;
    private String expiresAt;
    private Integer validityDays;
}
