package com.ecofocus.dto;
import lombok.Data;
@Data public class SessionRequest {
    private Long userId;
    private Integer duration, points;
    private String name;
}
