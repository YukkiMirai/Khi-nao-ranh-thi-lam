package com.wibuneverdie.core.dto;

import com.wibuneverdie.core.entity.UaUser;

public record UserResponse(
        String userUid,
        String userId,
        String fullName,
        String phone,
        String email,
        String status,
        String type,
        String authProvider
) {
    public static UserResponse fromEntity(UaUser user) {
        return new UserResponse(
                user.getUserUid(),
                user.getUserId(),
                user.getFullName(),
                user.getPhone(),
                user.getEmail(),
                user.getStatus(),
                user.getType(),
                user.getAuthProvider()
        );
    }
}
