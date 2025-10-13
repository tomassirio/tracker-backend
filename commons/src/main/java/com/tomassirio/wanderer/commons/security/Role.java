package com.tomassirio.wanderer.commons.security;

import java.util.Optional;

/** Enum representing application roles. */
public enum Role {
    USER,
    ADMIN;

    /** Returns the Spring Security authority name for this role (e.g. ROLE_USER). */
    public String authority() {
        return "ROLE_" + name();
    }

    /**
     * Parse a role string produced in tokens or configs. Accepts values like "USER", "ROLE_USER",
     * "user".
     */
    public static Optional<Role> fromString(String s) {
        if (s == null) return Optional.empty();
        String normalized = s.startsWith("ROLE_") ? s.substring(5) : s;
        try {
            return Optional.of(Role.valueOf(normalized.toUpperCase()));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    /**
     * Derive a Role from a scope name used in tests/clients. For example: "login" -> USER, "admin"
     * -> ADMIN.
     */
    public static Optional<Role> fromScope(String scope) {
        if (scope == null) return Optional.empty();
        String s = scope.trim();
        if (s.equalsIgnoreCase("login")) return Optional.of(USER);
        if (s.equalsIgnoreCase("admin") || s.equalsIgnoreCase("administrator"))
            return Optional.of(ADMIN);
        try {
            return Optional.of(Role.valueOf(s.toUpperCase()));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
