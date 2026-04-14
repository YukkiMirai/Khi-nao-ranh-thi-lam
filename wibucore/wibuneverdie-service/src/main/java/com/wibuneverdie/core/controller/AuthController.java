package com.wibuneverdie.core.controller;

import com.wibuneverdie.core.dto.JwtResponse;
import com.wibuneverdie.core.dto.LoginRequest;
import com.wibuneverdie.core.dto.UserCreateRequest;
import com.wibuneverdie.core.dto.UserResponse;
import com.wibuneverdie.core.security.CustomUserDetails;
import com.wibuneverdie.core.security.JwtUtils;
import com.wibuneverdie.core.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils              jwtUtils;
    private final UserService userService;

    /**
     * POST /api/auth/login
     * Body: { "userId": "...", "password": "..." }
     * Response: { "token": "...", "tokenType": "Bearer", "expiresIn": 86400000,
     *             "userId": "...", "userUid": "..." }
     */
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.userId(), request.password())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String token = jwtUtils.generateToken(userDetails.getUsername(), userDetails.getUserUid());

        return ResponseEntity.ok(
                JwtResponse.of(token, jwtUtils.getExpirationMs(),
                               userDetails.getUsername(), userDetails.getUserUid())
        );
    }

    /**
     * POST /api/users/init
     * Khởi tạo user hệ thống đầu tiên (thường là ADMIN).
     * API này được cấu hình permitAll() trong SecurityConfig.
     */
    @PostMapping("/init") // Nên đổi đường dẫn để tránh nhầm lẫn với API tạo user thông thường
    public ResponseEntity<UserResponse> initUser(
            @Valid @RequestBody UserCreateRequest req) {

        // Vì là API khởi tạo public, chúng ta gán một UID mặc định cho người tạo
        // Hoặc truyền "SYSTEM" để đánh dấu đây là bản ghi khởi tạo.
        String systemCreatorUid = "SYSTEM_INITIALIZER";

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createUser(req, systemCreatorUid));
    }
}
