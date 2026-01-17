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
            // Use frontend URL for email verification link
            String frontendUrl = applicationConfig.getFrontendUrl() != null 
                    ? applicationConfig.getFrontendUrl() 
                    : applicationConfig.getBaseUrl().replace(":8080", ":3000");
            String verificationUrl = frontendUrl + "/verify-email/" + token;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(applicationConfig.getEmailFrom());
            message.setTo(to);
            message.setSubject("Email Verification - " + applicationConfig.getName());
            message.setText("Please click the following link to verify your email:\n\n" + verificationUrl +
                    "\n\nThis link will expire in 24 hours.\n\n" +
                    "Or copy and paste this URL in your browser:\n" + verificationUrl);

            mailSender.send(message);
            log.info("Verification email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", to, e);
        }
    }

    public void sendPasswordResetEmail(String to, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(applicationConfig.getEmailFrom());
            message.setTo(to);
            message.setSubject("Password Reset OTP - " + applicationConfig.getName());
            message.setText("You have requested to reset your password.\n\n" +
                    "Your OTP code is: " + otp + "\n\n" +
                    "Please enter this code in the reset password form. This code will expire in 10 minutes.\n\n" +
                    "If you did not request this, please ignore this email.");

            mailSender.send(message);
            log.info("Password reset OTP sent to: {}", to);
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
