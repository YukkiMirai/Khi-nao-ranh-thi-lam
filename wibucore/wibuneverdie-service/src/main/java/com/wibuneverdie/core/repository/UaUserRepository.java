package com.wibuneverdie.core.repository;

import com.wibuneverdie.core.entity.UaUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UaUserRepository extends JpaRepository<UaUser, String> {

    Optional<UaUser> findByUserId(String userId);

    Optional<UaUser> findByEmail(String email);

    boolean existsByUserId(String userId);

    boolean existsByEmail(String email);
}
