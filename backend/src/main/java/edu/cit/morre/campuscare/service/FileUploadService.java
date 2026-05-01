package edu.cit.morre.campuscare.service;

import edu.cit.morre.campuscare.model.Appointment;
import edu.cit.morre.campuscare.model.UploadedFile;
import edu.cit.morre.campuscare.model.User;
import edu.cit.morre.campuscare.repository.AppointmentRepository;
import edu.cit.morre.campuscare.repository.UploadedFileRepository;
import edu.cit.morre.campuscare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class FileUploadService {

    private final UploadedFileRepository uploadedFileRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    public FileUploadService(UploadedFileRepository uploadedFileRepository,
                             UserRepository userRepository,
                             AppointmentRepository appointmentRepository) {
        this.uploadedFileRepository = uploadedFileRepository;
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
    }

    // ✅ Upload and store file on server + save record to DB
    public UploadedFile uploadFile(String email, Long appointmentId, MultipartFile file) throws IOException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create upload directory if needed
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename to avoid conflicts
        String originalName = file.getOriginalFilename();
        String extension = (originalName != null && originalName.contains("."))
                ? originalName.substring(originalName.lastIndexOf("."))
                : "";
        String uniqueFileName = UUID.randomUUID() + extension;
        Path filePath = uploadPath.resolve(uniqueFileName);

        // ✅ Save file to server disk
        Files.copy(file.getInputStream(), filePath);

        // ✅ Save record to database (linked to user and optionally appointment)
        UploadedFile uploadedFile = new UploadedFile();
        uploadedFile.setUser(user);
        uploadedFile.setFileName(originalName);
        uploadedFile.setFilePath(filePath.toString());

        if (appointmentId != null) {
            Appointment appointment = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new RuntimeException("Appointment not found"));
            uploadedFile.setAppointment(appointment);
        }

        return uploadedFileRepository.save(uploadedFile);
    }

    // ✅ Get file by ID (for download/view)
    public UploadedFile getFileById(Long fileId) {
        return uploadedFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));
    }

    // ✅ Get all files for an appointment
    public List<UploadedFile> getFilesForAppointment(Long appointmentId) {
        return uploadedFileRepository.findByAppointmentId(appointmentId);
    }

    // ✅ Get all files for a user
    public List<UploadedFile> getFilesForUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return uploadedFileRepository.findByUserId(user.getId());
    }

    // ✅ Delete file from disk and database
    public void deleteFile(Long fileId, String email) throws IOException {
        UploadedFile uploadedFile = uploadedFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        // Only the owner can delete
        if (!uploadedFile.getUser().getEmail().equals(email)) {
            throw new RuntimeException("You are not authorized to delete this file");
        }

        // Delete from disk
        Path filePath = Paths.get(uploadedFile.getFilePath());
        Files.deleteIfExists(filePath);

        // Delete from database
        uploadedFileRepository.delete(uploadedFile);
    }
}