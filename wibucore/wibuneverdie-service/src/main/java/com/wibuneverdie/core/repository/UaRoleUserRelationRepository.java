package com.wibuneverdie.core.repository;

import com.wibuneverdie.core.entity.UaRoleUserRelation;
import com.wibuneverdie.core.entity.embeddable.UaRoleUserRelationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UaRoleUserRelationRepository extends JpaRepository<UaRoleUserRelation, UaRoleUserRelationId> {

    List<UaRoleUserRelation> findByIdUserUid(String userUid);

    List<UaRoleUserRelation> findByIdRoleId(String roleId);

    /** Kiểm tra user có thuộc role cụ thể không */
    boolean existsByIdUserUidAndIdRoleId(String userUid, String roleId);

    /** Lấy danh sách roleId của 1 user */
    @Query("SELECT r.id.roleId FROM UaRoleUserRelation r WHERE r.id.userUid = :userUid")
    List<String> findRoleIdsByUserUid(String userUid);

    void deleteByIdUserUid(String userUid);

    void deleteByIdRoleId(String roleId);
}
