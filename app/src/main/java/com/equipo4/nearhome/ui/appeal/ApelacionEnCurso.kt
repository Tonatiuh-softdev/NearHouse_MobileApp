package com.equipo4.nearhome.ui.appeal

import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale

/** Qué decisión se va a apelar. Se guarda aquí antes de abrir la pantalla "Apelar decisión". */
sealed interface ObjetivoApelacion {
    /** Anuncio retirado por un reporte. [motivo] es lo que escribió el administrador al retirarlo. */
    data class Publicacion(val id: Int, val titulo: String, val motivo: String?) : ObjetivoApelacion

    /** Cuenta suspendida. [token] lo da el inicio de sesión (dura 30 minutos). */
    data class Suspension(val token: String, val motivo: String, val hasta: String?) : ObjetivoApelacion
}

/**
 * La apelación que se está escribiendo. Solo vive en memoria: el comprobante de una suspensión
 * no se pasa en la ruta de navegación ni se guarda en el teléfono.
 */
object ApelacionEnCurso {
    var objetivo: ObjetivoApelacion? = null
        private set

    fun iniciar(nuevo: ObjetivoApelacion) {
        objetivo = nuevo
    }

    fun limpiar() {
        objetivo = null
    }
}

/**
 * Lo que responde el API cuando una cuenta suspendida intenta iniciar sesión (403, codigo CUENTA_SUSPENDIDA).
 * [hasta]: fecha "yyyy-MM-dd" o null si es indefinida. [token]: para apelar; null si ya apeló esta suspensión.
 * [apelacionEstado]: PENDIENTE o RECHAZADA si ya apeló.
 */
data class CuentaSuspendida(
    val motivo: String,
    val hasta: String?,
    val token: String?,
    val apelacionEstado: String?,
    val apelacionNota: String?
) {
    val puedeApelar: Boolean get() = token != null

    /** La fecha de término para mostrar ("21 nov 2026"), o null si es indefinida. */
    fun hastaTexto(locale: Locale): String? = hasta?.let {
        try {
            val dia = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(it)
            if (dia == null) it else SimpleDateFormat("d MMM yyyy", locale).format(dia)
        } catch (e: Exception) {
            it
        }
    }

    companion object {
        /** Null si el error no es de cuenta suspendida. */
        fun desdeError(cuerpo: String?): CuentaSuspendida? {
            val json = try {
                JSONObject(cuerpo ?: return null)
            } catch (e: Exception) {
                return null
            }
            if (json.optString("codigo") != "CUENTA_SUSPENDIDA") return null
            fun texto(clave: String): String? =
                if (json.isNull(clave)) null else json.optString(clave).takeIf { it.isNotBlank() }
            return CuentaSuspendida(
                motivo = texto("motivo").orEmpty(),
                hasta = texto("hasta"),
                token = texto("apelacionToken"),
                apelacionEstado = texto("apelacionEstado"),
                apelacionNota = texto("apelacionNota")
            )
        }
    }
}
