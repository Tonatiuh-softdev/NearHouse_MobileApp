package com.equipo4.nearhome.ui.createlisting

import java.text.Normalizer
import java.util.Locale

/**
 * Saca el nombre de la colonia de lo que devuelve el Geocoder (ya no se compara con una lista fija:
 * si la colonia no existe en la base, el API la crea sola).
 *
 * 1) Manda lo que el geocoder marca como colonia (subLocality).
 * 2) Si no trae ninguna, se prueba cada segmento de la dirección y se queda con el primero
 *    que parezca una colonia (sin números, sin calle, sin ciudad ni estado).
 */
object ColoniaDetector {

    private val ignorar = setOf("manzanillo", "colima", "mexico", "mx", "mex", "col", "colonia")
    private val prefijoColonia =
        Regex("^(colonia|col\\.?|fraccionamiento|fracc\\.?|barrio|residencial)\\s+", RegexOption.IGNORE_CASE)
    private val pareceCalle = Regex(
        "^(av\\.?|avenida|calle|c\\.|blvd\\.?|boulevard|carretera|carr\\.?|calzada|calz\\.?|priv\\.?|privada|" +
                "andador|prolongaci[oó]n|camino|libramiento|perif[eé]rico|cerrada|callej[oó]n)(\\s|$)",
        RegexOption.IGNORE_CASE
    )

    fun detect(address: GeoAddress): String? {
        limpiar(address.colonia)?.takeIf { sinAcentos(it) !in ignorar }?.let { return it }
        return address.hints.firstNotNullOfOrNull { candidato(it, address.street) }
    }

    private fun candidato(texto: String, calle: String): String? {
        val nombre = limpiar(texto) ?: return null
        if (nombre.any(Char::isDigit)) return null
        if (pareceCalle.containsMatchIn(nombre)) return null
        if (nombre.equals(calle.trim(), ignoreCase = true)) return null
        if (sinAcentos(nombre) in ignorar) return null
        return nombre
    }

    /** Quita "Col."/"Colonia" del inicio y espacios de más. Null si queda vacío o demasiado corto/largo. */
    private fun limpiar(texto: String): String? {
        val limpio = texto.trim().replace(prefijoColonia, "").replace(Regex("\\s+"), " ").trim()
        return limpio.takeIf { it.length in 3..60 }
    }

    private fun sinAcentos(s: String): String =
        Normalizer.normalize(s.lowercase(Locale.ROOT), Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "")
}