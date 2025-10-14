package com.tomassirio.wanderer.auth.domain;

import com.tomassirio.wanderer.commons.security.Role;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Converter
public class RolesConverter implements AttributeConverter<Set<Role>, String> {
    private static final String SEPARATOR = ",";

    @Override
    public String convertToDatabaseColumn(Set<Role> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "";
        }
        return attribute.stream().map(Role::name).collect(Collectors.joining(SEPARATOR));
    }

    @Override
    public Set<Role> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return Collections.emptySet();
        }
        return Arrays.stream(dbData.split(SEPARATOR))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Role::fromString)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .collect(Collectors.toSet());
    }
}
