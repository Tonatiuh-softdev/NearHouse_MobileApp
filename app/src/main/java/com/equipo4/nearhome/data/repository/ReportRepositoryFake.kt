package com.equipo4.nearhome.data.repository

import com.equipo4.nearhome.R
import com.equipo4.nearhome.domain.model.Report
import com.equipo4.nearhome.domain.model.ReportStatus
import com.equipo4.nearhome.domain.model.ReportType
import com.equipo4.nearhome.domain.repository.ReportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Calendar
import java.util.Locale

class ReportRepositoryFake : ReportRepository {

    private val _reports = MutableStateFlow(
        listOf(
            // 3 Pendientes (más nuevo a más viejo)
            Report(
                id = "r1",
                type = ReportType.PUBLICACION,
                motive = "Información engañosa en el precio del inmueble",
                description = "El precio publicado cambia al contactar a la persona anunciante. También se solicita un depósito antes de permitir una visita al inmueble.",
                date = getRelativeDate(0, "10:32"),
                status = ReportStatus.PENDIENTE,
                result = null,
                attendedDate = null,
                evidences = listOf(R.drawable.prop_depto_sala, R.drawable.prop_depto_cocina, R.drawable.prop_depto_recamara),
                targetId = "1"
            ),
            Report(
                id = "r2",
                type = ReportType.USUARIO,
                motive = "Mensajes ofensivos y comportamiento intimidatorio",
                description = "Después de preguntar por una visita, el usuario insistió en continuar la conversación fuera de NearHouse y envió mensajes ofensivos al negarme.",
                date = getRelativeDate(1, "18:05"),
                status = ReportStatus.PENDIENTE,
                result = null,
                attendedDate = null,
                evidences = listOf(R.drawable.prop_casa_sala, R.drawable.prop_casa_cocina, R.drawable.prop_casa_recamara),
                targetId = "user_logan"
            ),
            Report(
                id = "r3",
                type = ReportType.PUBLICACION,
                motive = "La propiedad no corresponde con las fotografías",
                description = "Las fotos publicadas corresponden a otro fraccionamiento con mejores acabados que la casa real que se muestra.",
                date = getRelativeDate(2, "09:41"),
                status = ReportStatus.PENDIENTE,
                result = null,
                attendedDate = null,
                evidences = listOf(R.drawable.prop_casa_fachada, R.drawable.prop_casa_sala, R.drawable.prop_casa_cocina, R.drawable.prop_casa_recamara, R.drawable.prop_casa_jardin),
                targetId = "3"
            ),

            // 3 Atendidos
            Report(
                id = "r4",
                type = ReportType.PUBLICACION,
                motive = "Publicación duplicada en la misma zona",
                description = "Se detectó el mismo inmueble publicado múltiples veces por diferentes cuentas con precios distintos.",
                date = getRelativeDate(1, "11:20"),
                status = ReportStatus.ATENDIDO,
                result = "Medida aplicada: publicación desactivada",
                attendedDate = getRelativeAttendedDate(1),
                evidences = listOf(R.drawable.prop_terreno_1, R.drawable.prop_terreno_4),
                targetId = "2"
            ),
            Report(
                id = "r5",
                type = ReportType.USUARIO,
                motive = "Perfil con datos de identidad inconsistentes",
                description = "La identificación oficial proporcionada no coincide con el nombre registrado en la cuenta.",
                date = getRelativeDate(3, "14:15"),
                status = ReportStatus.ATENDIDO,
                result = "Cerrado sin aplicar medida",
                attendedDate = getRelativeAttendedDate(3),
                evidences = listOf(R.drawable.prop_terreno_3, R.drawable.prop_terreno_5, R.drawable.prop_terreno_2),
                targetId = "user_ana"
            ),
            Report(
                id = "r6",
                type = ReportType.PUBLICACION,
                motive = "Contenido comercial fuera de categoría",
                description = "Anuncio que ofrece servicios de mudanza en la sección de renta de departamentos.",
                date = getRelativeDate(4, "08:30"),
                status = ReportStatus.ATENDIDO,
                result = "Medida aplicada: anuncio retirado",
                attendedDate = getRelativeAttendedDate(4),
                evidences = listOf(R.drawable.prop_depto_fachada2),
                targetId = "4"
            )
        )
    )

    override fun observeReports(): Flow<List<Report>> = _reports.asStateFlow()

    override suspend fun getReportById(reportId: String): Report? =
        _reports.value.firstOrNull { it.id == reportId }

    override suspend fun applyMeasure(reportId: String, measureResult: String, attendedDate: String) {
        _reports.update { list ->
            list.map { report ->
                if (report.id == reportId) {
                    report.copy(
                        status = ReportStatus.ATENDIDO,
                        result = measureResult,
                        attendedDate = attendedDate
                    )
                } else {
                    report
                }
            }
        }
    }

    override suspend fun closeWithoutMeasure(reportId: String, attendedDate: String) {
        _reports.update { list ->
            list.map { report ->
                if (report.id == reportId) {
                    report.copy(
                        status = ReportStatus.ATENDIDO,
                        result = "Cerrado sin aplicar medida",
                        attendedDate = attendedDate
                    )
                } else {
                    report
                }
            }
        }
    }

    private fun getRelativeDate(daysAgo: Int, time: String): String {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -daysAgo)
        }
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val monthNames = arrayOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")
        val month = monthNames[calendar.get(Calendar.MONTH)]
        return String.format(Locale.getDefault(), "%02d %s · %s", day, month, time)
    }

    private fun getRelativeAttendedDate(daysAgo: Int): String {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -daysAgo)
        }
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val monthNames = arrayOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")
        val month = monthNames[calendar.get(Calendar.MONTH)]
        return String.format(Locale.getDefault(), "Atendido · %02d %s", day, month)
    }
}
