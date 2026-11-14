package com.equipo4.nearhome.domain.model

import androidx.annotation.StringRes
import com.equipo4.nearhome.R
import com.equipo4.nearhome.util.AppStrings

/** Qué decisión se apela: el retiro de un anuncio por un reporte o la suspensión de la cuenta. */
enum class AppealType(@StringRes val labelRes: Int) {
    PUBLICACION(R.string.appeal_type_listing),
    SUSPENSION(R.string.appeal_type_account);

    val label: String get() = AppStrings.get(labelRes)
}

/** ACEPTADA = se revirtió la decisión. RECHAZADA = la decisión se mantiene. */
enum class AppealStatus { PENDIENTE, ACEPTADA, RECHAZADA }

data class Appeal(
    val id: String,
    val type: AppealType,
    /** Lo que escribió quien apela. En la lista viene recortado. */
    val description: String,
    val date: String,
    val status: AppealStatus,
    /** Ids de las imágenes. Solo viene en el detalle. */
    val evidences: List<Int>,
    val evidenceCount: Int,
    val appellantId: Int,
    val appellantName: String,
    val appellantEmail: String,
    val appellantPhotoUrl: String?,
    /** Solo en PUBLICACION. */
    val publicationId: String?,
    val publicationTitle: String?,
    // ---- La decisión apelada (solo en el detalle) ----
    val measureReason: String? = null,
    val measureDate: String? = null,
    val measureById: Int? = null,
    val measureByName: String? = null,
    /** Solo en SUSPENSION y si sigue vigente: fecha de término, o null si es indefinida. */
    val suspensionUntil: String? = null,
    val suspensionActive: Boolean = false,
    // ---- Resolución ----
    val note: String? = null,
    val resolvedBy: String? = null,
    val resolvedDate: String? = null
)
