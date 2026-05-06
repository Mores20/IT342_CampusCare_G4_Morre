package edu.cit.morre.campuscare.features.auth.model

import org.junit.Assert.*
import org.junit.Test

class ModelTest {

    @Test
    fun testLoginRequest_PropertiesSetCorrectly() {
        val loginRequest = LoginRequest("test@example.com", "Password123!")
        assertEquals("test@example.com", loginRequest.email)
        assertEquals("Password123!", loginRequest.password)
    }

    @Test
    fun testRegisterRequest_AllFieldsSet() {
        val registerRequest = RegisterRequest("John", "Doe", "john@example.com", "Password123!")
        assertEquals("John", registerRequest.firstName)
        assertEquals("Doe", registerRequest.lastName)
        assertEquals("john@example.com", registerRequest.email)
        assertEquals("Password123!", registerRequest.password)
    }

    @Test
    fun testAuthResponse_AllFieldsParsed() {
        val authResponse = AuthResponse(
            accessToken = "mock-access-token",
            refreshToken = "mock-refresh-token",
            email = "test@example.com",
            firstName = "Test",
            lastName = "User",
            role = "STUDENT"
        )
        assertEquals("mock-access-token", authResponse.accessToken)
        assertEquals("mock-refresh-token", authResponse.refreshToken)
        assertEquals("test@example.com", authResponse.email)
        assertEquals("Test", authResponse.firstName)
        assertEquals("User", authResponse.lastName)
        assertEquals("STUDENT", authResponse.role)
    }

    @Test
    fun testAuthResponse_NullableFieldsCanBeNull() {
        val authResponse = AuthResponse()
        assertNull(authResponse.accessToken)
        assertNull(authResponse.email)
        assertNull(authResponse.role)
    }
}