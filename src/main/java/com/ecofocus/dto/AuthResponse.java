package com.ecofocus.dto;
import lombok.*;
@Data @AllArgsConstructor
public class AuthResponse {
    private String token;
    private String refreshToken;
    private UserDto user;
}
