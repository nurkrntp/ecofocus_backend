package com.ecofocus.dto;
import lombok.*;
@Data @AllArgsConstructor
public class FriendDto {
    private Long id, userId, friendId;
    private String username, status;
    private Integer friendTotalPoints;
    private boolean incoming;
    private Integer avatarId;
    private boolean vacationMode;
}
