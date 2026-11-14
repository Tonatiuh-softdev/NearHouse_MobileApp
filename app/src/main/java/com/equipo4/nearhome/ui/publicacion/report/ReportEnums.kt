package com.equipo4.nearhome.ui.publicacion.report

import android.net.Uri
import androidx.annotation.StringRes
import com.equipo4.nearhome.R
import com.equipo4.nearhome.domain.model.ReportType

/**
 * El nombre de cada categoría es el código que recibe el API.
 * [tipo] dice si es para reportar una publicación o a un usuario (cada pantalla muestra solo las suyas).
 */
enum class ReportCategory(
    val tipo: ReportType,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int
) {
    // Publicaciones
    INFORMACION_FALSA(ReportType.PUBLICACION, R.string.report_category_false_info, R.string.report_category_false_info_desc),
    CONTENIDO_OFENSIVO(ReportType.PUBLICACION, R.string.report_category_offensive, R.string.report_category_offensive_desc),
    POSIBLE_FRAUDE(ReportType.PUBLICACION, R.string.report_category_fraud, R.string.report_category_fraud_desc),

    // Usuarios (igual que la migración 15 del API)
    PERFIL_FALSO(ReportType.USUARIO, R.string.report_category_fake_profile, R.string.report_category_fake_profile_desc),
    COMPORTAMIENTO_INAPROPIADO(ReportType.USUARIO, R.string.report_category_behavior, R.string.report_category_behavior_desc),
    ESTAFA(ReportType.USUARIO, R.string.report_category_scam, R.string.report_category_scam_desc)
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
    FRAUDE_OTRO(ReportCategory.POSIBLE_FRAUDE, "OTRO", R.string.report_reason_other),

    // ---------- Usuarios ----------

    // Perfil falso (4 motivos)
    PERFIL_SUPLANTACION(ReportCategory.PERFIL_FALSO, "SUPLANTACION", R.string.report_reason_impersonation),
    PERFIL_DATOS_FALSOS(ReportCategory.PERFIL_FALSO, "DATOS_FALSOS", R.string.report_reason_fake_data),
    PERFIL_FOTO_INAPROPIADA(ReportCategory.PERFIL_FALSO, "FOTO_INAPROPIADA", R.string.report_reason_inappropriate_photo),
    PERFIL_OTRO(ReportCategory.PERFIL_FALSO, "OTRO", R.string.report_reason_other),

    // Comportamiento inapropiado (5 motivos)
    CONDUCTA_ACOSO(ReportCategory.COMPORTAMIENTO_INAPROPIADO, "ACOSO", R.string.report_reason_harassment),
    CONDUCTA_LENGUAJE_OFENSIVO(ReportCategory.COMPORTAMIENTO_INAPROPIADO, "LENGUAJE_OFENSIVO", R.string.report_reason_offensive_language),
    CONDUCTA_AMENAZAS(ReportCategory.COMPORTAMIENTO_INAPROPIADO, "AMENAZAS", R.string.report_reason_threats),
    CONDUCTA_DISCRIMINACION(ReportCategory.COMPORTAMIENTO_INAPROPIADO, "CONTENIDO_DISCRIMINATORIO", R.string.report_reason_discrimination),
    CONDUCTA_OTRO(ReportCategory.COMPORTAMIENTO_INAPROPIADO, "OTRO", R.string.report_reason_other),

    // Posible estafa (4 motivos)
    ESTAFA_PAGO_ANTICIPADO(ReportCategory.ESTAFA, "PIDE_PAGO_ANTICIPADO", R.string.report_reason_user_advance_payment),
    ESTAFA_ANUNCIOS_FALSOS(ReportCategory.ESTAFA, "ANUNCIOS_FALSOS", R.string.report_reason_fake_listings),
    ESTAFA_NO_ES_EL_DUENO(ReportCategory.ESTAFA, "NO_ES_EL_DUENO", R.string.report_reason_not_owner),
    ESTAFA_OTRO(ReportCategory.ESTAFA, "OTRO", R.string.report_reason_other)
}

/** Solo imágenes. La compresión se hace al enviar. */
data class EvidenceFile(val uri: Uri)