package edu.cit.morre.campuscare.repository;

import edu.cit.morre.campuscare.model.UploadedFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UploadedFileRepository extends JpaRepository<UploadedFile, Long> {
    List<UploadedFile> findByAppointmentId(Long appointmentId);
    List<UploadedFile> findByUserId(Long userId);
}