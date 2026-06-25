package com.ecofocus.dto;

import lombok.Data;

@Data
public class BuyRequest {
    private Long userId;
    private String itemType;
}
