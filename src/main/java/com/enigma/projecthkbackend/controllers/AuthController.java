package com.enigma.projecthkbackend.controllers;

import com.enigma.projecthkbackend.dtos.TokenResponse;
import com.enigma.projecthkbackend.dtos.UserDto;
import com.enigma.projecthkbackend.entities.User;
import com.enigma.projecthkbackend.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("auth/google")
public class AuthController {

    private AuthService authService;
    AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/token")
    public ResponseEntity<Map<String, String>> token(@RequestParam String authCode)
    {
        return authService.getToken(authCode);
    }

    @GetMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestParam String authCode)
    {
        return authService.login(authCode);
    }

    @GetMapping("/refresh")
    public ResponseEntity<TokenResponse> refershToken(@RequestParam String refreshToken)
    {
        return authService.jwtRefreshToken(refreshToken);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getMe() {
        // no need to pass token — Spring Security has it
        User user = (User) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return ResponseEntity.ok(
                UserDto.builder()
                        .username(user.getDisplayName().toString())
                        .email(user.getEmail().toString())
                        .id(user.getId().toString())
                        .avatarUrl(user.getAvatarUrl())
                        .created_at(user.getCreatedAt())
                        .build()
        );
    }
}
