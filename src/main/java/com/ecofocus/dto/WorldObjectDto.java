package com.ecofocus.dto;
import lombok.*;
@Data @AllArgsConstructor
public class WorldObjectDto {
    private Long id, worldId;
    private String type;
    private Integer tileX, tileY;
}
