package com.equipo4.nearhome.ui.publicacion.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.data.repository.Repositories
import com.equipo4.nearhome.domain.model.ListingType
import com.equipo4.nearhome.domain.model.Property
import com.equipo4.nearhome.domain.model.PropertyType
import com.equipo4.nearhome.domain.repository.PropiedadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.equipo4.nearhome.util.AppStrings
import com.equipo4.nearhome.R

class DetailPublicacionViewModel(
    private val repository: PropiedadRepository = Repositories.propiedades
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailPublicacionUiState>(DetailPublicacionUiState.Loading)
    val uiState: StateFlow<DetailPublicacionUiState> = _uiState.asStateFlow()

    fun cargarPublicacion(id: String) {
        viewModelScope.launch {
            _uiState.value = DetailPublicacionUiState.Loading
            try {
                val property = repository.getById(id)
                if (property == null) {
                    _uiState.value = DetailPublicacionUiState.Error(AppStrings.get(R.string.detail_error_not_found))
                } else {
                    _uiState.value = DetailPublicacionUiState.Success(property.toDetail())
                }
            } catch (e: Exception) {
                _uiState.value = DetailPublicacionUiState.Error(AppStrings.get(R.string.detail_error_load))
            }
        }
    }

    fun toggleGuardar() {
        val currentState = _uiState.value
        if (currentState is DetailPublicacionUiState.Success) {
            val pub = currentState.publicacion
            val nuevoEstadoGuardado = !pub.estaGuardado
            _uiState.value = DetailPublicacionUiState.Success(pub.copy(estaGuardado = nuevoEstadoGuardado))
            // Se guarda en el repositorio para que Lista, Mapa y Guardados lo reflejen.
            viewModelScope.launch { repository.setSaved(pub.id, nuevoEstadoGuardado) }
        }
    }
}

/**
 * Adapta una propiedad al modelo del detalle. Con datos del API usa la descripción y el contacto reales;
 * con los datos de prueba (sin descripción) conserva los textos de ejemplo.
 */
private fun Property.toDetail(): PublicacionDetail {
    val tipoLabel = when (type) {
        PropertyType.CASA -> AppStrings.get(R.string.search_filters_type_house)
        PropertyType.DEPARTAMENTO -> AppStrings.get(R.string.search_filters_type_apartment)
        PropertyType.TERRENO -> AppStrings.get(R.string.search_filters_type_land)
    }
    val esReal = description != null

    val descripcionFinal = if (esReal) {
        buildString {
            append(description)
            if (!rules.isNullOrBlank()) append("\n\n${AppStrings.get(R.string.create_listing_ad_rules)}: $rules")
        }
    } else {
        "Exclusivo inmueble de ${areaSqM}m² ubicado en una de las mejores zonas de Manzanillo. " +
                "Cuenta con una distribución ideal para disfrutar del confort y recibir invitados. " +
                "Cerca de comercios, escuelas y servicios. Agenda una visita y conócelo."
    }

    val columna1: List<String>
    val columna2: List<String>
    if (esReal) {
        val si = AppStrings.get(R.string.create_listing_yes)
        val no = AppStrings.get(R.string.create_listing_no)
        columna1 = buildList {
            if (jardinPatio) add(AppStrings.get(R.string.create_listing_garden))
            if (amueblado) add(AppStrings.get(R.string.create_listing_furnished))
            if (elevador) add(AppStrings.get(R.string.create_listing_elevator))
        }
        columna2 = buildList {
            piso?.let {
                val valor = if (it == 0) AppStrings.get(R.string.create_listing_ground_floor) else it.toString()
                add("${AppStrings.get(R.string.create_listing_floor)}: $valor")
            }
            niveles?.let { add("${AppStrings.get(R.string.create_listing_levels)}: $it") }
            if (listingType == ListingType.RENTA && type != PropertyType.TERRENO) {
                add("${AppStrings.get(R.string.create_listing_pets)}: ${if (mascotasPermitidas) si else no}")
            }
        }
    } else {
        columna1 = listOf("Alberca", "Terraza")
        columna2 = listOf("Estancia mínima en días: 1", "No se permiten mascotas")
    }

    return PublicacionDetail(
        id = id,
        titulo = title,
        descripcion = descripcionFinal,
        precio = price,
        esRenta = listingType == ListingType.RENTA,
        tipoPropiedad = tipoLabel,
        direccion = location,
        imagenes = images,
        metrosCuadrados = areaSqM,
        recamaras = bedrooms ?: 0,
        banos = bathrooms ?: 0,
        cocheras = garages ?: 0,
        caracteristicasColumna1 = columna1,
        caracteristicasColumna2 = columna2,
        esPropietario = isOwner,
        estaGuardado = isSaved,
        telefonoVendedor = if (esReal) whatsappNumber(sellerPhone) else "523121234567",
        emailVendedor = sellerEmail ?: "vendedor@ejemplo.com"
    )
}

/** WhatsApp pide el número con lada de país y solo dígitos: a los de 10 dígitos se les antepone 52. */
private fun whatsappNumber(raw: String?): String {
    val digits = raw.orEmpty().filter(Char::isDigit)
    return if (digits.length == 10) "52$digits" else digits
}