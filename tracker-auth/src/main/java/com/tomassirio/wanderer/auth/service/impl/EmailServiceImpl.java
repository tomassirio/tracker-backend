package com.tomassirio.wanderer.auth.service.impl;

import com.tomassirio.wanderer.auth.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Email service implementation that logs verification emails to the console. This is useful for
 * development and testing. In production, this can be replaced with an SMTP-based implementation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Override
    public void sendVerificationEmail(String email, String username, String verificationToken) {
        // For now, just log the email content
        // In production, this would send an actual email via SMTP
        log.info("========================================");
        log.info("EMAIL VERIFICATION");
        log.info("========================================");
        log.info("To: {}", email);
        log.info("Subject: Verify your email address");
        log.info("----------------------------------------");
        log.info("Hello {},", username);
        log.info("");
        log.info("Thank you for registering!");
        log.info("");
        log.info("Please verify your email address by using the following token:");
        log.info("");
        log.info("Token: {}", verificationToken);
        log.info("");
        log.info(
                "You can verify your email by sending a POST request to /api/1/auth/verify-email"
                        + " with this token.");
        log.info("");
        log.info("This token will expire in 24 hours.");
        log.info("");
        log.info("If you did not create an account, please ignore this email.");
        log.info("========================================");
    }
}
