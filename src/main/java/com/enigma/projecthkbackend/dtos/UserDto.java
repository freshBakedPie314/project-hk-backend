package com.enigma.projecthkbackend.dtos;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Builder
public class UserDto {
    private String id;
    private String email;
    private String username;

    private String avatarUrl;
    private Instant created_at;
}
