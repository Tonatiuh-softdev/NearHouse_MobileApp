package com.equipo4.nearhome.ui.home.filter

import com.equipo4.nearhome.domain.model.ListingType
import com.equipo4.nearhome.domain.model.Property
import com.equipo4.nearhome.domain.model.PropertyType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

import com.equipo4.nearhome.util.AppStrings
import com.equipo4.nearhome.R

data class PropertyFilterState(
    val listingType: ListingType? = null,
    val propertyTypes: Set<PropertyType> = emptySet(),
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
    fun reset(): PropertyFilterState = PropertyFilterState(
        listingType = null,
        propertyTypes = emptySet()
    )

    fun getSelectedCount(): Int {
        var count = 0
        if (propertyTypes.isNotEmpty()) count++
        if (listingType != null) count++
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
    if (filter.listingType != null && this.listingType != filter.listingType) return false

    // 2. Property Types
    if (filter.propertyTypes.isNotEmpty() && this.type !in filter.propertyTypes) return false

    // 3. Price
    if (filter.minPrice != null && this.price < filter.minPrice) return false
    if (filter.maxPrice != null && this.price > filter.maxPrice) return false

    // 4. Colonias
    if (filter.selectedColonias.isNotEmpty() && this.colonia !in filter.selectedColonias) return false

    // 5. Characteristics based on Property Type
    when (this.type) {
        PropertyType.CASA -> {
            if (filter.bedrooms.isNotEmpty() && !matchMultiSelect(this.bedrooms, filter.bedrooms)) return false
            if (filter.bathrooms.isNotEmpty() && !matchMultiSelect(this.bathrooms, filter.bathrooms)) return false
            if (filter.garages.isNotEmpty() && !matchMultiSelect(this.garages, filter.garages)) return false
            if (filter.niveles.isNotEmpty() && !matchMultiSelect(this.niveles, filter.niveles)) return false
            if (filter.jardinPatio && !this.jardinPatio) return false
            if (this.listingType == ListingType.RENTA && filter.mascotasPermitidas && !this.mascotasPermitidas) return false
        }
        PropertyType.DEPARTAMENTO -> {
            if (filter.bedrooms.isNotEmpty() && !matchMultiSelect(this.bedrooms, filter.bedrooms)) return false
            if (filter.bathrooms.isNotEmpty() && !matchMultiSelect(this.bathrooms, filter.bathrooms)) return false
            if (filter.garages.isNotEmpty() && !matchMultiSelect(this.garages, filter.garages)) return false
            if (filter.piso.isNotEmpty() && !matchMultiSelect(this.piso, filter.piso)) return false
            if (filter.amueblado && !this.amueblado) return false
            if (this.listingType == ListingType.RENTA) {
                if (filter.mascotasPermitidas && !this.mascotasPermitidas) return false
                if (filter.elevador && !this.elevador) return false
            }
        }
        PropertyType.TERRENO -> {
            val superficie = this.superficieSqM ?: this.areaSqM.toDouble()
            if (filter.minSuperficie != null && superficie < filter.minSuperficie) return false
            if (filter.maxSuperficie != null && superficie > filter.maxSuperficie) return false
            if (filter.usoSuelo != null && !this.usoSuelo.isNullOrEmpty() && !this.usoSuelo.equals(filter.usoSuelo, ignoreCase = true)) return false
        }
    }

    return true
}

fun PropertyFilterState.buildSummaryText(): String {
    val modeStr = when (listingType) {
        ListingType.VENTA -> AppStrings.get(R.string.filter_summary_for_sale)
        ListingType.RENTA -> AppStrings.get(R.string.filter_summary_for_rent)
        null -> ""
    }
    val typeStr = when {
        propertyTypes.size == 1 -> when (propertyTypes.first()) {
            PropertyType.CASA -> AppStrings.get(R.string.search_filters_type_house)
            PropertyType.DEPARTAMENTO -> AppStrings.get(R.string.search_filters_type_apartment)
            PropertyType.TERRENO -> AppStrings.get(R.string.search_filters_type_land)
        }
        propertyTypes.size > 1 -> "${propertyTypes.size} tipos"
        else -> AppStrings.get(R.string.filter_summary_properties)
    }
    val typeForm = "$typeStr $modeStr".trim()

    val details = mutableListOf<String>()
    if (selectedColonias.isNotEmpty()) {
        if (selectedColonias.size == 1) {
            details.add(AppStrings.get(R.string.filter_summary_in_place, selectedColonias.first()))
        } else {
            details.add(AppStrings.get(R.string.filter_summary_in_n_places, selectedColonias.size))
        }
    }
    if (amueblado) details.add(AppStrings.get(R.string.filter_summary_furnished))
    if (jardinPatio) details.add(AppStrings.get(R.string.filter_summary_garden))
    if (elevador) details.add(AppStrings.get(R.string.filter_summary_elevator))
    if (mascotasPermitidas) details.add(AppStrings.get(R.string.filter_summary_pet_friendly))
    if (usoSuelo != null) details.add(AppStrings.get(R.string.filter_summary_land_use, usoSuelo))

    return if (details.isEmpty()) typeForm else "$typeForm ${details.joinToString(", ")}"
}
