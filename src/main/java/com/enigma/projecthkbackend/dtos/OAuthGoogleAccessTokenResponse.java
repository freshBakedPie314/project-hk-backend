package com.enigma.projecthkbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OAuthGoogleAccessTokenResponse {
    public String access_token;
    public String expires_in;
    public String refresh_token;
    public String scope;
    public String token_type;
    public String refresh_token_expires_in;
}
