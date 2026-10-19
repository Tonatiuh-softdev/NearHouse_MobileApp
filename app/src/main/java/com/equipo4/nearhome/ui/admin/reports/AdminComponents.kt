package com.equipo4.nearhome.ui.admin.reports

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equipo4.nearhome.domain.model.Report
import com.equipo4.nearhome.domain.model.ReportStatus
import com.equipo4.nearhome.domain.model.ReportType

import androidx.compose.ui.res.stringResource
import com.equipo4.nearhome.R
import androidx.compose.ui.res.pluralStringResource

@Composable
fun ReportTypeTag(type: ReportType) {
    val (bg, textColor, icon) = when (type) {
        ReportType.PUBLICACION -> Triple(Color(0xFFFFF7ED), Color(0xFFC2410C), Icons.Outlined.Home)
        ReportType.USUARIO -> Triple(Color(0xFFEFF6FF), Color(0xFF1D4ED8), Icons.Outlined.Person)
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = type.label,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ReportStatusBadge() {
    Surface(
        color = Color(0xFFDCFCE7),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF15803D),
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = stringResource(R.string.status_attended),
                color = Color(0xFF15803D),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ReportCardItem(
    report: Report,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ReportTypeTag(report.type)

                if (report.status == ReportStatus.ATENDIDO) {
                    ReportStatusBadge()
                } else {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = stringResource(R.string.common_view_detail),
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = report.motive,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1F2937),
                maxLines = 2
            )

            if (report.status == ReportStatus.ATENDIDO && !report.result.isNullOrEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = report.result,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (report.result.contains("Cerrado")) Color(0xFF6B7280) else Color(0xFF15803D)
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val dateText = if (report.status == ReportStatus.ATENDIDO) {
                    val rawDate = report.attendedDate ?: report.date
                    if (rawDate.startsWith("Atendido") || rawDate.startsWith("Attended")) rawDate else stringResource(R.string.attended_date_prefix, rawDate)
                } else {
                    report.date
                }
                Text(
                    text = dateText,
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PhotoCamera,
                        contentDescription = null,
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(14.dp)
                    )
                    val evidencesText = pluralStringResource(R.plurals.evidences_count, report.evidences.size, report.evidences.size)
                    Text(
                        text = evidencesText,
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }
        }
    }
}