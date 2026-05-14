package edu.cit.morre.campuscare.features.email;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ✅ 1. Welcome email — sent on registration
    @Async
    public void sendWelcomeEmail(String toEmail, String firstName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject("Welcome to CampusCare! 🏥");
            helper.setText(buildWelcomeEmail(firstName), true); // true = HTML

            mailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }
    }

    // ✅ 2. Appointment status email — sent when admin updates status
    @Async
    public void sendAppointmentStatusEmail(String toEmail, String firstName,
                                           String status, String date, String time, String reason) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject("CampusCare — Appointment " + capitalize(status));
            helper.setText(buildStatusEmail(firstName, status, date, time, reason), true);

            mailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("Failed to send status email: " + e.getMessage());
        }
    }

    // ── Email Templates ──

    private String buildWelcomeEmail(String firstName) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 30px; background: #f8fbff; border-radius: 10px;">
                <div style="text-align: center; margin-bottom: 30px;">
                    <h1 style="color: #0355A1;">CampusCare</h1>
                    <p style="color: #397EBF; font-style: italic;">Fast. Simple. Secure Appointments</p>
                </div>
                <div style="background: white; padding: 30px; border-radius: 10px; border-left: 5px solid #0355A1;">
                    <h2 style="color: #0355A1;">Welcome, %s! 👋</h2>
                    <p style="color: #333; line-height: 1.6;">
                        Your CampusCare account has been created successfully.
                        You can now book clinic appointments quickly and easily.
                    </p>
                    <p style="color: #333; line-height: 1.6;">
                        To get started, log in and book your first appointment from your dashboard.
                    </p>
                    <div style="margin-top: 30px; padding: 15px; background: #f0f6ff; border-radius: 8px;">
                        <p style="margin: 0; color: #666; font-size: 13px;">
                            If you did not create this account, please contact us immediately.
                        </p>
                    </div>
                </div>
                <p style="text-align: center; color: #aaa; font-size: 12px; margin-top: 20px;">
                    © 2026 CampusCare. All rights reserved.
                </p>
            </div>
            """.formatted(firstName);
    }

    private String buildStatusEmail(String firstName, String status,
                                    String date, String time, String reason) {
        String statusColor = switch (status) {
            case "APPROVED"  -> "#2f855a";
            case "COMPLETED" -> "#3730a3";
            case "CANCELLED" -> "#c53030";
            default          -> "#b7791f";
        };

        String statusMessage = switch (status) {
            case "APPROVED"  -> "Your appointment has been approved. Please arrive on time.";
            case "COMPLETED" -> "Your appointment has been marked as completed. Thank you for visiting!";
            case "CANCELLED" -> "Unfortunately your appointment has been cancelled. Please book a new one.";
            default          -> "Your appointment status has been updated.";
        };

        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 30px; background: #f8fbff; border-radius: 10px;">
                <div style="text-align: center; margin-bottom: 30px;">
                    <h1 style="color: #0355A1;">CampusCare</h1>
                    <p style="color: #397EBF; font-style: italic;">Fast. Simple. Secure Appointments</p>
                </div>
                <div style="background: white; padding: 30px; border-radius: 10px; border-left: 5px solid %s;">
                    <h2 style="color: %s;">Appointment %s</h2>
                    <p style="color: #333;">Hi %s,</p>
                    <p style="color: #333; line-height: 1.6;">%s</p>

                    <div style="margin: 20px 0; padding: 20px; background: #f8fbff; border-radius: 8px;">
                        <h3 style="color: #0355A1; margin-top: 0;">Appointment Details</h3>
                        <p style="margin: 5px 0; color: #333;"><strong>Reason:</strong> %s</p>
                        <p style="margin: 5px 0; color: #333;"><strong>Date:</strong> %s</p>
                        <p style="margin: 5px 0; color: #333;"><strong>Time:</strong> %s</p>
                        <p style="margin: 5px 0;">
                            <strong>Status:</strong>
                            <span style="color: %s; font-weight: bold;">%s</span>
                        </p>
                    </div>
                </div>
                <p style="text-align: center; color: #aaa; font-size: 12px; margin-top: 20px;">
                    © 2026 CampusCare. All rights reserved.
                </p>
            </div>
            """.formatted(statusColor, statusColor, capitalize(status),
                firstName, statusMessage,
                reason, date, time,
                statusColor, status);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.charAt(0) + s.substring(1).toLowerCase();
    }
}