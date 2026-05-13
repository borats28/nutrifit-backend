package com.nutrifit.payload.response;

import lombok.Data;

import java.util.List;

@Data
public class JwtResponse {
    private String token;
    private String type = "Bearer"; // token türü
    private Long id;
    private String username;
    private String email;

    private List<String> permissions;

    // Constructor'ı güncelle
    public JwtResponse(String accessToken, Long id, String username, String email, List<String> permissions) {
        this.token = accessToken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.permissions = permissions;
    }
}
