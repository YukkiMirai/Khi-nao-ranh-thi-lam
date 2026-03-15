package com.wibuneverdie.core.dto;

import com.wibuneverdie.core.entity.UaMenu;
import com.wibuneverdie.core.entity.UaRoleMenuRelation;

/**
 * Trả về thông tin menu kèm các cờ phân quyền của role đó trên menu.
 */
public record RoleMenuResponse(
        String menuId,
        String menuName,
        String menuNameEn,
        String menuNameVi,
        String linkUri,
        Long   displayOrder,
        String menuType,
        Long   lev,
        String upperMenuId,
        String readYn,
        String wrtYn,
        String modYn,
        String delYn,
        String mngYn,
        String pntYn,
        String excDnYn
) {
    public static RoleMenuResponse fromRelationAndMenu(UaRoleMenuRelation rel, UaMenu menu) {
        return new RoleMenuResponse(
                menu.getMenuId(),
                menu.getMenuName(),
                menu.getMenuNameEn(),
                menu.getMenuNameVi(),
                menu.getLinkUri(),
                menu.getDisplayOrder(),
                menu.getMenuType(),
                menu.getLev(),
                menu.getUpperMenu() != null ? menu.getUpperMenu().getMenuId() : null,
                rel.getReadYn(),
                rel.getWrtYn(),
                rel.getModYn(),
                rel.getDelYn(),
                rel.getMngYn(),
                rel.getPntYn(),
                rel.getExcDnYn()
        );
    }
}
