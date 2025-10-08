package com.tomassirio.wanderer.auth.client;

import com.tomassirio.wanderer.commons.domain.User;
import java.util.Map;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "tracker-command", url = "${tracker.command.url}")
public interface TrackerCommandClient {

    @PostMapping("/api/1/users")
    User createUser(@RequestBody Map<String, String> payload);

    @DeleteMapping("/api/1/users/{id}")
    void deleteUser(@PathVariable UUID id);
}
