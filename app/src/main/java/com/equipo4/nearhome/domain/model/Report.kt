package com.equipo4.nearhome.domain.model

import androidx.annotation.StringRes
import com.equipo4.nearhome.R
import com.equipo4.nearhome.util.AppStrings

enum class ReportType(@StringRes val labelRes: Int) {
    PUBLICACION(R.string.type_publication),
    USUARIO(R.string.type_user);

    val label: String get() = AppStrings.get(labelRes)
}

enum class ReportStatus {
    PENDIENTE,
    ATENDIDO
}

data class Report(
    val id: String,
    val type: ReportType,
    val motive: String,
    val description: String,
    val date: String,
    val status: ReportStatus,
    /** Resultado ya traducido. Null si está pendiente. */
    val result: String?,
    val attendedDate: String?,
    /** Nota que escribió el administrador al resolver. */
    val note: String?,
    /** Ids de las evidencias. Solo viene en el detalle. */
    val evidences: List<Int>,
    /** Cantidad de evidencias. Viene también en la lista. */
    val evidenceCount: Int,
    /** Id de la publicación reportada. */
    val targetId: String?,
    /** Null = la cuenta que reportó ya no existe. */
    val reportedByName: String?,
    val reportedByEmail: String?
)