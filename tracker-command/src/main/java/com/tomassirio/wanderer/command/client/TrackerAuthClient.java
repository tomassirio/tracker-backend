package com.tomassirio.wanderer.command.client;

import com.tomassirio.wanderer.commons.constants.ApiConstants;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Feign client for communicating with the tracker-auth service. Used by admin operations to
 * delegate role management and credential deletion to the auth service.
 *
 * @since 0.5.3
 */
@FeignClient(name = "tracker-auth", url = "${tracker.auth.url}")
public interface TrackerAuthClient {

    @PostMapping(ApiConstants.ADMIN_USERS_PATH + ApiConstants.ADMIN_USER_PROMOTE_ENDPOINT)
    void promoteToAdmin(@PathVariable UUID userId);

    @DeleteMapping(ApiConstants.ADMIN_USERS_PATH + ApiConstants.ADMIN_USER_PROMOTE_ENDPOINT)
    void demoteFromAdmin(@PathVariable UUID userId);

    @DeleteMapping(ApiConstants.ADMIN_USERS_PATH + ApiConstants.ADMIN_USER_CREDENTIALS_ENDPOINT)
    void deleteCredentials(@PathVariable UUID userId);
}
