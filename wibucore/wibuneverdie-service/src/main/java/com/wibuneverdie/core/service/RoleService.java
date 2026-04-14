package com.wibuneverdie.core.service;

import com.wibuneverdie.core.dto.RoleMenuAssignRequest;
import com.wibuneverdie.core.dto.RoleMenuResponse;
import com.wibuneverdie.core.dto.RoleRequest;
import com.wibuneverdie.core.dto.RoleResponse;
import com.wibuneverdie.core.entity.UaMenu;
import com.wibuneverdie.core.entity.UaRole;
import com.wibuneverdie.core.entity.UaRoleMenuRelation;
import com.wibuneverdie.core.entity.embeddable.UaRoleMenuRelationId;
import com.wibuneverdie.core.repository.UaMenuRepository;
import com.wibuneverdie.core.repository.UaRoleMenuRelationRepository;
import com.wibuneverdie.core.repository.UaRoleRepository;
import com.wibuneverdie.core.repository.UaRoleUserRelationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final UaRoleRepository             roleRepository;
    private final UaMenuRepository             menuRepository;
    private final UaRoleMenuRelationRepository roleMenuRelationRepository;
    private final UaRoleUserRelationRepository roleUserRelationRepository;

    // ------------------------------------------------------------------ //
    //  Role CRUD                                                           //
    // ------------------------------------------------------------------ //

    @Transactional
    public RoleResponse createRole(RoleRequest req, String currentUserUid) {
        if (roleRepository.existsById(req.roleId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "roleId đã tồn tại: " + req.roleId());
        }

        UaRole role = UaRole.builder()
                .roleId(req.roleId())
                .name(req.name())
                .description(req.description())
                .adminRoleYn(req.adminRoleYn() != null ? req.adminRoleYn() : "N")
                .level(req.level() != null ? req.level() : 9999)
                .useYn("Y")
                .regDt(LocalDateTime.now())
                .regUserUid(currentUserUid)
                .build();

        return RoleResponse.fromEntity(roleRepository.save(role));
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll()
                .stream()
                .map(RoleResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public RoleResponse getRole(String roleId) {
        return RoleResponse.fromEntity(findRoleOrThrow(roleId));
    }

    @Transactional
    public RoleResponse updateRole(String roleId, RoleRequest req, String currentUserUid) {
        UaRole role = findRoleOrThrow(roleId);

        if (req.name()        != null) role.setName(req.name());
        if (req.description() != null) role.setDescription(req.description());
        if (req.adminRoleYn() != null) role.setAdminRoleYn(req.adminRoleYn());
        if (req.level()       != null) role.setLevel(req.level());

        role.setLastModDt(LocalDateTime.now());
        role.setLastModUserUid(currentUserUid);

        return RoleResponse.fromEntity(roleRepository.save(role));
    }

    /** Soft-delete: đặt useYn = N */
    @Transactional
    public void deleteRole(String roleId) {
        UaRole role = findRoleOrThrow(roleId);
        role.setUseYn("N");
        role.setLastModDt(LocalDateTime.now());
        roleRepository.save(role);
    }

    // ------------------------------------------------------------------ //
    //  Role ↔ Menu                                                         //
    // ------------------------------------------------------------------ //

    /**
     * Gán (upsert) danh sách menu vào role, kèm cờ phân quyền chi tiết.
     * Nếu quan hệ đã tồn tại thì cập nhật lại permission.
     */
    @Transactional
    public void assignMenus(String roleId, RoleMenuAssignRequest req, String currentUserUid) {
        UaRole role = findRoleOrThrow(roleId);

        for (RoleMenuAssignRequest.PermissionItem item : req.permissions()) {
            menuRepository.findById(item.menuId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Không tìm thấy menu: " + item.menuId()));

            UaRoleMenuRelationId relationId = new UaRoleMenuRelationId(roleId, item.menuId());

            // Lấy bản ghi cũ nếu có để upsert
            UaRoleMenuRelation relation = roleMenuRelationRepository.findById(relationId)
                    .orElseGet(() -> UaRoleMenuRelation.builder()
                            .id(relationId)
                            .role(role)
                            .regDt(OffsetDateTime.now())
                            .regUserUid(currentUserUid)
                            .build());

            relation.setReadYn(item.readYn());
            relation.setWrtYn(item.wrtYn());
            relation.setModYn(item.modYn());
            relation.setDelYn(item.delYn());
            relation.setMngYn(item.mngYn());
            relation.setPntYn(item.pntYn());
            relation.setExcDnYn(item.excDnYn());

            roleMenuRelationRepository.save(relation);
        }
    }

    /** Lấy danh sách menu (kèm permission) đã gán cho role. */
    @Transactional(readOnly = true)
    public List<RoleMenuResponse> getRoleMenus(String roleId) {
        findRoleOrThrow(roleId);

        return roleMenuRelationRepository.findByIdRoleId(roleId)
                .stream()
                .map(rel -> {
                    UaMenu menu = menuRepository.findById(rel.getId().getMenuId())
                            .orElseThrow();
                    return RoleMenuResponse.fromRelationAndMenu(rel, menu);
                })
                .toList();
    }

    /** Xoá 1 menu khỏi role. */
    @Transactional
    public void removeMenu(String roleId, String menuId) {
        UaRoleMenuRelationId id = new UaRoleMenuRelationId(roleId, menuId);
        if (!roleMenuRelationRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Role không có menu: " + menuId);
        }
        roleMenuRelationRepository.deleteById(id);
    }

    // ------------------------------------------------------------------ //
    //  Helper                                                              //
    // ------------------------------------------------------------------ //

    private UaRole findRoleOrThrow(String roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy role: " + roleId));
    }
}
