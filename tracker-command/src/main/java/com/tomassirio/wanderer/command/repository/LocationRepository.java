package com.tomassirio.wanderer.command.repository;

import com.tomassirio.wanderer.commons.domain.Location;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationRepository extends JpaRepository<Location, UUID> {}
