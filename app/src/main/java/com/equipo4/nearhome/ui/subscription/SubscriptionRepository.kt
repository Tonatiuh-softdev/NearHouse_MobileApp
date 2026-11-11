package com.equipo4.nearhome.ui.subscription

import android.content.Context
import com.equipo4.nearhome.data.local.SessionManager
import com.equipo4.nearhome.data.remote.codigoApi
import com.equipo4.nearhome.data.remote.mensajeApi
import com.equipo4.nearhome.data.session.CurrentUserStore
import com.equipo4.nearhome.di.NetworkModule
import com.equipo4.nearhome.network.AuthApi
import com.equipo4.nearhome.network.ContratarPlanRequest
import com.equipo4.nearhome.network.PlanDto
import com.equipo4.nearhome.network.UsuarioMeDto
import retrofit2.Response
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

sealed interface Resultado<out T> {
    data class Ok<out T>(val valor: T) : Resultado<T>
    data class Error(val mensaje: String, val codigo: String? = null) : Resultado<Nothing>
}

class SubscriptionRepository(context: Context) {

    private val app = context.applicationContext
    private val session = SessionManager(app)
    private val api = NetworkModule.provideRetrofit(NetworkModule.provideOkHttpClient(session))
        .create(AuthApi::class.java)

    suspend fun obtenerPlanes(): Resultado<List<PlanDto>> = llamar { api.getPlanes() }

    /**
     * Contrata o cambia el plan. El API devuelve un token nuevo con el rol Vendedor:
     * se guarda el token, el rol y el usuario, para que la app ya no lo vea como comprador.
     */
    suspend fun contratar(planId: Int): Resultado<UsuarioMeDto> =
        when (val r = llamar { api.contratarPlan(ContratarPlanRequest(planId)) }) {
            is Resultado.Ok -> {
                session.saveToken(r.valor.token)
                session.saveUser(name = r.valor.usuario.nombre, role = r.valor.usuario.rol)
                CurrentUserStore.set(r.valor.usuario)
                Resultado.Ok(r.valor.usuario)
            }
            is Resultado.Error -> Resultado.Error(r.mensaje, r.codigo)
        }

    /**
     * TODO: el API todavía no tiene endpoint de cancelación.
     * Hoy solo cambia el estado local; el servidor sigue dejando publicar hasta que venza el plan.
     */
    suspend fun cancelarPlan(): Resultado<UsuarioMeDto> {
        val actual = CurrentUserStore.user.value
            ?: return Resultado.Error("NO_USER")
        val actualizado = actual.copy(plan = null)
        CurrentUserStore.set(actualizado)
        return Resultado.Ok(actualizado)
    }

    private suspend fun <T> llamar(bloque: suspend () -> Response<T>): Resultado<T> = try {
        val respuesta = bloque()
        val cuerpo = respuesta.body()
        if (respuesta.isSuccessful && cuerpo != null) {
            Resultado.Ok(cuerpo)
        } else {
            val raw = respuesta.errorBody()?.string()
            Resultado.Error(
                mensaje = mensajeApi(raw) ?: "UNEXPECTED_ERROR",
                codigo = codigoApi(raw)
            )
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        Resultado.Error("NETWORK_ERROR")
    } catch (e: Exception) {
        Resultado.Error("UNEXPECTED_ERROR")
    }
}