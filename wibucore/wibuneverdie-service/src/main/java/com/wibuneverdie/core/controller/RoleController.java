package com.wibuneverdie.core.controller;

import com.wibuneverdie.core.dto.RoleMenuAssignRequest;
import com.wibuneverdie.core.dto.RoleMenuResponse;
import com.wibuneverdie.core.dto.RoleRequest;
import com.wibuneverdie.core.dto.RoleResponse;
import com.wibuneverdie.core.security.CustomUserDetails;
import com.wibuneverdie.core.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    // ------------------------------------------------------------------ //
    //  Role CRUD                                                           //
    // ------------------------------------------------------------------ //

    /**
     * POST /api/roles
     * Tạo role mới.
     * Body: { "roleId": "ROLE_DOCTOR", "name": "Bác sĩ", "level": 10 }
     */
    @PostMapping
    public ResponseEntity<RoleResponse> createRole(
            @Valid @RequestBody RoleRequest req,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(roleService.createRole(req, principal.getUserUid()));
    }

    /**
     * GET /api/roles
     * Lấy danh sách tất cả role.
     */
    @GetMapping
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    /**
     * GET /api/roles/{roleId}
     * Lấy thông tin 1 role.
     */
    @GetMapping("/{roleId}")
    public ResponseEntity<RoleResponse> getRole(@PathVariable String roleId) {
        return ResponseEntity.ok(roleService.getRole(roleId));
    }

    /**
     * PUT /api/roles/{roleId}
     * Cập nhật role (name, description, adminRoleYn, level).
     */
    @PutMapping("/{roleId}")
    public ResponseEntity<RoleResponse> updateRole(
            @PathVariable String roleId,
            @RequestBody RoleRequest req,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(roleService.updateRole(roleId, req, principal.getUserUid()));
    }

    /**
     * DELETE /api/roles/{roleId}
     * Soft-delete: đặt useYn = N.
     */
    @DeleteMapping("/{roleId}")
    public ResponseEntity<Void> deleteRole(@PathVariable String roleId) {
        roleService.deleteRole(roleId);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------ //
    //  Role ↔ Menu (phân quyền menu)                                       //
    // ------------------------------------------------------------------ //

    /**
     * POST /api/roles/{roleId}/menus
     * Gán (upsert) danh sách menu + cờ phân quyền vào role.
     * Body:
     * <pre>
     * {
     *   "permissions": [
     *     { "menuId": "MENU_DASHBOARD", "readYn": "Y", "wrtYn": "N", "modYn": "N",
     *       "delYn": "N", "mngYn": "N", "pntYn": "N", "excDnYn": "N" }
     *   ]
     * }
     * </pre>
     */
    @PostMapping("/{roleId}/menus")
    public ResponseEntity<Void> assignMenus(
            @PathVariable String roleId,
            @Valid @RequestBody RoleMenuAssignRequest req,
            @AuthenticationPrincipal CustomUserDetails principal) {
        roleService.assignMenus(roleId, req, principal.getUserUid());
        return ResponseEntity.ok().build();
    }

    /**
     * GET /api/roles/{roleId}/menus
     * Lấy danh sách menu (kèm phân quyền) đã gán cho role.
     */
    @GetMapping("/{roleId}/menus")
    public ResponseEntity<List<RoleMenuResponse>> getRoleMenus(@PathVariable String roleId) {
        return ResponseEntity.ok(roleService.getRoleMenus(roleId));
    }

    /**
     * DELETE /api/roles/{roleId}/menus/{menuId}
     * Xoá 1 menu khỏi role.
     */
    @DeleteMapping("/{roleId}/menus/{menuId}")
    public ResponseEntity<Void> removeMenu(
            @PathVariable String roleId,
            @PathVariable String menuId) {
        roleService.removeMenu(roleId, menuId);
        return ResponseEntity.noContent().build();
    }
}
