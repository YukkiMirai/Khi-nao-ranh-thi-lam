package com.wibuneverdie.core.dto;

import java.util.List;

/**
 * Aggregate response cho /api/common/me.
 * Trả về toàn bộ thông tin cần thiết để Redux frontend khởi tạo session:
 *  - userInfo : thông tin cơ bản của user
 *  - roles    : danh sách role được gán
 *  - menus    : cây menu (tree) tương ứng với các role
 */
public record UserProfileResponse(
        UserResponse      userInfo,
        List<RoleResponse> roles,
        List<MenuDto>      menus
) {}
