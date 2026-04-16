package edu.cit.morre.campuscare.controller;

import edu.cit.morre.campuscare.dto.AppointmentRequest;
import edu.cit.morre.campuscare.model.Appointment;
import edu.cit.morre.campuscare.model.User;
import edu.cit.morre.campuscare.repository.AppointmentRepository;
import edu.cit.morre.campuscare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalTime;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentRepository repo;
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/all") // Admin Panel calls this
    public List<Appointment> getAll() {
        return repo.findAll();
    }

    @GetMapping("/my")
    public List<Appointment> getMyAppointments() {
        String email = (String) org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return repo.findByUserEmail(email);
    }

    @PutMapping("/{id}/status") // Admin Status Update
    public Appointment updateStatus(@PathVariable Long id, @RequestBody Appointment statusUpdate) {
        Appointment appt = repo.findById(id).orElseThrow();
        appt.setStatus(statusUpdate.getStatus());
        return repo.save(appt);
    }
    @PostMapping
    public Appointment bookAppointment(@RequestBody AppointmentRequest request, Principal principal) {
        // 1. Get the email from the authenticated JWT (Principal)
        String email = principal.getName();

        // Debugging: check the console to see if the email is correct
        System.out.println("Booking appointment for: " + email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        Appointment appointment = new Appointment();
        appointment.setUser(user);
        appointment.setReason(request.getReason());

        // Ensure you use the correct Date/Time parsing
        appointment.setAppointmentDate(LocalDate.parse(request.getAppointmentDate()));
        appointment.setAppointmentTime(LocalTime.parse(request.getAppointmentTime()));
        appointment.setStatus("PENDING");

        return repo.save(appointment);
    }
}