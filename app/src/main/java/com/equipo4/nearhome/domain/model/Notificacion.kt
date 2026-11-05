package com.equipo4.nearhome.domain.model

enum class TipoNotificacion {
    EN_REVISION,
    APROBADA,
    RECHAZADA,
    PAGO,
    BIENVENIDA,
    NOVEDADES_BUSQUEDA,
    ENCONTRAMOS_ALGO,
    RECIEN_PUBLICADAS
}

data class Notificacion(
    val id: String,
    val tipo: TipoNotificacion,
    val timestampMs: Long,
    val leida: Boolean = false,
    val imagenUrl: String? = null,
    val publicacionId: String? = null,
    // Argumentos para formatear strings de forma dinámica sin concatenar
    val tituloPropiedad: String? = null,
    val motivoRechazo: String? = null,
    val nombreUsuario: String? = null,
    val nombrePlan: String? = null,
    val criterioBusqueda: String? = null
)