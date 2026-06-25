package com.ecofocus.dto;

import lombok.*;
import java.util.List;

@Data @AllArgsConstructor
public class TeamDto {
    private Long id;
    private String name;
    private Long creatorId;
    private String creatorUsername;
    private List<TeamMemberDto> members;

    @Data @AllArgsConstructor
    public static class TeamMemberDto {
        private Long userId;
        private String username;
        private Integer avatarId;
        private boolean vacationMode;
    }
}
