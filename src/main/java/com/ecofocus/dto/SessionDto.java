package com.ecofocus.dto;
import lombok.*;
@Data @AllArgsConstructor
public class SessionDto {
    private Long id, userId;
    private Integer duration, points;
    private String completedAt;
    private String name;
}
