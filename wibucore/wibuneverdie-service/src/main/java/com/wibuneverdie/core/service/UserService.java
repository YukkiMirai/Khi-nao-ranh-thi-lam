package com.wibuneverdie.core.service;

import com.wibuneverdie.core.dto.*;
import com.wibuneverdie.core.entity.UaRole;
import com.wibuneverdie.core.entity.UaRoleUserRelation;
import com.wibuneverdie.core.entity.UaUser;
import com.wibuneverdie.core.entity.embeddable.UaRoleUserRelationId;
import com.wibuneverdie.core.repository.UaRoleRepository;
import com.wibuneverdie.core.repository.UaRoleUserRelationRepository;
import com.wibuneverdie.core.repository.UaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UaUserRepository           userRepository;
    private final UaRoleRepository           roleRepository;
    private final UaRoleUserRelationRepository roleUserRelationRepository;
    private final PasswordEncoder            passwordEncoder;

    // ------------------------------------------------------------------ //
    //  User CRUD                                                           //
    // ------------------------------------------------------------------ //

    @Transactional
    public UserResponse createUser(UserCreateRequest req, String currentUserUid) {
        if (userRepository.existsByUserId(req.userId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "userId đã tồn tại: " + req.userId());
        }

        UaUser user = UaUser.builder()
                .userId(req.userId())
                .pwd(passwordEncoder.encode(req.password()))
                .fullName(req.fullName())
                .phone(req.phone())
                .email(req.email())
                .type(req.type() != null ? req.type() : "STAFF")
                .authProvider(req.authProvider() != null ? req.authProvider() : "LOCAL")
                .status("ACTIVE")
                .build();

        return UserResponse.fromEntity(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(String userUid) {
        return UserResponse.fromEntity(findUserOrThrow(userUid));
    }

    @Transactional
    public UserResponse updateUser(String userUid, UserUpdateRequest req, String currentUserUid) {
        UaUser user = findUserOrThrow(userUid);

        if (req.fullName()  != null) user.setFullName(req.fullName());
        if (req.phone()     != null) user.setPhone(req.phone());
        if (req.email()     != null) user.setEmail(req.email());
        if (req.status()    != null) user.setStatus(req.status());
        if (req.type()      != null) user.setType(req.type());

        return UserResponse.fromEntity(userRepository.save(user));
    }

    /** Soft-delete: đặt status = INACTIVE */
    @Transactional
    public void deleteUser(String userUid) {
        UaUser user = findUserOrThrow(userUid);
        user.setStatus("INACTIVE");
        userRepository.save(user);
    }

    // ------------------------------------------------------------------ //
    //  User ↔ Role                                                         //
    // ------------------------------------------------------------------ //

    /**
     * Gán danh sách role vào user.
     * Nếu quan hệ đã tồn tại thì bỏ qua (idempotent).
     */
    @Transactional
    public void assignRoles(String userUid, UserRoleAssignRequest req, String currentUserUid) {
        findUserOrThrow(userUid);

        for (String roleId : req.roleIds()) {
            roleRepository.findById(roleId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Không tìm thấy role: " + roleId));

            UaRoleUserRelationId relationId = new UaRoleUserRelationId(roleId, userUid);
            if (!roleUserRelationRepository.existsById(relationId)) {
                UaRole role = roleRepository.getReferenceById(roleId);
                UaRoleUserRelation relation = UaRoleUserRelation.builder()
                        .id(relationId)
                        .role(role)
                        .regDt(LocalDateTime.now())
                        .regUserUid(currentUserUid)
                        .build();
                roleUserRelationRepository.save(relation);
            }
        }
    }

    /** Lấy danh sách role của user. */
    @Transactional(readOnly = true)
    public List<RoleResponse> getUserRoles(String userUid) {
        findUserOrThrow(userUid);
        return roleUserRelationRepository.findByIdUserUid(userUid)
                .stream()
                .map(rel -> RoleResponse.fromEntity(rel.getRole()))
                .toList();
    }

    /** Xoá 1 role khỏi user. */
    @Transactional
    public void removeRole(String userUid, String roleId) {
        UaRoleUserRelationId id = new UaRoleUserRelationId(roleId, userUid);
        if (!roleUserRelationRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "User không có role: " + roleId);
        }
        roleUserRelationRepository.deleteById(id);
    }

    // ------------------------------------------------------------------ //
    //  Helper                                                              //
    // ------------------------------------------------------------------ //

    private UaUser findUserOrThrow(String userUid) {
        return userRepository.findById(userUid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy user: " + userUid));
    }
}
