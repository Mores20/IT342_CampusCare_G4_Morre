package edu.cit.morre.campuscare.features.appointment;

import edu.cit.morre.campuscare.features.appointment.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    // Find appointments specifically for the logged-in student
    List<Appointment> findByUserEmail(String email);
}