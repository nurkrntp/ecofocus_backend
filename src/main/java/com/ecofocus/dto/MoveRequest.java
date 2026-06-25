package com.ecofocus.dto;

import lombok.Data;

@Data
public class MoveRequest {
    private Long userId;
    private Integer tileX;
    private Integer tileY;
}
