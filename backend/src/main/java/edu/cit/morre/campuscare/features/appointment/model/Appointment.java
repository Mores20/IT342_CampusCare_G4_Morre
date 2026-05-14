package edu.cit.morre.campuscare.features.appointment.model;

import edu.cit.morre.campuscare.shared.model.User;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;


@Entity
@Table(name = "appointments")
@Data // Requires Lombok, otherwise generate Getters/Setters
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String reason;
    private String status = "PENDING";

    @Column(name = "appointment_date")
    private LocalDate appointmentDate;

    @Column(name = "appointment_time")
    private LocalTime appointmentTime;

    @ManyToOne (fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user; // This links to your User table
}