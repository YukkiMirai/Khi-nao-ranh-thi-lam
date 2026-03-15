package com.wibuneverdie.core.repository;

import com.wibuneverdie.core.entity.UaRoleMenuRelation;
import com.wibuneverdie.core.entity.embeddable.UaRoleMenuRelationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UaRoleMenuRelationRepository extends JpaRepository<UaRoleMenuRelation, UaRoleMenuRelationId> {

    List<UaRoleMenuRelation> findByIdRoleId(String roleId);

    List<UaRoleMenuRelation> findByIdMenuId(String menuId);

    void deleteByIdRoleId(String roleId);

    void deleteByIdMenuId(String menuId);
}
