package com.tomassirio.wanderer.auth.client;

import com.tomassirio.wanderer.commons.constants.ApiConstants;
import com.tomassirio.wanderer.commons.domain.User;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "tracker-query", url = "${tracker.query.url}")
public interface TrackerQueryClient {

    @GetMapping(ApiConstants.USERS_PATH + ApiConstants.USERNAME_ENDPOINT)
    User getUserByUsername(@PathVariable String username);

    @GetMapping(ApiConstants.USERS_PATH + ApiConstants.USER_BY_ID_ENDPOINT)
    User getUserById(@PathVariable("id") UUID id);
}
