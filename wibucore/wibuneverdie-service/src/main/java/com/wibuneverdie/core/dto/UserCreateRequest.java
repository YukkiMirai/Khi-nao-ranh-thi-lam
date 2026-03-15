package com.wibuneverdie.core.dto;

import jakarta.validation.constraints.NotBlank;

public record UserCreateRequest(
        @NotBlank String userId,
        @NotBlank String password,
        String fullName,
        String phone,
        String email,
        /** Loại tài khoản: ADMIN, STAFF, DOCTOR, PATIENT, ... */
        String type,
        /** Nhà cung cấp xác thực: LOCAL, GOOGLE, ... */
        String authProvider
) {}
