package com.tomassirio.wanderer.auth.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tomassirio.wanderer.auth.config.EmailProperties;
import com.tomassirio.wanderer.auth.service.impl.SmtpEmailServiceImpl;
import com.tomassirio.wanderer.commons.exception.EmailSendException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class SmtpEmailServiceImplTest {

    @Mock private JavaMailSender mailSender;

    @Mock private EmailProperties emailProperties;

    @Mock private MimeMessage mimeMessage;

    private SmtpEmailServiceImpl smtpEmailService;

    @BeforeEach
    void setUp() {
        smtpEmailService = new SmtpEmailServiceImpl(mailSender, emailProperties);
    }

    @Test
    void sendVerificationEmail_shouldSendEmailSuccessfully() throws Exception {
        // Given
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(emailProperties.getFrom()).thenReturn("noreply@tomassir.io");
        when(emailProperties.getFromName()).thenReturn("Wanderer No Reply");
        when(emailProperties.getBaseUrl()).thenReturn("http://localhost:3000");

        // When
        smtpEmailService.sendVerificationEmail("user@example.com", "testuser", "abc123token");

        // Then
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendVerificationEmail_whenAuthenticationFails_shouldThrowEmailSendException() {
        // Given
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(emailProperties.getFrom()).thenReturn("noreply@tomassir.io");
        when(emailProperties.getFromName()).thenReturn("Wanderer No Reply");
        when(emailProperties.getBaseUrl()).thenReturn("http://localhost:3000");
        doThrow(new MailAuthenticationException("535 5.7.8 Authentication failed"))
                .when(mailSender)
                .send(any(MimeMessage.class));

        // When & Then
        assertThatThrownBy(
                        () ->
                                smtpEmailService.sendVerificationEmail(
                                        "user@example.com", "testuser", "abc123token"))
                .isInstanceOf(EmailSendException.class)
                .hasMessageContaining("SMTP authentication failed");
    }

    @Test
    void sendVerificationEmail_whenMessagingExceptionOccurs_shouldThrowEmailSendException()
            throws Exception {
        // Given
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(emailProperties.getFrom()).thenReturn("noreply@tomassir.io");
        when(emailProperties.getFromName()).thenReturn("Wanderer No Reply");
        when(emailProperties.getBaseUrl()).thenReturn("http://localhost:3000");
        doThrow(
                        new RuntimeException(
                                "Connection refused", new MessagingException("Connection refused")))
                .when(mailSender)
                .send(any(MimeMessage.class));

        // When & Then
        assertThatThrownBy(
                        () ->
                                smtpEmailService.sendVerificationEmail(
                                        "user@example.com", "testuser", "abc123token"))
                .isInstanceOf(EmailSendException.class)
                .hasMessageContaining("Failed to send verification email");
    }

    @Test
    void sendVerificationEmail_whenUnexpectedExceptionOccurs_shouldThrowEmailSendException() {
        // Given
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(emailProperties.getFrom()).thenReturn("noreply@tomassir.io");
        when(emailProperties.getFromName()).thenReturn("Wanderer No Reply");
        when(emailProperties.getBaseUrl()).thenReturn("http://localhost:3000");
        doThrow(new RuntimeException("Unexpected error"))
                .when(mailSender)
                .send(any(MimeMessage.class));

        // When & Then
        assertThatThrownBy(
                        () ->
                                smtpEmailService.sendVerificationEmail(
                                        "user@example.com", "testuser", "abc123token"))
                .isInstanceOf(EmailSendException.class)
                .hasMessageContaining("Failed to send verification email");
    }
}
