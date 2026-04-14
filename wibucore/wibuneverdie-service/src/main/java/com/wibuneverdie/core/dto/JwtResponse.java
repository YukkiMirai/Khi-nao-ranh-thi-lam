package com.wibuneverdie.core.dto;

public record JwtResponse(
        String token,
        String tokenType,
        long   expiresIn,    // milliseconds
        String userId,
        String userUid
) {
    public static JwtResponse of(String token, long expiresIn, String userId, String userUid) {
        return new JwtResponse(token, "Bearer", expiresIn, userId, userUid);
    }
}
