package edu.cit.morre.campuscare.features.appointment.dto;

import lombok.Data;

@Data
public class AppointmentRequest {
    private String reason;
    private String appointmentDate;  // "2026-04-20"
    private String appointmentTime;  // "09:00"
    // getters and setters...
}
