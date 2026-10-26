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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equipo4.nearhome.R
import com.equipo4.nearhome.domain.model.Report
import com.equipo4.nearhome.domain.model.ReportStatus
import com.equipo4.nearhome.domain.model.ReportType

@Composable
fun ReportTypeTag(type: ReportType) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val (bg, textColor, icon) = when (type) {
        ReportType.PUBLICACION -> if (isDark) {
            Triple(Color(0xFF382314), Color(0xFFFB923C), Icons.Outlined.Home)
        } else {
            Triple(Color(0xFFFFF7ED), Color(0xFFC2410C), Icons.Outlined.Home)
        }
        ReportType.USUARIO -> if (isDark) {
            Triple(Color(0xFF122840), Color(0xFF60A5FA), Icons.Outlined.Person)
        } else {
            Triple(Color(0xFFEFF6FF), Color(0xFF1D4ED8), Icons.Outlined.Person)
        }
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
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val bg = if (isDark) Color(0xFF143821) else Color(0xFFDCFCE7)
    val textColor = if (isDark) Color(0xFF4ADE80) else Color(0xFF15803D)

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
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = stringResource(R.string.status_attended),
                color = textColor,
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = report.motive,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2
            )

            if (report.status == ReportStatus.ATENDIDO && !report.result.isNullOrEmpty()) {
                Spacer(Modifier.height(4.dp))
                val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
                val greenResultColor = if (isDark) Color(0xFF4ADE80) else Color(0xFF15803D)
                Text(
                    text = report.result,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if ((report.result.contains("Cerrado") || report.result.contains("Closed"))) MaterialTheme.colorScheme.onSurfaceVariant else greenResultColor
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PhotoCamera,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    val evidencesText = pluralStringResource(R.plurals.evidences_count, report.evidences.size, report.evidences.size)
                    Text(
                        text = evidencesText,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
