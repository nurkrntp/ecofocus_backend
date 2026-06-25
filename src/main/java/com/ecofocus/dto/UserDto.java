package com.ecofocus.dto;
import lombok.*;
import java.time.LocalDateTime;
@Data @AllArgsConstructor
public class UserDto {
    private Long id;
    private String username, email;
    private Integer avatarId;
    private LocalDateTime createdAt;
    private Boolean vacationMode;
}
