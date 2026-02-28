# Email Verification Configuration Guide

## Overview

The tracker-auth module supports email verification for user registration with two modes:
- **Development Mode**: Logs emails to console (default)
- **Production Mode**: Sends actual emails via SMTP using Jakarta Mail (Angus Mail)

## Configuration

### Development Mode (Console Logging)

By default, emails are logged to the console. This is useful for development and testing.

```properties
# application.properties
app.email.enabled=false  # or simply omit this property
```

When a user registers, you'll see the verification email in the application logs:

```
========================================
EMAIL VERIFICATION
========================================
To: user@example.com
Subject: Verify your email address
----------------------------------------
Hello username,

Thank you for registering!

Please verify your email address by using the following token:

Token: abc123...xyz789

You can verify your email by sending a POST request to /api/1/auth/verify-email with this token.

This token will expire in 24 hours.

If you did not create an account, please ignore this email.
========================================
```

### Production Mode (SMTP)

To send actual emails via SMTP, configure the following properties:

```properties
# Enable SMTP email sending
app.email.enabled=true

# SMTP Server Configuration (Brevo/Sendinblue)
app.email.host=smtp-relay.brevo.com
app.email.port=587

# SMTP Authentication
app.email.username=a39a49001@smtp-brevo.com
app.email.password=your-brevo-smtp-key  # Set via EMAIL_PASSWORD environment variable

# Email Sender Configuration
app.email.from=noreply@tomassir.io
app.email.from-name=Wanderer No Reply

# Application Base URL (for verification links)
app.email.base-url=https://tracker.example.com

# SMTP Settings
app.email.auth=true
app.email.start-tls=true
```

### Environment Variables

All email properties can be configured via environment variables:

| Property | Environment Variable | Default | Required |
|----------|---------------------|---------|----------|
| `app.email.enabled` | `EMAIL_ENABLED` | `false` | No |
| `app.email.host` | `EMAIL_HOST` | `smtp-relay.brevo.com` | Yes (if enabled) |
| `app.email.port` | `EMAIL_PORT` | `587` | No |
| `app.email.username` | `EMAIL_USERNAME` | `a39a49001@smtp-brevo.com` | Yes (if enabled) |
| `app.email.password` | `EMAIL_PASSWORD` | - | Yes (if enabled) - **Use GitHub Secret** |
| `app.email.from` | `EMAIL_FROM` | `noreply@tomassir.io` | No |
| `app.email.from-name` | `EMAIL_FROM_NAME` | `Wanderer No Reply` | No |
| `app.email.base-url` | `EMAIL_BASE_URL` | `http://localhost:3000` | No |
| `app.email.auth` | `EMAIL_AUTH` | `true` | No |
| `app.email.start-tls` | `EMAIL_START_TLS` | `true` | No |

### GitHub Secrets Configuration

For production deployments via GitHub Actions, configure the SMTP password as a secret:

1. Go to your repository **Settings** → **Secrets and variables** → **Actions**
2. Add a new repository secret named `EMAIL_PASSWORD`
3. Set the value to your Brevo SMTP key
4. Reference it in your GitHub Actions workflow:

```yaml
# .github/workflows/deploy.yml
env:
  EMAIL_ENABLED: true
  EMAIL_PASSWORD: ${{ secrets.EMAIL_PASSWORD }}
```

### Example Docker Configuration

```yaml
# docker-compose.yml
services:
  tracker-auth:
    environment:
      - EMAIL_ENABLED=true
      - EMAIL_HOST=smtp-relay.brevo.com
      - EMAIL_PORT=587
      - EMAIL_USERNAME=a39a49001@smtp-brevo.com
      - EMAIL_PASSWORD=${EMAIL_PASSWORD}  # Pass from GitHub secret or .env file
      - EMAIL_FROM=noreply@tomassir.io
      - EMAIL_FROM_NAME=Wanderer No Reply
      - EMAIL_BASE_URL=https://tracker.example.com
```

## Supported SMTP Providers

### Brevo (Sendinblue) - **Default Configuration**

```properties
app.email.host=smtp-relay.brevo.com
app.email.port=587
app.email.username=a39a49001@smtp-brevo.com
app.email.password=your-brevo-smtp-key  # Get from Brevo dashboard
app.email.from=noreply@tomassir.io
app.email.from-name=Wanderer No Reply
app.email.auth=true
app.email.start-tls=true
```

