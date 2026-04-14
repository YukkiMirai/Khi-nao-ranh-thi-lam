package com.wibuneverdie.core.repository;

import com.wibuneverdie.core.entity.UaMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UaMenuRepository extends JpaRepository<UaMenu, String> {

    /** Lấy tất cả menu gốc (không có parent) */
    List<UaMenu> findByUpperMenuIsNull();

    /** Lấy danh sách menu con theo parent */
    List<UaMenu> findByUpperMenuMenuId(String upperMenuId);

    /** Lấy menu đang hoạt động, sắp xếp theo thứ tự hiển thị */
    List<UaMenu> findByUseYnOrderByDisplayOrderAsc(String useYn);

    /** Lấy toàn bộ cây menu đang hoạt động (root + children trong 1 query) */
    @Query("SELECT m FROM UaMenu m LEFT JOIN FETCH m.children WHERE m.upperMenu IS NULL AND m.useYn = :useYn ORDER BY m.displayOrder ASC")
    List<UaMenu> findRootMenusWithChildren(String useYn);

    /**
     * Lấy danh sách menu mà user có quyền truy cập,
     * join qua ua_role_menu_relation theo danh sách roleId.
     */
    @Query("""
            SELECT DISTINCT m FROM UaMenu m
            JOIN UaRoleMenuRelation rmr ON rmr.id.menuId = m.menuId
            WHERE rmr.id.roleId IN :roleIds
              AND m.useYn = 'Y'
            ORDER BY m.lev ASC, m.displayOrder ASC
            """)
    List<UaMenu> findMenusByRoleIds(List<String> roleIds);
}
