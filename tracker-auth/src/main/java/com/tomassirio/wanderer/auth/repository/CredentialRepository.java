package com.tomassirio.wanderer.auth.repository;

import com.tomassirio.wanderer.auth.domain.Credential;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CredentialRepository extends JpaRepository<Credential, UUID> {
    Optional<Credential> findByEmail(String email);
}
