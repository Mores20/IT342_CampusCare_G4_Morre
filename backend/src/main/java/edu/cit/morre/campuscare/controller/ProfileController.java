package edu.cit.morre.campuscare.controller;

import edu.cit.morre.campuscare.dto.ChangePasswordRequest;
import edu.cit.morre.campuscare.dto.UpdateProfileRequest;
import edu.cit.morre.campuscare.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/profile")
@CrossOrigin(origins = "http://localhost:3000")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<?> getProfile(Authentication authentication) {

        return ResponseEntity.ok(
                profileService.getProfile(authentication.getName())
        );
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(
            Authentication authentication,
            @RequestBody UpdateProfileRequest request
    ) {

        profileService.updateProfile(authentication.getName(), request);

        return ResponseEntity.ok(Map.of("message", "Profile updated successfully"));
    }
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            Authentication authentication,
            @RequestBody ChangePasswordRequest request
    ) {
        profileService.changePassword(
                authentication.getName(),
                request.getCurrentPassword(),
                request.getNewPassword()
        );

        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }
}