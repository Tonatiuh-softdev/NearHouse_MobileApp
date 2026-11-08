package com.equipo4.nearhome.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkClient {
    // Dirección IP local por defecto para el emulador Android apuntando a Ktor/Docker en puerto 8080
    //private const val BASE_URL = "http://192.168.100.8:8080/"
    private const val BASE_URL = "https://nearhouseapi-production.up.railway.app/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }
}