package com.equipo4.nearhome.ui.admin.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo4.nearhome.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportConfirmationScreen(
    reportId: String,
    onReturnToList: () -> Unit = {}
) {
    val viewModel: ReportDetailViewModel = viewModel(
        factory = ReportDetailViewModelFactory(reportId)
    )
    val report by viewModel.report.collectAsState()

    Scaffold(
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Círculo verde con palomita
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFF16A34A), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Atendido",
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            Surface(
                color = Color(0xFFDCFCE7),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.report_attended_label),
                    color = Color(0xFF15803D),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Reporte marcado como atendido",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "La resolución se guardó correctamente y el reporte ya forma parte del historial de Atendidos.",
                fontSize = 14.sp,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(Modifier.height(24.dp))

            if (report != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tipo", color = Color(0xFF6B7280), fontSize = 14.sp)
                            Text(report!!.type.label, fontWeight = FontWeight.SemiBold, color = Color(0xFF1F2937), fontSize = 14.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Resultado", color = Color(0xFF6B7280), fontSize = 14.sp)
                            Text(
                                text = if (report!!.result?.contains("Medida aplicada") == true) "Medida aplicada" else (report!!.result ?: "Atendido"),
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1F2937),
                                fontSize = 14.sp,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Fecha", color = Color(0xFF6B7280), fontSize = 14.sp)
                            Text(
                                text = report!!.attendedDate ?: "26 sep - 10:48",
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1F2937),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = onReturnToList,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B2C4D))
            ) {
                Text(
                    text = stringResource(R.string.return_to_list),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
