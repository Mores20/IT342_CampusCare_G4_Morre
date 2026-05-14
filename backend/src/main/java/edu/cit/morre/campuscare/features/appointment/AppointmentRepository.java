package edu.cit.morre.campuscare.features.appointment;

import edu.cit.morre.campuscare.features.appointment.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    // Find appointments specifically for the logged-in student
    List<Appointment> findByUserEmail(String email);

    Optional<Appointment> findByAppointmentDateAndAppointmentTime(
            LocalDate appointmentDate,
            LocalTime appointmentTime
    );
    List<Appointment> findByAppointmentDate(LocalDate appointmentDate);
}