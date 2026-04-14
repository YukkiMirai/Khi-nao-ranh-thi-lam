package com.wibuneverdie.core.repository;

import com.wibuneverdie.core.entity.UaRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UaRoleRepository extends JpaRepository<UaRole, String> {

    List<UaRole> findByUseYnOrderByLevelAsc(String useYn);

    List<UaRole> findByAdminRoleYn(String adminRoleYn);
}
