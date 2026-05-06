package edu.cit.morre.campuscare.features.auth.model

data class AuthResponse(
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val email: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val role: String? = null
)