package com.tomassirio.wanderer.auth.repository;

import com.tomassirio.wanderer.auth.domain.Credential;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CredentialRepository extends JpaRepository<Credential, UUID> {
    Optional<Credential> findByEmail(String email);

    /**
     * Checks if any credentials exist with roles containing the specified role string.
     *
     * @param role the role string to search for (e.g., "ADMIN")
     * @return true if at least one credential has the role, false otherwise
     */
    boolean existsByRolesContaining(String role);
}
