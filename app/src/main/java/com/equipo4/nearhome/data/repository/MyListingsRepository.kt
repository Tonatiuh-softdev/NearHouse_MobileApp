package com.equipo4.nearhome.data.repository

import android.content.Context
import com.equipo4.nearhome.R
import com.equipo4.nearhome.data.local.SessionManager
import com.equipo4.nearhome.data.remote.ActivaDto
import com.equipo4.nearhome.data.remote.CODIGO_LIMITE_ANUNCIOS
import com.equipo4.nearhome.data.remote.CODIGO_PLAN_REQUERIDO
import com.equipo4.nearhome.data.remote.CupoDto
import com.equipo4.nearhome.data.remote.PublicacionDto
import com.equipo4.nearhome.data.remote.PublicacionesApi
import com.equipo4.nearhome.data.remote.codigoApi
import com.equipo4.nearhome.data.remote.mensajeApi
import com.equipo4.nearhome.di.NetworkModule
import com.equipo4.nearhome.ui.profile.seller.ListingStatus
import com.equipo4.nearhome.ui.profile.seller.MyListingItem
import com.equipo4.nearhome.util.AppStrings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

/** Mis anuncios: todos los del vendedor con sesión, con su estado de revisión y, si fue rechazado, el motivo. */
class MyListingsRepository(context: Context) {

    private val session = SessionManager(context.applicationContext)

    private val api: PublicacionesApi by lazy {
        NetworkModule.provideRetrofit(NetworkModule.provideOkHttpClient(session))
            .create(PublicacionesApi::class.java)
    }

    /** Anuncios activos y límite del plan: para saber si puede crear otro antes de abrir el formulario. */
    suspend fun cupo(): Result<CupoDto> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.cupo())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun load(): Result<List<MyListingItem>> = withContext(Dispatchers.IO) {
        try {
            val userId = session.getUserId()
            val items = api.misAnuncios().map { dto -> dto.toItem(userId) }
            Result.success(items)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Pausa o reactiva un anuncio publicado.
     * [activa] = false → pausar; true → reactivar (requiere plan vigente; el API responde PLAN_REQUERIDO si no,
     * y 403 si lo desactivó un administrador).
     */
    suspend fun setActiva(id: String, activa: Boolean): Result<MyListingItem> =
        withContext(Dispatchers.IO) {
            try {
                val userId = session.getUserId()
                val dto = api.cambiarActiva(id.toInt(), ActivaDto(activa))
                Result.success(dto.toItem(userId))
            } catch (e: CancellationException) {
                throw e
            } catch (e: HttpException) {
                val cuerpo = e.response()?.errorBody()?.string()
                val codigo = codigoApi(cuerpo)
                val mensaje = when {
                    codigo == CODIGO_PLAN_REQUERIDO -> AppStrings.get(R.string.plan_required_to_publish)
                    // Reactivar con el plan lleno: el API dice cuántos permite el plan
                    codigo == CODIGO_LIMITE_ANUNCIOS -> mensajeApi(cuerpo) ?: AppStrings.get(R.string.plan_limit_reached)
                    e.code() == 401 -> AppStrings.get(R.string.create_listing_error_session)
                    e.code() == 403 -> AppStrings.get(R.string.listing_reactivate_blocked_by_admin)
                    e.code() == 409 -> AppStrings.get(R.string.manage_listing_pause_only_published)
                    else -> AppStrings.get(R.string.create_listing_error_generic)
                }
                Result.failure(PublicacionException(mensaje, codigo))
            } catch (e: IOException) {
                Result.failure(PublicacionException(AppStrings.get(R.string.create_listing_error_network)))
            } catch (e: Exception) {
                Result.failure(PublicacionException(AppStrings.get(R.string.create_listing_error_generic)))
            }
        }

    /** Elimina un anuncio de forma permanente. Si falla, el mensaje del error ya viene listo para mostrarse. */
    suspend fun delete(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val respuesta = api.eliminar(id.toInt())
            // 204 No Content = borrado OK (el servidor no manda cuerpo)
            if (respuesta.isSuccessful || respuesta.code() == 204) {
                Result.success(Unit)
            } else {
                val mensaje = when (respuesta.code()) {
                    401 -> AppStrings.get(R.string.create_listing_error_session)
                    else -> AppStrings.get(R.string.create_listing_error_generic)
                }
                Result.failure(PublicacionException(mensaje))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            if (e.code() == 204) {
                Result.success(Unit)
            } else {
                val mensaje = when (e.code()) {
                    401 -> AppStrings.get(R.string.create_listing_error_session)
                    else -> AppStrings.get(R.string.create_listing_error_generic)
                }
                Result.failure(PublicacionException(mensaje))
            }
        } catch (e: IOException) {
            Result.failure(PublicacionException(AppStrings.get(R.string.create_listing_error_network)))
        } catch (e: Exception) {
            Result.failure(PublicacionException(AppStrings.get(R.string.create_listing_error_generic)))
        }
    }

    /** Convierte un anuncio del API en un ítem de Mis anuncios. */
    private fun PublicacionDto.toItem(userId: Int?): MyListingItem {
        val status = when (estado.uppercase()) {
            "PUBLICADA" -> ListingStatus.PUBLISHED
            "RECHAZADA" -> ListingStatus.REJECTED
            else -> ListingStatus.IN_REVIEW
        }
        return MyListingItem(
            // Son anuncios del propio usuario, aunque no se haya guardado su id en la sesión
            property = toProperty(userId).copy(isOwner = true),
            status = status,
            rejectionReason = if (status == ListingStatus.REJECTED) revision?.motivo else null,
            isActive = activa,
            // Lo desactivó un administrador: el vendedor no puede reactivarlo
            desactivadaPorAdmin = desactivadaPorAdmin,
            motivoDesactivacion = motivoDesactivacion,
            ocultaPorLimite = ocultaPorLimite,
            retiradaPorReporte = retiradaPorReporte,
            apelacionEstado = apelacionEstado,
            apelacionNota = apelacionNota
        )
    }
}