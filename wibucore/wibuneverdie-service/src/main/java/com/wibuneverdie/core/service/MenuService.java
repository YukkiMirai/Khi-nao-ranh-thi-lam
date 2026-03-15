package com.wibuneverdie.core.service;

import com.wibuneverdie.core.dto.MenuCreateRequest;
import com.wibuneverdie.core.dto.MenuDto;
import com.wibuneverdie.core.dto.MenuUpdateRequest;
import com.wibuneverdie.core.entity.UaMenu;
import com.wibuneverdie.core.repository.UaMenuRepository;
import com.wibuneverdie.core.repository.UaRoleMenuRelationRepository;
import com.wibuneverdie.core.repository.UaRoleUserRelationRepository;
import com.wibuneverdie.core.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final UaMenuRepository             uaMenuRepository;
    private final UaRoleUserRelationRepository roleUserRelationRepository;
    private final UaRoleMenuRelationRepository roleMenuRelationRepository;

    // ------------------------------------------------------------------ //
    //  Public API                                                          //
    // ------------------------------------------------------------------ //

    /**
     * Lấy danh sách menu (flat) dựa trên role của user đang login.
     * Lấy userUid từ SecurityContext → tra role → tra menu.
     */
    @Transactional(readOnly = true)
    public List<MenuDto> getMenusForCurrentUser() {
        String userUid = resolveCurrentUserUid();
        return getMenusByUserUid(userUid);
    }

    /**
     * Lấy danh sách menu (flat) theo userUid cụ thể.
     * Dùng khi cần tra menu cho user khác (admin use-case).
     */
    @Transactional(readOnly = true)
    public List<MenuDto> getMenusByUserUid(String userUid) {
        List<String> roleIds = roleUserRelationRepository.findRoleIdsByUserUid(userUid);

        if (roleIds.isEmpty()) {
            return List.of();
        }

        return uaMenuRepository.findMenusByRoleIds(roleIds)
                .stream()
                .map(MenuDto::fromEntity)
                .toList();
    }

    /**
     * Lấy cây menu (có children) dựa trên role của user đang login.
     * Chỉ trả về các node ROOT; children được nested bên trong.
     */
    @Transactional(readOnly = true)
    public List<MenuDto> getMenuTreeForCurrentUser() {
        String userUid = resolveCurrentUserUid();
        return getMenuTreeByUserUid(userUid);
    }

    /**
     * Lấy cây menu (có children) theo userUid.
     * Thuật toán: lấy flat list → build tree trong memory để tránh N+1 query.
     */
    @Transactional(readOnly = true)
    public List<MenuDto> getMenuTreeByUserUid(String userUid) {
        List<String> roleIds = roleUserRelationRepository.findRoleIdsByUserUid(userUid);

        if (roleIds.isEmpty()) {
            return List.of();
        }

        List<MenuDto> flatList = uaMenuRepository.findMenusByRoleIds(roleIds)
                .stream()
                .map(MenuDto::fromEntity)
                .toList();

        return buildTree(flatList);
    }

    // ------------------------------------------------------------------ //
    //  Internal helpers                                                    //
    // ------------------------------------------------------------------ //

    /** Lấy userUid của user đang đăng nhập từ SecurityContext. */
    private String resolveCurrentUserUid() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails details)) {
            throw new IllegalStateException("No authenticated user found in SecurityContext");
        }
        return details.getUserUid();
    }

    /**
     * Xây dựng cây menu từ flat list theo quan hệ upperMenuId → menuId.
     * Trả về danh sách các node ROOT (upperMenuId == null).
     */
    private List<MenuDto> buildTree(List<MenuDto> flatList) {
        // Map menuId → List<children>
        java.util.Map<String, List<MenuDto>> childrenMap = new java.util.HashMap<>();
        for (MenuDto dto : flatList) {
            if (dto.upperMenuId() != null) {
                childrenMap.computeIfAbsent(dto.upperMenuId(), k -> new java.util.ArrayList<>())
                           .add(dto);
            }
        }

        // Rebuild DTOs với children populated
        java.util.Map<String, MenuDto> dtoWithChildren = new java.util.LinkedHashMap<>();
        for (MenuDto dto : flatList) {
            dtoWithChildren.put(dto.menuId(),
                    new MenuDto(dto.menuId(), dto.upperMenuId(), dto.menuName(),
                                dto.menuNameEn(), dto.menuNameVi(), dto.linkUri(),
                                dto.displayOrder(), dto.menuType(), dto.lev(),
                                dto.useYn(), dto.description(),
                                childrenMap.getOrDefault(dto.menuId(), List.of())));
        }

        // Chỉ trả về root nodes
        return dtoWithChildren.values().stream()
                .filter(d -> d.upperMenuId() == null)
                .toList();
    }

    // ------------------------------------------------------------------ //
    //  Menu CRUD (admin)                                                   //
    // ------------------------------------------------------------------ //

    /** Lấy toàn bộ menu (flat, không lọc useYn). */
    @Transactional(readOnly = true)
    public List<MenuDto> getAllMenus() {
        return uaMenuRepository.findAll()
                .stream()
                .map(MenuDto::fromEntity)
                .toList();
    }

    /** Lấy cây menu đang hoạt động (useYn = Y) cho tất cả user. */
    @Transactional(readOnly = true)
    public List<MenuDto> getMenuTree() {
        List<MenuDto> flatList = uaMenuRepository.findByUseYnOrderByDisplayOrderAsc("Y")
                .stream()
                .map(MenuDto::fromEntity)
                .toList();
        return buildTree(flatList);
    }

    /** Tạo menu mới. */
    @Transactional
    public MenuDto createMenu(MenuCreateRequest req, String currentUserUid) {
        if (uaMenuRepository.existsById(req.menuId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "menuId đã tồn tại: " + req.menuId());
        }

        UaMenu upperMenu = null;
        if (req.upperMenuId() != null) {
            upperMenu = uaMenuRepository.findById(req.upperMenuId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Không tìm thấy menu cha: " + req.upperMenuId()));
        }

        UaMenu menu = UaMenu.builder()
                .menuId(req.menuId())
                .upperMenu(upperMenu)
                .menuName(req.menuName())
                .menuNameEn(req.menuNameEn())
                .menuNameVi(req.menuNameVi())
                .linkUri(req.linkUri())
                .displayOrder(req.displayOrder())
                .menuType(req.menuType())
                .lev(req.lev())
                .description(req.description())
                .remark(req.remark())
                .useYn("Y")
                .regDt(OffsetDateTime.now())
                .regUserUid(currentUserUid)
                .build();

        return MenuDto.fromEntity(uaMenuRepository.save(menu));
    }

    /** Cập nhật menu. */
    @Transactional
    public MenuDto updateMenu(String menuId, MenuUpdateRequest req, String currentUserUid) {
        UaMenu menu = uaMenuRepository.findById(menuId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy menu: " + menuId));

        if (req.upperMenuId() != null) {
            if (req.upperMenuId().isBlank()) {
                menu.setUpperMenu(null);
            } else {
                UaMenu parent = uaMenuRepository.findById(req.upperMenuId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Không tìm thấy menu cha: " + req.upperMenuId()));
                menu.setUpperMenu(parent);
            }
        }
        if (req.menuName()     != null) menu.setMenuName(req.menuName());
        if (req.menuNameEn()   != null) menu.setMenuNameEn(req.menuNameEn());
        if (req.menuNameVi()   != null) menu.setMenuNameVi(req.menuNameVi());
        if (req.linkUri()      != null) menu.setLinkUri(req.linkUri());
        if (req.displayOrder() != null) menu.setDisplayOrder(req.displayOrder());
        if (req.menuType()     != null) menu.setMenuType(req.menuType());
        if (req.useYn()        != null) menu.setUseYn(req.useYn());
        if (req.lev()          != null) menu.setLev(req.lev());
        if (req.description()  != null) menu.setDescription(req.description());
        if (req.remark()       != null) menu.setRemark(req.remark());

        menu.setLastModDt(OffsetDateTime.now());
        menu.setLastModUserUid(currentUserUid);

        return MenuDto.fromEntity(uaMenuRepository.save(menu));
    }

    /**
     * Xoá menu.
     * Trước tiên kiểm tra menu không còn được gán cho bất kỳ role nào.
     */
    @Transactional
    public void deleteMenu(String menuId) {
        if (!uaMenuRepository.existsById(menuId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Không tìm thấy menu: " + menuId);
        }
        if (!roleMenuRelationRepository.findByIdMenuId(menuId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Menu đang được gán cho một hoặc nhiều role. Hãy xoá phân quyền trước.");
        }
        uaMenuRepository.deleteById(menuId);
    }
}
