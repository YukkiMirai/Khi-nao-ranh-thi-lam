package com.wibuneverdie.core.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request body để gán danh sách menu (kèm phân quyền chi tiết) vào một role.
 * Gửi lên dưới dạng:
 * <pre>
 * {
 *   "permissions": [
 *     { "menuId": "MENU_DASHBOARD", "readYn": "Y", "wrtYn": "N", ... },
 *     ...
 *   ]
 * }
 * </pre>
 */
public record RoleMenuAssignRequest(
        @NotEmpty @Valid List<PermissionItem> permissions
) {
    public record PermissionItem(
            @NotBlank String menuId,
            String readYn,
            String wrtYn,
            String modYn,
            String delYn,
            String mngYn,
            String pntYn,
            String excDnYn
    ) {}
}
