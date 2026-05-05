package edu.cit.morre.campuscare.model

data class ProfileResponse(
    val firstName: String,
    val lastName: String,
    val email: String,
    val role: String
)

data class UpdateProfileRequest(
    val firstName: String,
    val lastName: String
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
    val confirmPassword: String
)