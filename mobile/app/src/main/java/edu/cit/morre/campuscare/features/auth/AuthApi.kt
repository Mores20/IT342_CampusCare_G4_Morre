package edu.cit.morre.campuscare.features.auth

import edu.cit.morre.campuscare.features.appointment.model.AppointmentRequest
import edu.cit.morre.campuscare.features.appointment.AppointmentResponse
import edu.cit.morre.campuscare.features.auth.model.AuthResponse
import edu.cit.morre.campuscare.features.profile.model.ChangePasswordRequest
import edu.cit.morre.campuscare.features.auth.model.LoginRequest
import edu.cit.morre.campuscare.features.profile.model.ProfileResponse
import edu.cit.morre.campuscare.features.auth.model.RegisterRequest
import edu.cit.morre.campuscare.features.profile.model.UpdateProfileRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface AuthApi {

    // ── Auth ──
    @POST("auth/login")
    fun login(@Body request: LoginRequest): Call<AuthResponse>

    @POST("auth/register")
    fun register(@Body request: RegisterRequest): Call<AuthResponse>

    @POST("auth/google")
    fun googleLogin(@Body request: Map<String, String>): Call<AuthResponse>

    @POST("auth/logout")
    fun logout(@Body request: Map<String, String>): Call<Void>

    // ── Appointments ──
    @POST("appointments")
    fun bookAppointment(@Body request: AppointmentRequest): Call<AppointmentResponse>

    @GET("appointments/my")
    fun getMyAppointments(): Call<List<AppointmentResponse>>

    @GET("appointments/all")
    fun getAllAppointments(): Call<List<AppointmentResponse>>

    @PUT("appointments/{id}/status")
    fun updateAppointmentStatus(
        @Path("id") id: Long,
        @Body body: Map<String, String>
    ): Call<AppointmentResponse>

    // ── Profile ──
    @GET("profile")
    fun getProfile(): Call<ProfileResponse>

    @PUT("profile")
    fun updateProfile(@Body request: UpdateProfileRequest): Call<Void>

    @PUT("profile/change-password")  // Changed from "auth/change-password"
    fun changePassword(@Body request: ChangePasswordRequest): Call<Map<String, Any>>

    // ── File Upload ──
    @Multipart
    @POST("files/upload")
    fun uploadFile(
        @Part file: MultipartBody.Part,
        @Part("appointmentId") appointmentId: RequestBody?
    ): Call<Map<String, Any>>

    @GET("files/appointment/{appointmentId}")
    fun getFilesForAppointment(@Path("appointmentId") appointmentId: Long): Call<List<Map<String, Any>>>
}