package com.tomassirio.wanderer.auth.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller to serve static assets such as the application logo. These assets are used in
 * verification result HTML pages served by the auth endpoints.
 */
@RestController
@Slf4j
public class AssetController {

    private static final String LOGO_RESOURCE = "assets/wanderer-logo.png";

    @GetMapping(value = "/assets/wanderer-logo.png", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<Resource> serveLogo() {
        try {
            ClassPathResource resource = new ClassPathResource(LOGO_RESOURCE);
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.noCache())
                    .contentType(MediaType.IMAGE_PNG)
                    .body(resource);
        } catch (Exception e) {
            log.error("Failed to serve logo asset", e);
            return ResponseEntity.notFound().build();
        }
    }
}
