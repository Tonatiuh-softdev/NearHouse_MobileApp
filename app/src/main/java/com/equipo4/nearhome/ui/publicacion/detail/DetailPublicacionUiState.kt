package com.equipo4.nearhome.ui.publicacion.detail

import com.equipo4.nearhome.domain.model.PropertyType

data class PublicacionDetail(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val precio: Double,
    val esRenta: Boolean,
    val tipoPropiedad: String,
    val tipo: PropertyType,
    val direccion: String,
    val latitud: Double?,
    val longitud: Double?,
    val imagenes: List<Any>,
    val metrosCuadrados: Int,
    val recamaras: Int,
    val banos: Int,
    val cocheras: Int,
    val caracteristicasColumna1: List<String>,
    val caracteristicasColumna2: List<String>,
    val esPropietario: Boolean = false,
    val estaGuardado: Boolean = false,
    val sellerId: Int?,
    val sellerName: String,
    val telefonoVendedor: String,
    val emailVendedor: String,
    /** Reglas del inmueble. Solo existen en renta; en venta siempre es null. */
    val reglas: String? = null
)

sealed interface DetailPublicacionUiState {
    object Loading : DetailPublicacionUiState
    data class Error(val mensaje: String) : DetailPublicacionUiState
    data class Success(val publicacion: PublicacionDetail) : DetailPublicacionUiState
}