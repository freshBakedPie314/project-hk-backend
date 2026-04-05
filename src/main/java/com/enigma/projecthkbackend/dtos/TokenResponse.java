package com.enigma.projecthkbackend.dtos;

import com.enigma.projecthkbackend.entities.User;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class TokenResponse {
    private String access_token;
    private String refresh_token;
    private String expires_in;
    private UserDto user;
}
