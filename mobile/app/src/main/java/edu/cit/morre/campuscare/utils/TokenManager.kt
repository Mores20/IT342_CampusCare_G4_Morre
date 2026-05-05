package edu.cit.morre.campuscare.utils

import android.content.Context

object TokenManager {

    private const val PREF_NAME = "auth_prefs"
    private const val KEY_TOKEN = "jwt_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_EMAIL = "user_email"
    private const val KEY_FIRST_NAME = "user_first_name"
    private const val KEY_LAST_NAME = "user_last_name"
    private const val KEY_ROLE = "user_role"

    fun saveToken(context: Context, token: String) =
        prefs(context).edit().putString(KEY_TOKEN, token).apply()

    fun getToken(context: Context): String? =
        prefs(context).getString(KEY_TOKEN, null)

    fun saveRefreshToken(context: Context, token: String) =
        prefs(context).edit().putString(KEY_REFRESH_TOKEN, token).apply()

    fun getRefreshToken(context: Context): String? =
        prefs(context).getString(KEY_REFRESH_TOKEN, null)

    fun saveEmail(context: Context, email: String) =
        prefs(context).edit().putString(KEY_EMAIL, email).apply()

    fun getEmail(context: Context): String? =
        prefs(context).getString(KEY_EMAIL, null)

    fun saveFirstName(context: Context, firstName: String) =
        prefs(context).edit().putString(KEY_FIRST_NAME, firstName).apply()

    fun getFirstName(context: Context): String? =
        prefs(context).getString(KEY_FIRST_NAME, null)

    fun saveLastName(context: Context, lastName: String) =
        prefs(context).edit().putString(KEY_LAST_NAME, lastName).apply()

    fun getLastName(context: Context): String? =
        prefs(context).getString(KEY_LAST_NAME, null)

    fun saveRole(context: Context, role: String) =
        prefs(context).edit().putString(KEY_ROLE, role).apply()

    fun getRole(context: Context): String? =
        prefs(context).getString(KEY_ROLE, null)

    fun clearToken(context: Context) =
        prefs(context).edit().clear().apply()

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
}