package com.equipo4.nearhome.data.remote

import com.equipo4.nearhome.di.NetworkModule
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/** Una colonia del catálogo que regresa el API (solo colonias con anuncios publicados). */
data class ColoniaDto(
    @SerializedName("id") val id: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("totalAnuncios") val totalAnuncios: Int = 0
)

interface ColoniaApi {
    /**
     * GET /api/v1/colonias?modalidad=Venta&tipo=CASA
     * Los dos parámetros son opcionales: si van null, Retrofit no los manda.
     */
    @GET("api/v1/colonias")
    suspend fun listar(
        @Query("modalidad") modalidad: String?,
        @Query("tipo") tipo: String?
    ): List<ColoniaDto>
}

/**
 * La ruta de colonias es pública, por eso no necesita sesión ni token.
 * Usa la misma BASE_URL que el resto de la app.
 */
object ColoniaApiProvider {
    val api: ColoniaApi by lazy {
        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            .build()
        Retrofit.Builder()
            .baseUrl(NetworkModule.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ColoniaApi::class.java)
    }
}