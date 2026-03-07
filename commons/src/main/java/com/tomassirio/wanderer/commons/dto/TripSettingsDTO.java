package com.tomassirio.wanderer.commons.dto;

import com.tomassirio.wanderer.commons.domain.TripModality;
import com.tomassirio.wanderer.commons.domain.TripStatus;
import com.tomassirio.wanderer.commons.domain.TripVisibility;

public record TripSettingsDTO(
        TripStatus tripStatus,
        TripVisibility visibility,
        Integer updateRefresh,
        Boolean automaticUpdates,
        TripModality tripModality) {}
