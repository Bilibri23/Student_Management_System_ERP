package org.erp.sms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.erp.sms.config.ApplicationConfig;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final ApplicationConfig applicationConfig;

    public void sendVerificationEmail(String to, String token) {
        try {
            String verificationUrl = applicationConfig.getBaseUrl() + "/api/auth/verify-email/" + token;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(applicationConfig.getEmailFrom());
            message.setTo(to);
            message.setSubject("Email Verification - " + applicationConfig.getName());
            message.setText("Please click the following link to verify your email:\n\n" + verificationUrl +
                    "\n\nThis link will expire in 24 hours.");

            mailSender.send(message);
            log.info("Verification email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", to, e);
        }
    }

    public void sendPasswordResetEmail(String to, String token) {
        try {
            String resetUrl = applicationConfig.getBaseUrl() + "/reset-password?token=" + token;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(applicationConfig.getEmailFrom());
            message.setTo(to);
            message.setSubject("Password Reset - " + applicationConfig.getName());
            message.setText("You have requested to reset your password. Please click the following link:\n\n" +
                    resetUrl + "\n\nThis link will expire in 1 hour.\n\n" +
                    "If you did not request this, please ignore this email.");

            mailSender.send(message);
            log.info("Password reset email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", to, e);
        }
    }

    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(applicationConfig.getEmailFrom());
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
        }
    }
}
