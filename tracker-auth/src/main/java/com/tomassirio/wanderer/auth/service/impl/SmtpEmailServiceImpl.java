package com.tomassirio.wanderer.auth.service.impl;

import com.tomassirio.wanderer.auth.config.EmailProperties;
import com.tomassirio.wanderer.auth.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * SMTP-based email service implementation using Jakarta Mail. This implementation sends actual
 * emails via an SMTP server. It is enabled when app.email.enabled=true.
 */
@Service
@ConditionalOnProperty(prefix = "app.email", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class SmtpEmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final EmailProperties emailProperties;

    @Override
    public void sendVerificationEmail(String email, String username, String verificationToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(emailProperties.getFrom(), emailProperties.getFromName());
            helper.setTo(email);
            helper.setSubject("Verify your email address");
            helper.setText(buildEmailContent(username, verificationToken), true);

            mailSender.send(message);
            log.info("Verification email sent successfully to: {}", email);
        } catch (MessagingException e) {
            log.error("Failed to send verification email to: {}", email, e);
            throw new IllegalStateException("Failed to send verification email", e);
        } catch (Exception e) {
            log.error("Unexpected error sending verification email to: {}", email, e);
            throw new IllegalStateException("Failed to send verification email", e);
        }
    }

    private String buildEmailContent(String username, String verificationToken) {
        String verificationLink =
                emailProperties.getBaseUrl() + "/verify-email?token=" + verificationToken;

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            line-height: 1.6;
                            color: #333;
                            max-width: 600px;
                            margin: 0 auto;
                            padding: 20px;
                        }
                        .header {
                            background-color: #4CAF50;
                            color: white;
                            padding: 20px;
                            text-align: center;
                            border-radius: 5px 5px 0 0;
                        }
                        .content {
                            background-color: #f9f9f9;
                            padding: 30px;
                            border-radius: 0 0 5px 5px;
                        }
                        .button {
                            display: inline-block;
                            padding: 12px 24px;
                            background-color: #4CAF50;
                            color: white;
                            text-decoration: none;
                            border-radius: 4px;
                            margin: 20px 0;
                        }
                        .token-box {
                            background-color: #fff;
                            border: 1px solid #ddd;
                            padding: 15px;
                            margin: 20px 0;
                            border-radius: 4px;
                            font-family: monospace;
                            word-break: break-all;
                        }
                        .footer {
                            margin-top: 30px;
                            padding-top: 20px;
                            border-top: 1px solid #ddd;
                            color: #666;
                            font-size: 12px;
                        }
                    </style>
                </head>
                <body>
                    <div class="header">
                        <h1>Welcome to Tracker!</h1>
                    </div>
                    <div class="content">
                        <p>Hello <strong>%s</strong>,</p>

                        <p>Thank you for registering with Tracker! To complete your registration, please verify your email address.</p>

                        <p><a href="%s" class="button">Verify Email Address</a></p>

                        <p>Or copy and paste this verification token:</p>

                        <div class="token-box">%s</div>

                        <p>You can verify your email by sending a POST request to <code>/api/1/auth/verify-email</code> with this token.</p>

                        <p><strong>This token will expire in 24 hours.</strong></p>

                        <p>If you did not create an account, please ignore this email.</p>

                        <div class="footer">
                            <p>This is an automated message, please do not reply to this email.</p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(username, verificationLink, verificationToken);
    }
}
