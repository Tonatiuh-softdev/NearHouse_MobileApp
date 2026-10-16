package com.equipo4.nearhome.ui.home.filter

import com.equipo4.nearhome.domain.model.ListingType
import com.equipo4.nearhome.domain.model.Property
import com.equipo4.nearhome.domain.model.PropertyType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PropertyFilterState(
    val listingType: ListingType = ListingType.VENTA,
    val propertyType: PropertyType? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val selectedColonias: Set<String> = emptySet(),
    val bedrooms: Set<Int> = emptySet(),
    val bathrooms: Set<Int> = emptySet(),
    val garages: Set<Int> = emptySet(),
    val niveles: Set<Int> = emptySet(),
    val piso: Set<Int> = emptySet(),
    val jardinPatio: Boolean = false,
    val mascotasPermitidas: Boolean = false,
    val elevador: Boolean = false,
    val amueblado: Boolean = false,
    val minSuperficie: Double? = null,
    val maxSuperficie: Double? = null,
    val usoSuelo: String? = null
) {
    fun reset(): PropertyFilterState = PropertyFilterState(listingType = this.listingType)

    fun getSelectedCount(): Int {
        var count = 0
        if (propertyType != null) count++
        if (listingType == ListingType.RENTA) count++
        if (minPrice != null || maxPrice != null) count++
        if (selectedColonias.isNotEmpty()) count++
        if (bedrooms.isNotEmpty()) count++
        if (bathrooms.isNotEmpty()) count++
        if (garages.isNotEmpty()) count++
        if (niveles.isNotEmpty()) count++
        if (piso.isNotEmpty()) count++
        if (jardinPatio) count++
        if (mascotasPermitidas) count++
        if (elevador) count++
        if (amueblado) count++
        if (minSuperficie != null || maxSuperficie != null) count++
        if (usoSuelo != null) count++
        return count
    }
}

object PropertyFilterManager {
    private val _filterState = MutableStateFlow(PropertyFilterState())
    val filterState: StateFlow<PropertyFilterState> = _filterState.asStateFlow()

    fun updateFilters(newFilter: PropertyFilterState) {
        _filterState.value = newFilter
    }

    fun resetFilters() {
        _filterState.value = PropertyFilterState()
    }
}

val ALL_COLONIAS = listOf(
    "Las Hadas",
    "Club Santiago",
    "Miramar",
    "Salahua",
    "Las Brisas",
    "Centro Histórico",
    "San Pedrito",
    "Tapeixtles",
    "Valle de las Garzas",
    "Soleares",
    "La Joya",
    "Nuevo Salahua",
    "Campos",
    "El Colomo"
)

private fun matchMultiSelect(propVal: Int?, selected: Set<Int>): Boolean {
    if (selected.isEmpty()) return true
    val v = propVal ?: 0
    return selected.any { sel -> if (sel == 5) v >= 5 else v == sel }
}

fun Property.matchesFilter(filter: PropertyFilterState): Boolean {
    // 1. Listing Type
    if (this.listingType != filter.listingType) return false

    // 2. Property Type
    if (filter.propertyType != null && this.type != filter.propertyType) return false

    // 3. Price
    if (filter.minPrice != null && this.price < filter.minPrice) return false
    if (filter.maxPrice != null && this.price > filter.maxPrice) return false

    // 4. Colonias
    if (filter.selectedColonias.isNotEmpty() && this.colonia !in filter.selectedColonias) return false

    // 5. Characteristics based on Property Type
    when (this.type) {
        PropertyType.CASA -> {
            if (!matchMultiSelect(this.bedrooms, filter.bedrooms)) return false
            if (!matchMultiSelect(this.bathrooms, filter.bathrooms)) return false
            if (!matchMultiSelect(this.garages, filter.garages)) return false
            if (!matchMultiSelect(this.niveles, filter.niveles)) return false
            if (filter.jardinPatio && !this.jardinPatio) return false
            if (filter.listingType == ListingType.RENTA && filter.mascotasPermitidas && !this.mascotasPermitidas) return false
        }
        PropertyType.DEPARTAMENTO -> {
            if (!matchMultiSelect(this.bedrooms, filter.bedrooms)) return false
            if (!matchMultiSelect(this.bathrooms, filter.bathrooms)) return false
            if (!matchMultiSelect(this.garages, filter.garages)) return false
            if (!matchMultiSelect(this.piso, filter.piso)) return false
            if (filter.amueblado && !this.amueblado) return false
            if (filter.listingType == ListingType.RENTA) {
                if (filter.mascotasPermitidas && !this.mascotasPermitidas) return false
                if (filter.elevador && !this.elevador) return false
            }
        }
        PropertyType.TERRENO -> {
            val superficie = this.superficieSqM ?: this.areaSqM.toDouble()
            if (filter.minSuperficie != null && superficie < filter.minSuperficie) return false
            if (filter.maxSuperficie != null && superficie > filter.maxSuperficie) return false
            if (filter.usoSuelo != null && !this.usoSuelo.equals(filter.usoSuelo, ignoreCase = true)) return false
        }
    }

    return true
}

fun PropertyFilterState.buildSummaryText(): String {
    val modeStr = if (listingType == ListingType.VENTA) "en venta" else "en renta"
    val typeStr = when (propertyType) {
        PropertyType.CASA -> "Casa"
        PropertyType.DEPARTAMENTO -> "Departamento"
        PropertyType.TERRENO -> "Terreno"
        null -> "Inmuebles"
    }
    val typeForm = if (propertyType == null) "Inmuebles $modeStr" else "$typeStr $modeStr"

    val details = mutableListOf<String>()
    if (selectedColonias.isNotEmpty()) {
        if (selectedColonias.size == 1) {
            details.add("en ${selectedColonias.first()}")
        } else {
            details.add("en ${selectedColonias.size} colonias")
        }
    }
    if (amueblado) details.add("amueblado")
    if (jardinPatio) details.add("con jardín/patio")
    if (elevador) details.add("con elevador")
    if (mascotasPermitidas) details.add("pet-friendly")
    if (usoSuelo != null) details.add("uso $usoSuelo")

    return if (details.isEmpty()) typeForm else "$typeForm ${details.joinToString(", ")}"
}
