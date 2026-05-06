package edu.cit.morre.campuscare.features.auth.model


data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String
)
