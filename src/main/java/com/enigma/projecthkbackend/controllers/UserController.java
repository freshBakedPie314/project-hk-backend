package com.enigma.projecthkbackend.controllers;

import com.enigma.projecthkbackend.dtos.UserDto;
import com.enigma.projecthkbackend.entities.User;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<UserDto> me()
    {
        User user = (User) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        UserDto userDto = UserDto.builder()
                .id(user.getId().toString())
                .email(user.getEmail())
                .username(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .created_at(user.getCreatedAt())
                .build();

        return ResponseEntity.ok(userDto);
    }
}
