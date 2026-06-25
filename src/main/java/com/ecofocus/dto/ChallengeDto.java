package com.ecofocus.dto;
import lombok.*;
@Data @AllArgsConstructor
public class ChallengeDto {
    private Long id, senderId, receiverId;
    private String senderUsername, receiverUsername, status, createdAt;
    private Integer duration;
    private boolean senderCompleted;
    private boolean receiverCompleted;
    private String name;
}
