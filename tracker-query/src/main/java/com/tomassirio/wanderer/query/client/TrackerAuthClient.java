package com.tomassirio.wanderer.query.client;

import com.tomassirio.wanderer.commons.constants.ApiConstants;
import com.tomassirio.wanderer.commons.security.Role;
import java.util.Set;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for communicating with the tracker-auth service. Used by query operations to
 * retrieve user role information from the auth service.
 *
 * @since 0.5.3
 */
@FeignClient(name = "tracker-auth", url = "${tracker.auth.url}")
public interface TrackerAuthClient {

    @GetMapping(ApiConstants.ADMIN_USERS_PATH + ApiConstants.ADMIN_USER_ROLES_ENDPOINT)
    Set<Role> getUserRoles(@PathVariable UUID userId);
}
