package com.equipo4.nearhome.data.remote

import org.json.JSONObject

/** Lee el campo "error" del cuerpo de respuesta del API, si existe. */
fun mensajeApi(raw: String?): String? = try {
    if (raw.isNullOrBlank()) null
    else {
        val json = JSONObject(raw)
        json.optString("error").takeIf { it.isNotBlank() }
            ?: json.optString("mensaje").takeIf { it.isNotBlank() }
            ?: json.optString("message").takeIf { it.isNotBlank() }
            ?: json.optString("detail").takeIf { it.isNotBlank() }
            ?: raw.takeIf { it.length < 100 }
    }
} catch (e: Exception) {
    raw?.takeIf { it.length < 100 }
}

/** Lee el campo "codigo" del cuerpo de respuesta (por ejemplo PLAN_REQUERIDO). */
fun codigoApi(raw: String?): String? = try {
    if (raw.isNullOrBlank()) null
    else JSONObject(raw).optString("codigo").takeIf { it.isNotBlank() }
} catch (e: Exception) {
    null
}

/** Código que usa la app para "necesita plan". Lo compara el ViewModel de crear, editar y reactivar. */
const val CODIGO_PLAN_REQUERIDO = "PLAN_REQUERIDO"

/** El plan ya no tiene lugar para otro anuncio activo (Básico: 1, Profesional: 3). */
const val CODIGO_LIMITE_ANUNCIOS = "LIMITE_ANUNCIOS"