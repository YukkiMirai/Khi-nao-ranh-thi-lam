package com.wibuneverdie.core.dto;

import jakarta.validation.constraints.NotBlank;

public record MenuCreateRequest(
        @NotBlank String menuId,
        /** Null nếu là menu gốc (root) */
        String upperMenuId,
        @NotBlank String menuName,
        String menuNameEn,
        String menuNameVi,
        String linkUri,
        Long   displayOrder,
        String menuType,
        Long   lev,
        String description,
        String remark
) {}
