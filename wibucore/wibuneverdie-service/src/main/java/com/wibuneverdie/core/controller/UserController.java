package com.wibuneverdie.core.controller;

import com.wibuneverdie.core.dto.*;
import com.wibuneverdie.core.security.CustomUserDetails;
import com.wibuneverdie.core.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ------------------------------------------------------------------ //
    //  User CRUD                                                           //
    // ------------------------------------------------------------------ //

    /**
     * POST /api/users
     * Tạo user mới. Chỉ admin hoặc user có quyền mới được gọi.
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody UserCreateRequest req,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createUser(req, principal.getUserUid()));
    }

    /**
     * GET /api/users
     * Lấy danh sách tất cả user.
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * GET /api/users/{userUid}
     * Lấy thông tin 1 user theo userUid.
     */
    @GetMapping("/{userUid}")
    public ResponseEntity<UserResponse> getUser(@PathVariable String userUid) {
        return ResponseEntity.ok(userService.getUser(userUid));
    }

    /**
     * PUT /api/users/{userUid}
     * Cập nhật thông tin user (fullName, phone, email, status, type).
     */
    @PutMapping("/{userUid}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable String userUid,
            @RequestBody UserUpdateRequest req,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(userService.updateUser(userUid, req, principal.getUserUid()));
    }

    /**
     * DELETE /api/users/{userUid}
     * Soft-delete: đặt status = INACTIVE.
     */
    @DeleteMapping("/{userUid}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userUid) {
        userService.deleteUser(userUid);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------ //
    //  User ↔ Role                                                         //
    // ------------------------------------------------------------------ //

    /**
     * POST /api/users/{userUid}/roles
     * Gán danh sách role vào user.
     * Body: { "roleIds": ["ROLE_ADMIN", "ROLE_DOCTOR"] }
     */
    @PostMapping("/{userUid}/roles")
    public ResponseEntity<Void> assignRoles(
            @PathVariable String userUid,
            @Valid @RequestBody UserRoleAssignRequest req,
            @AuthenticationPrincipal CustomUserDetails principal) {
        userService.assignRoles(userUid, req, principal.getUserUid());
        return ResponseEntity.ok().build();
    }

    /**
     * GET /api/users/{userUid}/roles
     * Lấy danh sách role đã gán cho user.
     */
    @GetMapping("/{userUid}/roles")
    public ResponseEntity<List<RoleResponse>> getUserRoles(@PathVariable String userUid) {
        return ResponseEntity.ok(userService.getUserRoles(userUid));
    }

    /**
     * DELETE /api/users/{userUid}/roles/{roleId}
     * Xoá 1 role khỏi user.
     */
    @DeleteMapping("/{userUid}/roles/{roleId}")
    public ResponseEntity<Void> removeRole(
            @PathVariable String userUid,
            @PathVariable String roleId) {
        userService.removeRole(userUid, roleId);
        return ResponseEntity.noContent().build();
    }
}
