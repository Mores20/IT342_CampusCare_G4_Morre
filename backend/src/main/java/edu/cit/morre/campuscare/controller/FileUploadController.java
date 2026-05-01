package edu.cit.morre.campuscare.controller;

import edu.cit.morre.campuscare.model.UploadedFile;
import edu.cit.morre.campuscare.service.FileUploadService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/files")
@CrossOrigin(origins = "http://localhost:3000")
public class FileUploadController {

    private final FileUploadService fileUploadService;

    public FileUploadController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    // ✅ Upload file linked to an appointment
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            Authentication authentication,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "appointmentId", required = false) Long appointmentId) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "File is empty"));
            }

            String contentType = file.getContentType();
            if (contentType == null ||
                    (!contentType.startsWith("image/") && !contentType.equals("application/pdf"))) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Only images and PDFs are allowed"));
            }

            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "File size must be under 5MB"));
            }

            UploadedFile uploaded = fileUploadService.uploadFile(
                    authentication.getName(), appointmentId, file
            );

            return ResponseEntity.ok(Map.of(
                    "id", uploaded.getId(),
                    "fileName", uploaded.getFileName(),
                    "uploadedAt", uploaded.getUploadedAt().toString(),
                    "message", "File uploaded successfully"
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Upload failed: " + e.getMessage()));
        }
    }

    // ✅ Get all files for a specific appointment
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<List<UploadedFile>> getFilesForAppointment(
            @PathVariable Long appointmentId) {
        return ResponseEntity.ok(fileUploadService.getFilesForAppointment(appointmentId));
    }

    // ✅ Get all files uploaded by logged-in user
    @GetMapping("/my")
    public ResponseEntity<List<UploadedFile>> getMyFiles(Authentication authentication) {
        return ResponseEntity.ok(fileUploadService.getFilesForUser(authentication.getName()));
    }

    // ✅ View or download a file by ID
    // ?inline=true  → opens in browser (view)
    // ?inline=false → forces download (default)
    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long fileId,
            @RequestParam(value = "inline", defaultValue = "false") boolean inline) {
        try {
            UploadedFile uploadedFile = fileUploadService.getFileById(fileId);
            Path filePath = Paths.get(uploadedFile.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = determineContentType(uploadedFile.getFileName());
            String disposition = inline
                    ? "inline; filename=\"" + uploadedFile.getFileName() + "\""
                    : "attachment; filename=\"" + uploadedFile.getFileName() + "\"";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ✅ Delete a file
    @DeleteMapping("/{fileId}")
    public ResponseEntity<?> deleteFile(
            @PathVariable Long fileId,
            Authentication authentication) {
        try {
            fileUploadService.deleteFile(fileId, authentication.getName());
            return ResponseEntity.ok(Map.of("message", "File deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    private String determineContentType(String fileName) {
        if (fileName == null) return "application/octet-stream";
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".pdf"))  return "application/pdf";
        if (lower.endsWith(".png"))  return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".gif"))  return "image/gif";
        return "application/octet-stream";
    }
}