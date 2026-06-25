package com.ecofocus.dto;

import lombok.Data;

@Data
public class PlaceRequest {
    private Long userId;
    private String itemType;
    private Integer tileX;
    private Integer tileY;
}
