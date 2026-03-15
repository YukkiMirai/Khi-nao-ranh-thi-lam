package com.wibuneverdie.core.repository;

import com.wibuneverdie.core.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Integer> {

    Optional<Patient> findByMobilePhone(String mobilePhone);

    List<Patient> findByLastNameContainingIgnoreCaseOrFirstNameContainingIgnoreCase(String lastName, String firstName);

    boolean existsByMobilePhone(String mobilePhone);
}
