package com.equipo4.nearhome.domain.model

import androidx.annotation.StringRes

import com.equipo4.nearhome.util.AppStrings
import com.equipo4.nearhome.R

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
    val result: String?,
    val attendedDate: String?,
    val evidences: List<Int>,
    val targetId: String?
)