package com.tomassirio.wanderer.auth.client;

import com.tomassirio.wanderer.commons.constants.ApiConstants;
import java.util.Map;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "tracker-command", url = "${tracker.command.url}")
public interface TrackerCommandClient {

    @PostMapping(ApiConstants.USERS_PATH)
    UUID createUser(@RequestBody Map<String, String> payload);

    @DeleteMapping(ApiConstants.USERS_PATH + ApiConstants.USER_BY_ID_ENDPOINT)
    void deleteUser(@PathVariable UUID id);
}
