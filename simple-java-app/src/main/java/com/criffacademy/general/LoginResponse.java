package com.criffacademy.general;

import org.springframework.stereotype.Component;

@Component
public class LoginResponse {
    private String jwt;
    private String refreshToken;

    public LoginResponse(String jwt, String refreshToken) {
        this.jwt = jwt;
        this.refreshToken = refreshToken;
    }

    public String getJwt() {
        return jwt;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
