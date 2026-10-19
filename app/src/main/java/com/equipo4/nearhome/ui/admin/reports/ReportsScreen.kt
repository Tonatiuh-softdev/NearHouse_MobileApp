package com.equipo4.nearhome.ui.admin.reports

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Security
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
import com.equipo4.nearhome.ui.common.NearHomeBottomBar

import androidx.compose.ui.res.pluralStringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onReportClick: (String) -> Unit = {},
    onBottomTabSelected: (Int) -> Unit = {}
) {
    val pendingReports by viewModel.pendingReports.collectAsState()
    val attendedReports by viewModel.attendedReports.collectAsState()
    val pendingCount by viewModel.pendingCount.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Pendientes, 1: Atendidos

    val subtitleText = when (selectedTab) {
        0 -> {
            if (pendingCount == 0) {
                stringResource(R.string.reports_subtitle_day_to_day)
            } else {
                pluralStringResource(R.plurals.pending_reports_subtitle, pendingCount, pendingCount)
            }
        }
        else -> stringResource(R.string.reports_subtitle_history)
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(Color.White)) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = stringResource(R.string.reports_title),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F2937)
                            )
                            Text(
                                text = subtitleText,
                                fontSize = 12.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.common_back),
                                tint = Color(0xFF1F2937)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )

                SecondaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White,
                    contentColor = Color(0xFF0B2C4D)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = stringResource(R.string.tab_pending),
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 0) Color(0xFF0B2C4D) else Color(0xFF6B7280)
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                text = stringResource(R.string.tab_attended),
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 1) Color(0xFF0B2C4D) else Color(0xFF6B7280)
                            )
                        }
                    )
                }
                HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 1.dp)
            }
        },
        bottomBar = { NearHomeBottomBar(selectedTab = 3, onTabSelected = onBottomTabSelected) },
        containerColor = Color(0xFFF9FAFB)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> {
                    if (pendingReports.isEmpty()) {
                        EmptyPendingReports(
                            onConsultAttendedClick = { selectedTab = 1 }
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = paddingHorizontal16()
                        ) {
                            item { Spacer(Modifier.height(16.dp)) }
                            items(pendingReports) { report ->
                                ReportCardItem(
                                    report = report,
                                    onClick = { onReportClick(report.id) }
                                )
                                Spacer(Modifier.height(12.dp))
                            }
                            item { Spacer(Modifier.height(16.dp)) }
                        }
                    }
                }
                1 -> {
                    if (attendedReports.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(stringResource(R.string.reports_empty_attended), color = Color(0xFF6B7280))
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = paddingHorizontal16()
                        ) {
                            item { Spacer(Modifier.height(16.dp)) }
                            items(attendedReports) { report ->
                                ReportCardItem(
                                    report = report,
                                    onClick = { onReportClick(report.id) }
                                )
                                Spacer(Modifier.height(12.dp))
                            }
                            item { Spacer(Modifier.height(16.dp)) }
                        }
                    }
                }
            }
        }
    }
}

private fun paddingHorizontal16(): PaddingValues = PaddingValues(horizontal = 16.dp)

@Composable
private fun EmptyPendingReports(
    onConsultAttendedClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .background(Color(0xFFE5E7EB).copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Security,
                contentDescription = null,
                tint = Color(0xFF0B2C4D),
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.empty_pending_title),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.empty_pending_subtitle),
            fontSize = 14.sp,
            color = Color(0xFF6B7280),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(24.dp))

        OutlinedButton(
            onClick = onConsultAttendedClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0B2C4D)),
            border = BorderStroke(1.dp, Color(0xFF0B2C4D))
        ) {
            Icon(
                imageVector = Icons.Outlined.History,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.consult_attended_reports),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}