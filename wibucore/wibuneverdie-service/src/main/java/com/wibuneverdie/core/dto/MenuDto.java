package com.wibuneverdie.core.dto;

import com.wibuneverdie.core.entity.UaMenu;

import java.util.List;

public record MenuDto(
        String menuId,
        String upperMenuId,
        String menuName,
        String menuNameEn,
        String menuNameVi,
        String linkUri,
        Long   displayOrder,
        String menuType,
        Long   lev,
        String useYn,
        String description,
        List<MenuDto> children
) {
    /** Map entity → DTO (không đệ quy children để tránh N+1 khi dùng flat list) */
    public static MenuDto fromEntity(UaMenu menu) {
        return new MenuDto(
                menu.getMenuId(),
                menu.getUpperMenu() != null ? menu.getUpperMenu().getMenuId() : null,
                menu.getMenuName(),
                menu.getMenuNameEn(),
                menu.getMenuNameVi(),
                menu.getLinkUri(),
                menu.getDisplayOrder(),
                menu.getMenuType(),
                menu.getLev(),
                menu.getUseYn(),
                menu.getDescription(),
                null
        );
    }

    /** Map entity → DTO kèm đệ quy children (dùng khi trả về cây) */
    public static MenuDto fromEntityWithChildren(UaMenu menu) {
        List<MenuDto> childDtos = menu.getChildren() == null ? List.of() :
                menu.getChildren().stream()
                        .map(MenuDto::fromEntityWithChildren)
                        .toList();

        return new MenuDto(
                menu.getMenuId(),
                menu.getUpperMenu() != null ? menu.getUpperMenu().getMenuId() : null,
                menu.getMenuName(),
                menu.getMenuNameEn(),
                menu.getMenuNameVi(),
                menu.getLinkUri(),
                menu.getDisplayOrder(),
                menu.getMenuType(),
                menu.getLev(),
                menu.getUseYn(),
                menu.getDescription(),
                childDtos
        );
    }
}
