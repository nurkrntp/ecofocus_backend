package com.ecofocus.dto;
import lombok.*;
import java.util.List;
@Data @AllArgsConstructor
public class WorldDto {
    private Long id, userId;
    private Integer mapWidth, mapHeight, totalPoints, totalEarned;
    private List<WorldObjectDto> objects;
}
