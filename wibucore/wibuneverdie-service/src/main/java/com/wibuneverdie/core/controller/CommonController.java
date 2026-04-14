package com.wibuneverdie.core.controller;

import com.wibuneverdie.core.dto.UserProfileResponse;
import com.wibuneverdie.core.service.CommonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Các endpoint dùng chung cho frontend sau khi đăng nhập.
 * Tất cả đều yêu cầu JWT hợp lệ (được bảo vệ bởi SecurityConfig).
 */
@RestController
@RequestMapping("/api/common")
@RequiredArgsConstructor
public class CommonController {

    private final CommonService commonService;

    /**
     * GET /api/common/me
     *
     * Trả về thông tin session đầy đủ của user đang đăng nhập:
     * <ul>
     *   <li><b>userInfo</b> – thông tin cơ bản (userUid, userId, fullName, email …)</li>
     *   <li><b>roles</b>    – danh sách role được gán</li>
     *   <li><b>menus</b>    – cây menu theo role (dùng để render sidebar)</li>
     * </ul>
     *
     * Redux dùng endpoint này để populate store sau khi nhận JWT từ /api/auth/login.
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile() {
        return ResponseEntity.ok(commonService.getMyProfile());
    }
}
