package com.tomassirio.wanderer.auth.service;

/**
 * Service for sending emails. This interface abstracts email sending functionality, allowing for
 * different implementations (e.g., logging for development, SMTP for production).
 */
public interface EmailService {

    /**
     * Sends an email verification link to the specified email address.
     *
     * @param email The recipient's email address
     * @param username The username for the pending registration
     * @param verificationToken The verification token (plain text, not hashed)
     */
    void sendVerificationEmail(String email, String username, String verificationToken);

    /**
     * Sends a password reset email with a link to the password reset form.
     *
     * @param email The recipient's email address
     * @param username The username of the account
     * @param resetToken The password reset token (plain text, not hashed)
     */
    void sendPasswordResetEmail(String email, String username, String resetToken);
}
