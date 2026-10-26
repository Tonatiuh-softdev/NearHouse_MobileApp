package com.equipo4.nearhome.data.repository

import com.equipo4.nearhome.data.remote.ColoniaApi
import com.equipo4.nearhome.data.remote.ColoniaApiProvider
import com.equipo4.nearhome.data.remote.ColoniaDto
import com.equipo4.nearhome.domain.model.ListingType
import com.equipo4.nearhome.domain.model.PropertyType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.text.Collator
import java.util.Locale
import kotlin.coroutines.cancellation.CancellationException

/**
 * Catálogo de colonias para el filtro de Ubicación.
 * Solo regresa colonias que tienen al menos un anuncio publicado.
 */
class ColoniaRepository(private val api: ColoniaApi = ColoniaApiProvider.api) {

    /**
     * [listingType] null = venta y renta. [tipos] vacío = todos los tipos.
     * Con varios tipos seleccionados se hace una consulta por tipo y se unen los resultados.
     */
    suspend fun obtener(listingType: ListingType?, tipos: Set<PropertyType>): Result<List<String>> =
        try {
            val modalidad = when (listingType) {
                ListingType.VENTA -> "Venta"
                ListingType.RENTA -> "Renta"
                null -> null
            }
            val tiposConsulta: List<PropertyType?> = if (tipos.isEmpty()) listOf(null) else tipos.toList()

            val listas: List<List<ColoniaDto>> = coroutineScope {
                tiposConsulta.map { t -> async { api.listar(modalidad, t?.name) } }.awaitAll()
            }

            val nombres = listas.flatten()
                .distinctBy { it.id }
                .map { it.nombre }
                .sortedWith { a, b -> COLLATOR.compare(a, b) }
            Result.success(nombres)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }

    private companion object {
        val COLLATOR: Collator = Collator.getInstance(Locale.forLanguageTag("es-MX")).apply {
            strength = Collator.PRIMARY
        }
    }
}