package com.tomassirio.wanderer.commons.exception;

/**
 * Exception thrown when sending an email fails. This can happen due to SMTP authentication errors,
 * connectivity issues, or invalid email addresses.
 */
public class EmailSendException extends RuntimeException {

    public EmailSendException(String message) {
        super(message);
    }

    public EmailSendException(String message, Throwable cause) {
        super(message, cause);
    }
}
