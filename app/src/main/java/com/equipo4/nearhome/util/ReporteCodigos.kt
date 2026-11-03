package com.equipo4.nearhome.util

import androidx.annotation.StringRes
import com.equipo4.nearhome.R

data class OpcionReporte(val codigo: String, @StringRes val texto: Int)

object ReporteCodigos {

    val categorias: List<OpcionReporte> = listOf(
        OpcionReporte("INFORMACION_FALSA", R.string.report_category_false_info),
        OpcionReporte("CONTENIDO_OFENSIVO", R.string.report_category_offensive),
        OpcionReporte("POSIBLE_FRAUDE", R.string.report_category_fraud)
    )

    private val motivos: Map<String, List<OpcionReporte>> = mapOf(
        "INFORMACION_FALSA" to listOf(
            OpcionReporte("FOTOS_NO_CORRESPONDEN", R.string.report_reason_photos_mismatch),
            OpcionReporte("PRECIO_FALSO", R.string.report_reason_wrong_price),
            OpcionReporte("DATOS_INCORRECTOS", R.string.report_reason_wrong_data),
            OpcionReporte("UBICACION_INCORRECTA", R.string.report_reason_wrong_location),
            OpcionReporte("PROPIEDAD_NO_EXISTE", R.string.report_reason_not_exist)
        ),
        "CONTENIDO_OFENSIVO" to listOf(
            OpcionReporte("LENGUAJE_OFENSIVO", R.string.report_reason_offensive_language),
            OpcionReporte("CONTENIDO_DISCRIMINATORIO", R.string.report_reason_discriminatory),
            OpcionReporte("CONTENIDO_SEXUAL", R.string.report_reason_sexual),
            OpcionReporte("CONTENIDO_VIOLENTO", R.string.report_reason_violent),
            OpcionReporte("OTRO", R.string.report_reason_other)
        ),
        "POSIBLE_FRAUDE" to listOf(
            OpcionReporte("PRECIO_FALSO", R.string.report_reason_wrong_price),
            OpcionReporte("DATOS_INCORRECTOS", R.string.report_reason_wrong_data),
            OpcionReporte("PUBLICADA_POR_VARIOS", R.string.report_reason_multiple_publishers),
            OpcionReporte("YA_NO_DISPONIBLE", R.string.report_reason_not_available),
            OpcionReporte("OTRO", R.string.report_reason_other)
        )
    )

    /** Motivos de una categoría. Úsalos en la pantalla y manda solo el código al API. */
    fun motivosDe(categoria: String): List<OpcionReporte> = motivos[categoria].orEmpty()
}