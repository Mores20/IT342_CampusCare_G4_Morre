package edu.cit.morre.campuscare.features.appointment;

import edu.cit.morre.campuscare.features.appointment.dto.AppointmentRequest;
import edu.cit.morre.campuscare.features.appointment.model.Appointment;
import edu.cit.morre.campuscare.shared.model.User;
import edu.cit.morre.campuscare.shared.repository.UserRepository;
import edu.cit.morre.campuscare.features.email.EmailService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    // Book appointment
    public Appointment bookAppointment(String email, AppointmentRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        LocalDate date = LocalDate.parse(request.getAppointmentDate());
        LocalTime time = LocalTime.parse(request.getAppointmentTime());

        if (LocalDateTime.of(date, time).isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Cannot book an appointment in the past.");
        }

        appointmentRepository
                .findByAppointmentDateAndAppointmentTime(date, time)
                .ifPresent(existing -> {
                    throw new IllegalStateException(
                            "This time slot is already booked. Please choose a different time."
                    );
                });

        Appointment appointment = new Appointment();
        appointment.setUser(user);
        appointment.setReason(request.getReason());
        appointment.setAppointmentDate(LocalDate.parse(request.getAppointmentDate()));
        appointment.setAppointmentTime(LocalTime.parse(request.getAppointmentTime()));
        appointment.setStatus("PENDING");

        return appointmentRepository.save(appointment);
    }

    // Get all appointments (admin)
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    // Get appointments for a specific user
    public List<Appointment> getMyAppointments(String email) {
        return appointmentRepository.findByUserEmail(email);
    }

    // NEW — returns list of booked times ("09:00", "14:00", etc.) for a date
    public List<String> getBookedSlots(String date) {
        LocalDate localDate = LocalDate.parse(date);
        return appointmentRepository.findByAppointmentDate(localDate)
                .stream()
                .map(a -> a.getAppointmentTime().toString())
                .collect(java.util.stream.Collectors.toList());
    }

    // Update status + send email notification
    public Appointment updateStatus(Long appointmentId, String newStatus) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        appointment.setStatus(newStatus);
        appointmentRepository.save(appointment);

        // Send status notification email to student
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