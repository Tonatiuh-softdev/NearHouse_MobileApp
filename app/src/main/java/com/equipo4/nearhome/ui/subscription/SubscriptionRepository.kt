package com.equipo4.nearhome.ui.subscription

import android.content.Context
import com.equipo4.nearhome.R
import com.equipo4.nearhome.data.local.SessionManager
import com.equipo4.nearhome.data.session.CurrentUserStore
import com.equipo4.nearhome.di.NetworkModule
import com.equipo4.nearhome.network.AuthApi
import com.equipo4.nearhome.network.ContratarPlanRequest
import com.equipo4.nearhome.network.PlanDto
import com.equipo4.nearhome.network.UsuarioMeDto
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

sealed interface Resultado<out T> {
    data class Ok<out T>(val valor: T) : Resultado<T>
    data class Error(val mensaje: String) : Resultado<Nothing>
}

class SubscriptionRepository(context: Context) {

    private val app = context.applicationContext
    private val session = SessionManager(app)
    private val api = NetworkModule.provideRetrofit(NetworkModule.provideOkHttpClient(session))
        .create(AuthApi::class.java)

    suspend fun obtenerPlanes(): Resultado<List<PlanDto>> = llamar { api.getPlanes() }

    /** Contrata o cambia el plan. Guarda el token nuevo y actualiza el usuario compartido. */
    suspend fun contratar(planId: Int): Resultado<UsuarioMeDto> =
        when (val r = llamar { api.contratarPlan(ContratarPlanRequest(planId)) }) {
            is Resultado.Ok -> {
                session.saveToken(r.valor.token)
                CurrentUserStore.set(r.valor.usuario)
                Resultado.Ok(r.valor.usuario)
            }
            is Resultado.Error -> Resultado.Error(r.mensaje)
        }

    private suspend fun <T> llamar(bloque: suspend () -> Response<T>): Resultado<T> = try {
        val respuesta = bloque()
        val cuerpo = respuesta.body()
        if (respuesta.isSuccessful && cuerpo != null) {
            Resultado.Ok(cuerpo)
        } else {
            Resultado.Error(mensajeApi(respuesta.errorBody()?.string()) ?: app.getString(R.string.error_unexpected_retry))
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        Resultado.Error(app.getString(R.string.network_error_connection))
    } catch (e: Exception) {
        Resultado.Error(app.getString(R.string.error_unexpected_retry))
    }

    private fun mensajeApi(raw: String?): String? = try {
        JSONObject(raw ?: "").optString("error").takeIf { it.isNotBlank() }
    } catch (e: Exception) {
        null
    }
}