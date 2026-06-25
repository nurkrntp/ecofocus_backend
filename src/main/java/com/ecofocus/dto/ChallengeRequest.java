package com.ecofocus.dto;
import lombok.Data;
@Data public class ChallengeRequest {
    private Long senderId, receiverId;
    private Integer duration;
    private String name;
}
