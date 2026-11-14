package com.equipo4.nearhome.domain.model

/**
 * Los tipos de notificación definidos en el backend.
 * `apiName` es el valor que manda/recibe el servidor en el campo `tipo`.
 */
enum class TipoNotificacion(val apiName: String) {
    BIENVENIDA("BIENVENIDA"),
    PLAN_CONTRATADO("PLAN_CONTRATADO"),
    PUBLICACION_EN_REVISION("PUBLICACION_EN_REVISION"),
    PUBLICACION_APROBADA("PUBLICACION_APROBADA"),
    PUBLICACION_RECHAZADA("PUBLICACION_RECHAZADA"),
    REPORTE_ENVIADO("REPORTE_ENVIADO"),
    REPORTE_ATENDIDO("REPORTE_ATENDIDO"),
    REPORTE_DESCARTADO("REPORTE_DESCARTADO"),
    PUBLICACION_RETIRADA("PUBLICACION_RETIRADA"),
    APELACION_ENVIADA("APELACION_ENVIADA"),
    APELACION_ACEPTADA("APELACION_ACEPTADA"),
    APELACION_RECHAZADA("APELACION_RECHAZADA");

    companion object {
        fun fromApi(valor: String?): TipoNotificacion? =
            entries.firstOrNull { it.apiName == valor }
    }
}

data class Notificacion(
    val id: String,
    val tipo: TipoNotificacion,
    val timestampMs: Long,
    val leida: Boolean = false,
    val imagenUrl: String? = null,
    val publicacionId: String? = null,
    val reporteId: String? = null,
    // ---- Datos del JSON (`datos` en la API) ----
    val tituloPropiedad: String? = null,   // {titulo} guardado al momento del aviso
    val motivo: String? = null,            // motivo del rechazo o del retiro
    val nombreUsuario: String? = null,     // {nombre}
    val plan: String? = null,              // {plan}
    val fechaPlanIso: String? = null,      // {fecha} en ISO, se formatea en la app
    val usuarioReportadoId: String? = null, // {usuarioReportado}: reportes a usuarios, para abrir su perfil
    val apelacion: String? = null           // {apelacion}: PUBLICACION o SUSPENSION
) {
    /** La apelación es de una suspensión de cuenta (no de una publicación). */
    val esApelacionDeCuenta: Boolean get() = apelacion == "SUSPENSION"
}