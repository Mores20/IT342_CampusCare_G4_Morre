package edu.cit.morre.campuscare.shared.network

import android.content.Context
import android.content.SharedPreferences
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class TokenManagerTest {

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var sharedPreferences: SharedPreferences

    @Mock
    private lateinit var editor: SharedPreferences.Editor

    @Before
    fun setUp() {
        `when`(context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE))
            .thenReturn(sharedPreferences)
        `when`(sharedPreferences.edit()).thenReturn(editor)
        `when`(editor.putString(anyString(), anyString())).thenReturn(editor)
        `when`(editor.clear()).thenReturn(editor)
    }

    @Test
    fun testSaveToken() {
        TokenManager.saveToken(context, "mock-jwt-token")
        verify(editor).putString("jwt_token", "mock-jwt-token")
        verify(editor).apply()
    }

    @Test
    fun testGetToken_ReturnsToken() {
        `when`(sharedPreferences.getString("jwt_token", null)).thenReturn("mock-jwt-token")
        assertEquals("mock-jwt-token", TokenManager.getToken(context))
    }

    @Test
    fun testGetToken_ReturnsNullWhenEmpty() {
        `when`(sharedPreferences.getString("jwt_token", null)).thenReturn(null)
        assertNull(TokenManager.getToken(context))
    }

    @Test
    fun testSaveAndGetEmail() {
        `when`(sharedPreferences.getString("user_email", null)).thenReturn("test@example.com")
        TokenManager.saveEmail(context, "test@example.com")
        assertEquals("test@example.com", TokenManager.getEmail(context))
    }

    @Test
    fun testSaveAndGetRole() {
        `when`(sharedPreferences.getString("user_role", null)).thenReturn("STUDENT")
        TokenManager.saveRole(context, "STUDENT")
        assertEquals("STUDENT", TokenManager.getRole(context))
    }

    @Test
    fun testClearToken() {
        TokenManager.clearToken(context)
        verify(editor).clear()
        verify(editor).apply()
    }

    @Test
    fun testSaveAndGetFirstName() {
        `when`(sharedPreferences.getString("user_first_name", null)).thenReturn("John")
        TokenManager.saveFirstName(context, "John")
        assertEquals("John", TokenManager.getFirstName(context))
    }
}