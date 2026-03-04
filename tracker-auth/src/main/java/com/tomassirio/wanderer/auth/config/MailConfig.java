package com.tomassirio.wanderer.auth.config;

import java.util.Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * Configuration for JavaMailSender using Jakarta Mail (Angus Mail). This configuration is only
 * active when app.email.enabled=true.
 */
@Configuration
@ConditionalOnProperty(prefix = "app.email", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class MailConfig {

    private final EmailProperties emailProperties;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        mailSender.setHost(emailProperties.getHost());
        mailSender.setPort(emailProperties.getPort());
        mailSender.setUsername(emailProperties.getUsername());
        mailSender.setPassword(emailProperties.getPassword());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", String.valueOf(emailProperties.isAuth()));
        props.put("mail.smtp.starttls.enable", String.valueOf(emailProperties.isStartTls()));
        props.put("mail.debug", "false");

        return mailSender;
    }
}
