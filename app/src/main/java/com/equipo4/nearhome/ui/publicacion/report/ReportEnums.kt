package com.equipo4.nearhome.ui.publicacion.report

import androidx.annotation.StringRes
import com.equipo4.nearhome.R

enum class ReportCategory(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int
) {
    INFORMACION_FALSA(
        titleRes = R.string.report_category_false_info,
        descriptionRes = R.string.report_category_false_info_desc
    ),
    CONTENIDO_OFENSIVO(
        titleRes = R.string.report_category_offensive,
        descriptionRes = R.string.report_category_offensive_desc
    ),
    POSIBLE_FRAUDE(
        titleRes = R.string.report_category_fraud,
        descriptionRes = R.string.report_category_fraud_desc
    )
}

enum class ReportReason(
    val category: ReportCategory,
    @StringRes val titleRes: Int
) {
    // Información falsa
    FOTOS_NO_CORRESPONDEN(ReportCategory.INFORMACION_FALSA, R.string.report_reason_photos_mismatch),
    PRECIO_INCORRECTO(ReportCategory.INFORMACION_FALSA, R.string.report_reason_wrong_price),
    DATOS_INCORRECTOS(ReportCategory.INFORMACION_FALSA, R.string.report_reason_wrong_data),
    UBICACION_INCORRECTA(ReportCategory.INFORMACION_FALSA, R.string.report_reason_wrong_location),
    PROPIEDAD_NO_DISPONIBLE(ReportCategory.INFORMACION_FALSA, R.string.report_reason_not_available),
    OTRO_INFORMACION_FALSA(ReportCategory.INFORMACION_FALSA, R.string.report_reason_other),

    // Contenido ofensivo
    LENGUAJE_OFENSIVO(ReportCategory.CONTENIDO_OFENSIVO, R.string.report_reason_offensive_language),
    CONTENIDO_DISCRIMINATORIO(ReportCategory.CONTENIDO_OFENSIVO, R.string.report_reason_discriminatory),
    CONTENIDO_SEXUAL(ReportCategory.CONTENIDO_OFENSIVO, R.string.report_reason_sexual),
    CONTENIDO_VIOLENTO(ReportCategory.CONTENIDO_OFENSIVO, R.string.report_reason_violent),
    OTRO_CONTENIDO_OFENSIVO(ReportCategory.CONTENIDO_OFENSIVO, R.string.report_reason_other),

    // Posible fraude
    PRECIO_SOSPECHOSAMENTE_BAJO(ReportCategory.POSIBLE_FRAUDE, R.string.report_reason_suspicious_price),
    PROPIEDAD_NO_EXISTE(ReportCategory.POSIBLE_FRAUDE, R.string.report_reason_not_exist),
    PUBLICADA_POR_VARIOS(ReportCategory.POSIBLE_FRAUDE, R.string.report_reason_multiple_publishers),
    PIDEN_PAGO_ANTICIPADO(ReportCategory.POSIBLE_FRAUDE, R.string.report_reason_advance_payment),
    OTRO_POSIBLE_FRAUDE(ReportCategory.POSIBLE_FRAUDE, R.string.report_reason_other)
}

data class EvidenceFile(
    val uri: android.net.Uri,
    val isVideo: Boolean = false,
    val durationText: String? = null
)