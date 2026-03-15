package com.wibuneverdie.core.repository;

import com.wibuneverdie.core.entity.UaUserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UaUserInfoRepository extends JpaRepository<UaUserInfo, String> {
}
