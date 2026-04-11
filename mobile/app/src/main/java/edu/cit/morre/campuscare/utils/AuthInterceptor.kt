package edu.cit.morre.campuscare.utils

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response


class AuthInterceptor(private val context: Context) : Interceptor {


    override fun intercept(chain: Interceptor.Chain): Response {


        val token = TokenManager.getToken(context)


        val request = chain.request().newBuilder()


        if (token != null) {
            request.addHeader("Authorization", "Bearer $token")
        }


        return chain.proceed(request.build())
    }
}
