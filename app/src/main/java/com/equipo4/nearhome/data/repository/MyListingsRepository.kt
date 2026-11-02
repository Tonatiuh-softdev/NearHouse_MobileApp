package com.equipo4.nearhome.data.repository

import android.content.Context
import com.equipo4.nearhome.R
import com.equipo4.nearhome.data.local.SessionManager
import com.equipo4.nearhome.data.remote.PublicacionesApi
import com.equipo4.nearhome.di.NetworkModule
import com.equipo4.nearhome.ui.profile.seller.ListingStatus
import com.equipo4.nearhome.ui.profile.seller.MyListingItem
import com.equipo4.nearhome.util.AppStrings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import com.equipo4.nearhome.data.remote.ActivaDto

/** Mis anuncios: todos los del vendedor con sesión, con su estado de revisión y, si fue rechazado, el motivo. */
class MyListingsRepository(context: Context) {

    private val session = SessionManager(context.applicationContext)

    private val api: PublicacionesApi by lazy {
        NetworkModule.provideRetrofit(NetworkModule.provideOkHttpClient(session))
            .create(PublicacionesApi::class.java)
    }

    suspend fun load(): Result<List<MyListingItem>> = withContext(Dispatchers.IO) {
        try {
            val userId = session.getUserId()
            val items = api.misAnuncios().map { dto ->
                val status = when (dto.estado.uppercase()) {
                    "PUBLICADA" -> ListingStatus.PUBLISHED
                    "RECHAZADA" -> ListingStatus.REJECTED
                    else -> ListingStatus.IN_REVIEW
                }
                MyListingItem(
                    // Son anuncios del propio usuario, aunque no se haya guardado su id en la sesión
                    property = dto.toProperty(userId).copy(isOwner = true),
                    status = status,
                    rejectionReason = if (status == ListingStatus.REJECTED) dto.revision?.motivo else null,
                    isActive = dto.activa
                )
            }
            Result.success(items)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Pausa o reactiva un anuncio publicado.
     * [activa] = false → pausar; true → reactivar.
     */
    suspend fun setActiva(id: String, activa: Boolean): Result<MyListingItem> =
        withContext(Dispatchers.IO) {
            try {
                val userId = session.getUserId()
                val dto = api.cambiarActiva(id.toInt(), ActivaDto(activa))
                val status = when (dto.estado.uppercase()) {
                    "PUBLICADA" -> ListingStatus.PUBLISHED
                    "RECHAZADA" -> ListingStatus.REJECTED
                    else -> ListingStatus.IN_REVIEW
                }
                val item = MyListingItem(
                    property = dto.toProperty(userId).copy(isOwner = true),
                    status = status,
                    rejectionReason = if (status == ListingStatus.REJECTED) dto.revision?.motivo else null,
                    isActive = dto.activa
                )
                Result.success(item)
            } catch (e: CancellationException) {
                throw e
            } catch (e: HttpException) {
                val mensaje = when (e.code()) {
                    401 -> AppStrings.get(R.string.create_listing_error_session)
                    409 -> AppStrings.get(R.string.manage_listing_pause_only_published)
                    // o el mensaje del body si lo parseas
                    else -> AppStrings.get(R.string.create_listing_error_generic)
                }
                Result.failure(PublicacionException(mensaje))
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
}