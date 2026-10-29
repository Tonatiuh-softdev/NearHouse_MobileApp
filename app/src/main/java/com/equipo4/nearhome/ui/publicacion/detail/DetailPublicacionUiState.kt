package com.equipo4.nearhome.ui.publicacion.detail

data class PublicacionDetail(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val precio: Double,
    val esRenta: Boolean,
    val tipoPropiedad: String,
    val direccion: String,
    val imagenes: List<Any>, // URLs del API (o R.drawable en los datos de prueba)
    val metrosCuadrados: Int,
    val recamaras: Int,
    val banos: Int,
    val cocheras: Int,
    val caracteristicasColumna1: List<String>,
    val caracteristicasColumna2: List<String>,
    val esPropietario: Boolean = false,
    val estaGuardado: Boolean = false,
    val telefonoVendedor: String,
    val emailVendedor: String
)

sealed interface DetailPublicacionUiState {
    object Loading : DetailPublicacionUiState
    data class Error(val mensaje: String) : DetailPublicacionUiState
    data class Success(val publicacion: PublicacionDetail) : DetailPublicacionUiState
}