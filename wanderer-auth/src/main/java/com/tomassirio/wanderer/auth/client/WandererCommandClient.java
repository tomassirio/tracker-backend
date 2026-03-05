package com.tomassirio.wanderer.auth.client;

import com.tomassirio.wanderer.commons.constants.ApiConstants;
import java.util.Map;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "wanderer-command", url = "${wanderer.command.url}")
public interface WandererCommandClient {

    @PostMapping(ApiConstants.USERS_PATH)
    UUID createUser(@RequestBody Map<String, String> payload);

    /**
     * Deletes a user and all associated data from the command service. Used as a compensation step
     * during registration rollback. This calls an internal endpoint not exposed to the frontend.
     */
    @DeleteMapping(ApiConstants.USERS_PATH + ApiConstants.USER_BY_ID_ENDPOINT)
    void deleteUser(@PathVariable UUID id);
}
