package com.equipo4.nearhome.domain.model

enum class ReportType(val label: String) {
    PUBLICACION("Publicación"),
    USUARIO("Usuario")
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
