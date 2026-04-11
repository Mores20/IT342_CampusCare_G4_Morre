package edu.cit.morre.campuscare.api


import edu.cit.morre.campuscare.model.AuthResponse
import edu.cit.morre.campuscare.model.LoginRequest
import edu.cit.morre.campuscare.model.RegisterRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST


interface AuthApi {


    @POST("auth/login")
    fun login(@Body request: LoginRequest): Call<AuthResponse>


    @POST("auth/register")
    fun register(@Body request: RegisterRequest): Call<AuthResponse>


    @POST("auth/google")
    fun googleLogin(@Body request: Map<String, String>): Call<AuthResponse>
}
