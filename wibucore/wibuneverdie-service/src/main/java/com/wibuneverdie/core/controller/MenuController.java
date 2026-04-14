package com.wibuneverdie.core.controller;

import com.wibuneverdie.core.dto.MenuCreateRequest;
import com.wibuneverdie.core.dto.MenuDto;
import com.wibuneverdie.core.dto.MenuUpdateRequest;
import com.wibuneverdie.core.security.CustomUserDetails;
import com.wibuneverdie.core.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    // ------------------------------------------------------------------ //
    //  Menu CRUD (admin)                                                   //
    // ------------------------------------------------------------------ //

    /**
     * POST /api/menus
     * Tạo menu mới.
     */
    @PostMapping
    public ResponseEntity<MenuDto> createMenu(
            @Valid @RequestBody MenuCreateRequest req,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(menuService.createMenu(req, principal.getUserUid()));
    }

    /**
     * GET /api/menus
     * Lấy toàn bộ danh sách menu (flat, không lọc useYn).
     */
    @GetMapping
    public ResponseEntity<List<MenuDto>> getAllMenus() {
        return ResponseEntity.ok(menuService.getAllMenus());
    }

    /**
     * GET /api/menus/tree
     * Lấy cây menu đang hoạt động (useYn = Y) dưới dạng nested.
     */
    @GetMapping("/tree")
    public ResponseEntity<List<MenuDto>> getMenuTree() {
        return ResponseEntity.ok(menuService.getMenuTree());
    }

    /**
     * PUT /api/menus/{menuId}
     * Cập nhật menu.
     */
    @PutMapping("/{menuId}")
    public ResponseEntity<MenuDto> updateMenu(
            @PathVariable String menuId,
            @RequestBody MenuUpdateRequest req,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(menuService.updateMenu(menuId, req, principal.getUserUid()));
    }

    /**
     * DELETE /api/menus/{menuId}
     * Xoá menu. Trả về 409 nếu menu đang được gán cho role nào đó.
     */
    @DeleteMapping("/{menuId}")
    public ResponseEntity<Void> deleteMenu(@PathVariable String menuId) {
        menuService.deleteMenu(menuId);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------ //
    //  Menu của user đang login                                            //
    // ------------------------------------------------------------------ //

    /**
     * GET /api/menus/my
     * Trả về danh sách menu (flat) mà user đang login có quyền truy cập.
     */
    @GetMapping("/my")
    public ResponseEntity<List<MenuDto>> getMyMenus() {
        return ResponseEntity.ok(menuService.getMenusForCurrentUser());
    }

    /**
     * GET /api/menus/my/tree
     * Trả về cây menu (nested children) mà user đang login có quyền truy cập.
     */
    @GetMapping("/my/tree")
    public ResponseEntity<List<MenuDto>> getMyMenuTree() {
        return ResponseEntity.ok(menuService.getMenuTreeForCurrentUser());
    }
}

