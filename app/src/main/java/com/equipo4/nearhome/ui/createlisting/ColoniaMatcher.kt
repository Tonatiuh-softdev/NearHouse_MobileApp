package com.equipo4.nearhome.ui.createlisting

import java.text.Normalizer
import java.util.Locale

object ColoniaMatcher {

    private val ignorar = setOf("manzanillo", "colima", "mexico", "mx", "mex", "col")
    private val prefijo = Regex("^(colonia|col|fraccionamiento|fracc|barrio|residencial)\\s+")

    private fun norm(s: String): String {
        val sinAcentos = Normalizer.normalize(s.lowercase(Locale.ROOT), Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
        return sinAcentos
            .replace(Regex("[^a-z0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
            .replace(prefijo, "")
    }

    /** Devuelve el nombre EXACTO del catálogo, o null si no hay coincidencia confiable. */
    fun match(hints: List<String>, catalogo: List<String>): String? {
        val cat = catalogo.map { it to norm(it) }
        val h = hints.map(::norm).filter { it.length >= 3 && it !in ignorar && !it.all(Char::isDigit) }

        // 1) Coincidencia exacta, respetando el orden de prioridad de los hints
        for (hint in h) cat.firstOrNull { it.second == hint }?.let { return it.first }

        // 2) Coincidencia parcial (el nombre del catálogo dentro del hint, o al revés)
        for (hint in h) {
            cat.firstOrNull { (_, n) ->
                n.length >= 4 && (hint.contains(n) || (hint.length >= 4 && n.contains(hint)))
            }?.let { return it.first }
        }
        return null
    }
}