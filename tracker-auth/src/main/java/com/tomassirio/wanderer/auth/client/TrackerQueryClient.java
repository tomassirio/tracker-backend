package com.tomassirio.wanderer.auth.client;

import com.tomassirio.wanderer.commons.domain.User;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "tracker-query", url = "${tracker.query.url}")
public interface TrackerQueryClient {

    @GetMapping("/api/1/users/username/{username}")
    User getUserByUsername(@PathVariable String username);

    @GetMapping("/api/1/users/{id}")
    User getUserById(@PathVariable("id") UUID id);
}
