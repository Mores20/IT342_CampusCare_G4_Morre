package edu.cit.morre.campuscare.model

data class AppointmentResponse(
    val id: Long,
    val reason: String,
    val appointmentDate: String,
    val appointmentTime: String,
    val status: String,
    val user: UserInfo? = null
)

data class UserInfo(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val email: String
)