**Note**: Get your SMTP key from the [Brevo dashboard](https://app.brevo.com/settings/keys/smtp) under Settings → SMTP & API.

### Gmail

```properties
app.email.host=smtp.gmail.com
app.email.port=587
app.email.username=your-email@gmail.com
app.email.password=your-app-password  # Use App Password, not regular password
app.email.auth=true
app.email.start-tls=true
```

**Note**: For Gmail, you need to create an [App Password](https://support.google.com/accounts/answer/185833):
1. Enable 2-Factor Authentication on your Google account
2. Go to Google Account Settings > Security > App Passwords
3. Generate a new app password for "Mail"
4. Use this app password in the configuration

### SendGrid

```properties
app.email.host=smtp.sendgrid.net
app.email.port=587
app.email.username=apikey
app.email.password=your-sendgrid-api-key
app.email.auth=true
app.email.start-tls=true
```

### Amazon SES

```properties
app.email.host=email-smtp.us-east-1.amazonaws.com
app.email.port=587
app.email.username=your-smtp-username
app.email.password=your-smtp-password
app.email.auth=true
app.email.start-tls=true
```

### Mailgun

```properties
app.email.host=smtp.mailgun.org
app.email.port=587
app.email.username=postmaster@your-domain.mailgun.org
app.email.password=your-mailgun-smtp-password
app.email.auth=true
app.email.start-tls=true
```

### Custom SMTP Server

```properties
app.email.host=smtp.your-server.com
app.email.port=587  # or 465 for SSL
app.email.username=your-smtp-username
app.email.password=your-smtp-password
app.email.auth=true
app.email.start-tls=true  # or false if using SSL on port 465
```

## Email Template

The verification email is sent as HTML with the following content:

- **Subject**: "Verify your email address"
- **Header**: "Welcome to Tracker!"
- **Body**:
  - Greeting with username
  - Clickable verification button
  - Plain text verification token (for API clients)
  - Expiry notice (24 hours)
  - Security notice

### Customization

To customize the email template, modify the `buildEmailContent()` method in `SmtpEmailServiceImpl.java`.

## Testing

### Test Console Mode

1. Set `app.email.enabled=false` (or omit the property)
2. Register a new user via POST `/api/1/auth/register`
3. Check application logs for the verification email
4. Copy the token from the logs
5. Verify email via POST `/api/1/auth/verify-email` with the token

### Test SMTP Mode

1. Configure SMTP settings as described above
2. Set `app.email.enabled=true`
3. Register a new user via POST `/api/1/auth/register`
4. Check your email inbox for the verification email
5. Click the verification link or use the token with POST `/api/1/auth/verify-email`

## Troubleshooting

### Emails Not Sending

1. **Check SMTP credentials**: Verify username and password are correct
2. **Check firewall**: Ensure port 587 (or 465) is not blocked
3. **Check logs**: Look for email sending errors in application logs
4. **Test SMTP connection**: Use telnet or other tools to verify SMTP server is reachable

### Authentication Errors

- For Gmail: Ensure you're using an App Password, not your regular password
- Verify `app.email.auth=true`
- Check if your SMTP provider requires specific authentication settings

### Connection Timeout

- Verify `app.email.host` and `app.email.port` are correct
- Check network/firewall rules
- Try alternative ports (e.g., 465 for SSL)

### SSL/TLS Issues

- For TLS (port 587): Set `app.email.start-tls=true`
- For SSL (port 465): Set `app.email.start-tls=false`

## Security Considerations

- **Never commit SMTP passwords**: Use environment variables or secrets management
- **Use App Passwords**: For Gmail and similar providers, use app-specific passwords
- **Limit rate**: Consider implementing rate limiting for registration to prevent spam
- **Monitor sending**: Set up alerts for failed email deliveries
- **SPF/DKIM/DMARC**: Configure these DNS records to improve email deliverability

## Architecture

The email service uses Spring Boot's conditional configuration:

- `EmailServiceImpl`: Active when `app.email.enabled=false` (console logging)
- `SmtpEmailServiceImpl`: Active when `app.email.enabled=true` (SMTP sending)
- `MailConfig`: Only loaded when SMTP is enabled
- `EmailProperties`: Configuration properties class

Only one implementation is active at runtime based on the `app.email.enabled` property.
