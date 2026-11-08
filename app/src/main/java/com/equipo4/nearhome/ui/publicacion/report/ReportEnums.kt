package com.equipo4.nearhome.ui.publicacion.report

import android.net.Uri
import androidx.annotation.StringRes
import com.equipo4.nearhome.R

/** El nombre de cada categoría es el código que recibe el API. */
enum class ReportCategory(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int
) {
    INFORMACION_FALSA(R.string.report_category_false_info, R.string.report_category_false_info_desc),
    CONTENIDO_OFENSIVO(R.string.report_category_offensive, R.string.report_category_offensive_desc),
    POSIBLE_FRAUDE(R.string.report_category_fraud, R.string.report_category_fraud_desc)
}

/**
 * [codigo] es lo que se manda al API. El texto sale de strings.xml según el idioma.
 * El mismo código puede estar en dos categorías (PRECIO_FALSO), por eso cada motivo es una constante distinta.
 */
enum class ReportReason(
    val category: ReportCategory,
    val codigo: String,
    @StringRes val titleRes: Int
) {
    // Información falsa (5 motivos, igual que el API)
    FOTOS_NO_CORRESPONDEN(ReportCategory.INFORMACION_FALSA, "FOTOS_NO_CORRESPONDEN", R.string.report_reason_photos_mismatch),
    INFO_PRECIO_FALSO(ReportCategory.INFORMACION_FALSA, "PRECIO_FALSO", R.string.report_reason_wrong_price),
    INFO_DATOS_INCORRECTOS(ReportCategory.INFORMACION_FALSA, "DATOS_INCORRECTOS", R.string.report_reason_wrong_data),
    UBICACION_INCORRECTA(ReportCategory.INFORMACION_FALSA, "UBICACION_INCORRECTA", R.string.report_reason_wrong_location),
    PROPIEDAD_NO_EXISTE(ReportCategory.INFORMACION_FALSA, "PROPIEDAD_NO_EXISTE", R.string.report_reason_not_exist),

    // Contenido ofensivo (5 motivos)
    LENGUAJE_OFENSIVO(ReportCategory.CONTENIDO_OFENSIVO, "LENGUAJE_OFENSIVO", R.string.report_reason_offensive_language),
    CONTENIDO_DISCRIMINATORIO(ReportCategory.CONTENIDO_OFENSIVO, "CONTENIDO_DISCRIMINATORIO", R.string.report_reason_discriminatory),
    CONTENIDO_SEXUAL(ReportCategory.CONTENIDO_OFENSIVO, "CONTENIDO_SEXUAL", R.string.report_reason_sexual),
    CONTENIDO_VIOLENTO(ReportCategory.CONTENIDO_OFENSIVO, "CONTENIDO_VIOLENTO", R.string.report_reason_violent),
    OFENSIVO_OTRO(ReportCategory.CONTENIDO_OFENSIVO, "OTRO", R.string.report_reason_other),

    // Posible fraude (5 motivos)
    FRAUDE_PRECIO_FALSO(ReportCategory.POSIBLE_FRAUDE, "PRECIO_FALSO", R.string.report_reason_wrong_price),
    FRAUDE_DATOS_INCORRECTOS(ReportCategory.POSIBLE_FRAUDE, "DATOS_INCORRECTOS", R.string.report_reason_wrong_data),
    PUBLICADA_POR_VARIOS(ReportCategory.POSIBLE_FRAUDE, "PUBLICADA_POR_VARIOS", R.string.report_reason_multiple_publishers),
    YA_NO_DISPONIBLE(ReportCategory.POSIBLE_FRAUDE, "YA_NO_DISPONIBLE", R.string.report_reason_not_available),
    FRAUDE_OTRO(ReportCategory.POSIBLE_FRAUDE, "OTRO", R.string.report_reason_other)
}

/** Solo imágenes. La compresión se hace al enviar. */
data class EvidenceFile(val uri: Uri)