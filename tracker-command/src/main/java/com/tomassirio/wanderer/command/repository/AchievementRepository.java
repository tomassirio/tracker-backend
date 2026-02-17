package com.tomassirio.wanderer.command.repository;

import com.tomassirio.wanderer.commons.domain.Achievement;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, UUID> {}
