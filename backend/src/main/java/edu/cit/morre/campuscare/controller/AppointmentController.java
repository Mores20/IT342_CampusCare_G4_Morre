package edu.cit.morre.campuscare.controller;

import edu.cit.morre.campuscare.dto.AppointmentRequest;
import edu.cit.morre.campuscare.model.Appointment;
import edu.cit.morre.campuscare.service.AppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/appointments")
@CrossOrigin(origins = "http://localhost:3000")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    // ✅ Book appointment
    @PostMapping
    public ResponseEntity<Appointment> bookAppointment(
            @RequestBody AppointmentRequest request,
            Authentication authentication) {
        Appointment appointment = appointmentService.bookAppointment(
                authentication.getName(), request
        );
        return ResponseEntity.ok(appointment);
    }

    // ✅ Get my appointments (student)
    @GetMapping("/my")
    public ResponseEntity<List<Appointment>> getMyAppointments(Authentication authentication) {
        return ResponseEntity.ok(
                appointmentService.getMyAppointments(authentication.getName())
        );
    }

    // ✅ Get all appointments (admin)
    @GetMapping("/all")
    public ResponseEntity<List<Appointment>> getAllAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    // ✅ Update status (admin) — sends email notification automatically
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String newStatus = body.get("status");

        if (newStatus == null || newStatus.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Status is required"));
        }

        Appointment updated = appointmentService.updateStatus(id, newStatus);
        return ResponseEntity.ok(updated);
    }
}