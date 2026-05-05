package edu.cit.morre.campuscare.model

data class AppointmentRequest(
        val reason: String,
        val appointmentDate: String,  // "2026-04-20"
        val appointmentTime: String,  // "09:00"
        val notes: String = ""
)