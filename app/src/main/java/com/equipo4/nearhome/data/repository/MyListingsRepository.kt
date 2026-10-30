package com.equipo4.nearhome.data.repository

import android.content.Context
import com.equipo4.nearhome.data.local.SessionManager
import com.equipo4.nearhome.data.remote.PublicacionesApi
import com.equipo4.nearhome.di.NetworkModule
import com.equipo4.nearhome.ui.profile.seller.ListingStatus
import com.equipo4.nearhome.ui.profile.seller.MyListingItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

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
}