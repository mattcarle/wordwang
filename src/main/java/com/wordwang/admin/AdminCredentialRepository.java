package com.wordwang.admin;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminCredentialRepository extends JpaRepository<AdminCredential, Long> {

    Optional<AdminCredential> findFirstByOrderByIdAsc();
}
