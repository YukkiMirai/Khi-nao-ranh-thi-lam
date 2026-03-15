package com.wibuneverdie.core.dto;

import jakarta.validation.constraints.NotBlank;

public record RoleRequest(
        /** Mã role do admin định nghĩa, vd: ROLE_ADMIN, ROLE_DOCTOR */
        @NotBlank String roleId,
        @NotBlank String name,
        String description,
        /** Y nếu đây là admin role */
        String adminRoleYn,
        /** Cấp độ quyền hạn – số nhỏ hơn = quyền cao hơn */
        Integer level
) {}
