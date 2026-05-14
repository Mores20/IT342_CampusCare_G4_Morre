package edu.cit.morre.campuscare.features.appointment;

import edu.cit.morre.campuscare.features.appointment.dto.AppointmentRequest;
import edu.cit.morre.campuscare.features.appointment.model.Appointment;
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
    public ResponseEntity<?> bookAppointment(
            @RequestBody AppointmentRequest request,
            Authentication authentication) {
        try {
            Appointment appointment = appointmentService.bookAppointment(
                    authentication.getName(), request
            );
            return ResponseEntity.ok(appointment);
        } catch (IllegalStateException e) {
            // 409 Conflict — duplicate slot
            return ResponseEntity.status(409).body(Map.of("message", e.getMessage()));
        }
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
    // ✅ NEW — returns booked time slots for a given date (used by frontend time picker)
    @GetMapping("/booked-slots")
    public ResponseEntity<List<String>> getBookedSlots(@RequestParam String date) {
        List<String> bookedTimes = appointmentService.getBookedSlots(date);
        return ResponseEntity.ok(bookedTimes);
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