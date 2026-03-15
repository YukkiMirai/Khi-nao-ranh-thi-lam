package com.wibuneverdie.core.dto;

import com.wibuneverdie.core.entity.UaRole;

import java.time.LocalDateTime;

public record RoleResponse(
        String roleId,
        String name,
        String adminRoleYn,
        String description,
        String useYn,
        Integer level,
        LocalDateTime regDt
) {
    public static RoleResponse fromEntity(UaRole role) {
        return new RoleResponse(
                role.getRoleId(),
                role.getName(),
                role.getAdminRoleYn(),
                role.getDescription(),
                role.getUseYn(),
                role.getLevel(),
                role.getRegDt()
        );
    }
}
