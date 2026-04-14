package com.wibuneverdie.core.service;

import com.wibuneverdie.core.dto.MenuDto;
import com.wibuneverdie.core.dto.RoleResponse;
import com.wibuneverdie.core.dto.UserProfileResponse;
import com.wibuneverdie.core.dto.UserResponse;
import com.wibuneverdie.core.entity.UaUser;
import com.wibuneverdie.core.repository.UaRoleRepository;
import com.wibuneverdie.core.repository.UaRoleUserRelationRepository;
import com.wibuneverdie.core.repository.UaUserRepository;
import com.wibuneverdie.core.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommonService {

    private final UaUserRepository            userRepository;
    private final UaRoleRepository            roleRepository;
    private final UaRoleUserRelationRepository roleUserRelationRepository;
    private final MenuService                 menuService;

    /**
     * Lấy thông tin đầy đủ của user đang đăng nhập:
     * userInfo, danh sách role, cây menu.
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile() {
        CustomUserDetails principal = resolveCurrentUser();
        String userUid = principal.getUserUid();

        // 1. User info
        UaUser user = userRepository.findById(userUid)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found: " + userUid));
        UserResponse userInfo = UserResponse.fromEntity(user);

        // 2. Roles
        List<String> roleIds = roleUserRelationRepository.findRoleIdsByUserUid(userUid);
        List<RoleResponse> roles = roleIds.isEmpty()
                ? List.of()
                : roleRepository.findAllById(roleIds)
                        .stream()
                        .map(RoleResponse::fromEntity)
                        .toList();

        // 3. Menu tree (reuse MenuService logic)
        List<MenuDto> menus = menuService.getMenuTreeByUserUid(userUid);

        return new UserProfileResponse(userInfo, roles, menus);
    }

    // ------------------------------------------------------------------ //

    private CustomUserDetails resolveCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails details)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Unauthenticated");
        }
        return details;
    }
}
