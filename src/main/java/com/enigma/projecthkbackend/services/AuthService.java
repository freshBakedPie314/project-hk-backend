package com.enigma.projecthkbackend.services;

import com.enigma.projecthkbackend.dtos.OAuthGoogleAccessTokenResponse;
import com.enigma.projecthkbackend.dtos.TokenResponse;
import com.enigma.projecthkbackend.dtos.UserDto;
import com.enigma.projecthkbackend.entities.User;
import com.enigma.projecthkbackend.repos.UserRepository;
import com.enigma.projecthkbackend.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    @Value("${google.client-id}")
    private String CLIENT_ID;

    @Value("${google.client-secret}")
    private String CLIENT_SECRET;

    private UserRepository userRepository;

    private JwtUtil jwtUtil;

    AuthService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public ResponseEntity<Map<String, String>> getToken(String authCode) {
        RestClient restClient = RestClient.create();

        MultiValueMap<String, String> reqeust = new LinkedMultiValueMap<>();
        reqeust.add("client_id", CLIENT_ID);
        reqeust.add("client_secret", CLIENT_SECRET);
        reqeust.add("code", authCode);
        reqeust.add("grant_type", "authorization_code");
        reqeust.add("redirect_uri", "https://developers.google.com/oauthplayground");

        OAuthGoogleAccessTokenResponse response = restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(reqeust)
                .retrieve()
                .body(OAuthGoogleAccessTokenResponse.class);

        Map<String, String> returnEntity = new HashMap<>();
        returnEntity.put("access-token", response.getAccess_token());
        returnEntity.put("refresh-token", response.getRefresh_token());
        return ResponseEntity.ok().body(returnEntity);
    }

    public ResponseEntity<Map<String, String>> refreshToken(String authCode) {
        RestClient restClient = RestClient.create();

        MultiValueMap<String, String> reqeust = new LinkedMultiValueMap<>();
        reqeust.add("client_id", CLIENT_ID);
        reqeust.add("client_secret", CLIENT_SECRET);
        reqeust.add("grant_type", "refresh_token");
        reqeust.add("refresh_token", authCode);

        OAuthGoogleAccessTokenResponse response = restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(reqeust)
                .retrieve()
                .body(OAuthGoogleAccessTokenResponse.class);

        Map<String, String> returnEntity = new HashMap<>();
        returnEntity.put("access-token", response.getAccess_token());
        returnEntity.put("refresh-token", response.getRefresh_token());
        return ResponseEntity.ok().body(returnEntity);
    }

    public ResponseEntity<Map<String, Object>> getUserInfo(String accessToken) {
        RestClient restClient = RestClient.create();

        Map<String, Object> response = restClient.get()
                .uri("https://www.googleapis.com/oauth2/v3/userinfo")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {
                });

        return ResponseEntity.ok().body(response);
    }

    @Transactional
    public ResponseEntity<TokenResponse> login(String authCode) {
        ResponseEntity<Map<String, String>> response = getToken(authCode);

        ResponseEntity<Map<String, Object>> userInfo = getUserInfo(response.getBody().get("access-token"));
        if(userRepository.findByEmail(userInfo.getBody().get("email").toString()).isEmpty()) {
            User newUser = new User();

            newUser.setEmail(userInfo.getBody().get("email").toString());
            newUser.setAvatarUrl(userInfo.getBody().get("picture").toString());
            newUser.setDisplayName(userInfo.getBody().get("name").toString());

            userRepository.save(newUser);
        }

        Optional<User> user = userRepository.findByEmail(userInfo.getBody().get("email").toString());
        TokenResponse tokenResponse = TokenResponse.builder()
                        .access_token(jwtUtil.generateToken(user.get()))
                        .refresh_token(jwtUtil.generetRefreshToken(user.get()))
                .expires_in((jwtUtil.getACCESS_EXPIRY()*1000l)+"") // milliSeconds
                .user(UserDto.builder()
                        .id(user.get().getId().toString())
                        .username(user.get().getDisplayName().toString())
                        .email(user.get().getEmail().toString())
                        .avatarUrl(user.get().getAvatarUrl().toString())
                        .created_at(user.get().getCreatedAt())
                        .build()
                )
                .build();

        return ResponseEntity.ok().body(tokenResponse);
    }

    public ResponseEntity<TokenResponse> jwtRefreshToken(String refreshToken)
    {
        Claims claims = jwtUtil.validateAndExtract(refreshToken);

        if(!jwtUtil.isRefreshToken(claims))
        {
            throw new RuntimeException("Invalid refresh token");
        }

        String email = claims.get("email").toString();
        Optional<User> user = userRepository.findByEmail(email);

        TokenResponse tokenResponse = TokenResponse.builder()
                .access_token(jwtUtil.generateToken(user.get()))
                .refresh_token(refreshToken)
                .expires_in((jwtUtil.getACCESS_EXPIRY()*1000l)+"") // milliSeconds
                .user(UserDto.builder()
                        .id(user.get().getId().toString())
                        .username(user.get().getDisplayName().toString())
                        .email(user.get().getEmail().toString())
                        .avatarUrl(user.get().getAvatarUrl().toString())
                        .created_at(user.get().getCreatedAt())
                        .build()
                )
                .build();

        return  ResponseEntity.ok().body(tokenResponse);
    }

}
