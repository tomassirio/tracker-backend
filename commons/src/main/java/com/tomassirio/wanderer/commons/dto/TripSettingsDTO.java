package com.tomassirio.wanderer.commons.dto;

import com.tomassirio.wanderer.commons.domain.TripStatus;
import com.tomassirio.wanderer.commons.domain.TripVisibility;

public record TripSettingsDTO(
        TripStatus tripStatus, TripVisibility visibility, Integer updateRefresh) {}
