package com.equipo4.nearhome.di

import com.equipo4.nearhome.data.local.SessionManager //Importante: ajusta a donde está SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    // 10.0.2.2 es la IP para conectar desde el emulador a tu Docker local en el puerto 8080 (cambia si cambias de red)
    const val BASE_URL = "https://nearhouseapi-production.up.railway.app/"

    /** URL de la foto. El parámetro v cambia cuando la foto cambia, para no usar la vieja en caché. */
    fun fotoPerfilUrl(version: String): String = "${BASE_URL}api/v1/usuarios/me/foto?v=$version"

    fun provideOkHttpClient(sessionManager: SessionManager): OkHttpClient {
        // Nivel BASIC obligatorio para NO imprimir contraseñas ni tokens en Logcat
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val authInterceptor = Interceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
            sessionManager.getToken()?.let { token ->
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }
            val response = chain.proceed(requestBuilder.build())

            if (response.code == 401) {
                val path = chain.request().url.encodedPath
                val isAuthRoute = path.contains("auth/login") || path.contains("auth/registro")
                if (!isAuthRoute) {
                    sessionManager.expireSession()
                }
            }
            response
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .build()
    }

    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}