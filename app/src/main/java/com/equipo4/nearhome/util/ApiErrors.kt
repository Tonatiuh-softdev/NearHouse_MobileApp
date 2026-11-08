package com.equipo4.nearhome.util

import org.json.JSONObject

/**
 * Saca el mensaje para el usuario de un error de la API.
 *
 * La API responde {"error": "...", "detalles": ["..."]}. Si hay detalles, esos son los más
 * útiles (por ejemplo "El código es incorrecto. Te quedan 4 intentos."); si no, usa "error"
 * (por ejemplo "Espera 40 segundos antes de pedir otro código.").
 */
fun mensajeDeErrorApi(cuerpo: String?, porDefecto: String): String {
    if (cuerpo.isNullOrBlank()) return porDefecto
    return try {
        val json = JSONObject(cuerpo)
        val arreglo = json.optJSONArray("detalles")
        val detalles = if (arreglo == null) {
            emptyList()
        } else {
            (0 until arreglo.length()).map { arreglo.optString(it) }.filter { it.isNotBlank() }
        }
        val error = json.optString("error")
        when {
            detalles.isNotEmpty() -> detalles.joinToString("\n")
            error.isNotBlank() -> error
            else -> porDefecto
        }
    } catch (e: Exception) {
        porDefecto
    }
}
