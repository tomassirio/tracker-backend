package com.tomassirio.wanderer.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for email sending via SMTP. These properties can be configured via
 * application.properties or environment variables.
 */
@Configuration
@ConfigurationProperties(prefix = "app.email")
@Data
public class EmailProperties {

    /** Whether email sending is enabled (true) or should fall back to console logging (false) */
    private boolean enabled = false;

    /** SMTP server host (e.g., smtp.gmail.com, smtp.sendgrid.net) */
    private String host;

    /** SMTP server port (e.g., 587 for TLS, 465 for SSL) */
    private int port = 587;

    /** SMTP username or email address */
    private String username;

    /** SMTP password or API key */
    private String password;

    /** Email address to send from */
    private String from;

    /** Display name for the sender */
    private String fromName = "Tracker App";

    /** Base URL for the application (e.g., https://tracker.example.com) */
    private String baseUrl = "http://localhost:3000";

    /** Whether to use TLS for SMTP connection */
    private boolean startTls = true;

    /** Whether to enable SMTP authentication */
    private boolean auth = true;
}
