package com.wibuneverdie.core.dto;

public record MenuUpdateRequest(
        String upperMenuId,
        String menuName,
        String menuNameEn,
        String menuNameVi,
        String linkUri,
        Long   displayOrder,
        String menuType,
        String useYn,
        Long   lev,
        String description,
        String remark
) {}
