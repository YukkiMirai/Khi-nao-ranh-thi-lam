package com.wibuneverdie.core.dto;

public record UserUpdateRequest(
        String fullName,
        String phone,
        String email,
        /** ACTIVE | INACTIVE | LOCKED */
        String status,
        String type
) {}
