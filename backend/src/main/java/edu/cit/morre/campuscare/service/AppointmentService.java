package edu.cit.morre.campuscare.service;

import edu.cit.morre.campuscare.dto.AppointmentRequest;
import edu.cit.morre.campuscare.model.Appointment;
import edu.cit.morre.campuscare.model.User;
import edu.cit.morre.campuscare.repository.AppointmentRepository;
import edu.cit.morre.campuscare.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              UserRepository userRepository,
                              EmailService emailService) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    // ✅ Book appointment
    public Appointment bookAppointment(String email, AppointmentRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        Appointment appointment = new Appointment();
        appointment.setUser(user);
        appointment.setReason(request.getReason());
        appointment.setAppointmentDate(LocalDate.parse(request.getAppointmentDate()));
        appointment.setAppointmentTime(LocalTime.parse(request.getAppointmentTime()));
        appointment.setStatus("PENDING");

        return appointmentRepository.save(appointment);
    }

    // ✅ Get all appointments (admin)
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    // ✅ Get appointments for a specific user
    public List<Appointment> getMyAppointments(String email) {
        return appointmentRepository.findByUserEmail(email);
    }

    // ✅ Update status + send email notification
    public Appointment updateStatus(Long appointmentId, String newStatus) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        appointment.setStatus(newStatus);
        appointmentRepository.save(appointment);

        // ✅ Send status notification email to student
        User user = appointment.getUser();
        emailService.sendAppointmentStatusEmail(
                user.getEmail(),
                user.getFirstName(),
                newStatus,
                appointment.getAppointmentDate().toString(),
                appointment.getAppointmentTime().toString(),
                appointment.getReason()
        );

        return appointment;
    }
}