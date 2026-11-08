package com.equipo4.nearhome.ui.subscription

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.outlined.Visibility
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
fun ChoosePlanScreen(
    viewModel: ChoosePlanViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onPlanSelected: (SubscriptionPlan) -> Unit = {},
    onFreeTrialActivated: () -> Unit = {}
) {
    val ui by viewModel.uiState.collectAsState()
    var pendingTrial by remember { mutableStateOf<SubscriptionPlan?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.choose_plan_title),
                        fontSize = 18.sp, fontWeight = FontWeight.Bold,
                        color = SubscriptionColors.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                            tint = SubscriptionColors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SubscriptionColors.surface)
            )
        },
        containerColor = SubscriptionColors.screenBg
    ) { padding ->
        when {
            ui.cargando -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            ui.errorCarga -> Column(
                Modifier.fillMaxSize().padding(padding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(stringResource(R.string.plans_load_error), color = SubscriptionColors.errorText, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Button(onClick = viewModel::cargarPlanes) { Text(stringResource(R.string.plans_retry)) }
            }

            else -> Column(
                Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        stringResource(R.string.choose_plan_header_title),
                        fontSize = 20.sp, fontWeight = FontWeight.Bold,
                        color = SubscriptionColors.textPrimary, textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        stringResource(R.string.choose_plan_header_subtitle),
                        fontSize = 13.sp, color = SubscriptionColors.textMuted,
                        textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                ui.planes.forEach { plan ->
                    val esActual = plan.id == ui.planActualId
                    val esPrueba = plan.isFreeTrialFor(ui.pruebaDisponible) && !esActual
                    PlanCard(
                        plan = plan,
                        esActual = esActual,
                        esPrueba = esPrueba,
                        onClick = { if (esPrueba) pendingTrial = plan else onPlanSelected(plan) }
                    )
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }

    pendingTrial?.let { plan ->
        AlertDialog(
            onDismissRequest = { if (!ui.guardando) pendingTrial = null },
            text = {
                Column {
                    Text(
                        stringResource(
                            R.string.plan_free_trial_dialog_text,
                            plan.name,
                            plan.trialDays,
                            plan.priceText + stringResource(R.string.price_per_month)
                        )
                    )
                    ui.errorAccion?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = SubscriptionColors.errorText)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !ui.guardando,
                    onClick = { viewModel.contratar(plan) { pendingTrial = null; onFreeTrialActivated() } }
                ) {
                    if (ui.guardando) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    else Text(stringResource(R.string.free_trial_activate_btn))
                }
            },
            dismissButton = {
                TextButton(enabled = !ui.guardando, onClick = { pendingTrial = null }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
}

@Composable
private fun PlanCard(
    plan: SubscriptionPlan,
    esActual: Boolean,
    esPrueba: Boolean,
    onClick: () -> Unit
) {
    Box(Modifier.fillMaxWidth().padding(top = if (plan.isPopular) 16.dp else 0.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(top = if (plan.isPopular) 12.dp else 0.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SubscriptionColors.surface),
            border = BorderStroke(
                width = if (plan.isPopular) 5.dp else 1.dp,
                color = if (plan.isPopular) Color(0xFF008EFF) else SubscriptionColors.border
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(Modifier.fillMaxWidth().padding(20.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(plan.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SubscriptionColors.textPrimary)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(plan.priceText, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = SubscriptionColors.brand)
                        Text(
                            stringResource(R.string.price_per_month),
                            fontSize = 14.sp, color = SubscriptionColors.textMuted,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }

                HorizontalDivider(Modifier.padding(vertical = 14.dp), color = SubscriptionColors.border, thickness = 1.dp)

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.AutoMirrored.Outlined.Article, null, tint = SubscriptionColors.brand, modifier = Modifier.size(20.dp))
                    Text(plan.description, fontSize = 14.sp, color = SubscriptionColors.textBody)
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Outlined.Visibility, null, tint = SubscriptionColors.brand, modifier = Modifier.size(20.dp))
                    Text(plan.visibility, fontSize = 14.sp, color = SubscriptionColors.textBody)
                }

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = onClick,
                    enabled = !esActual,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (esPrueba) Color(0xFF16A34A) else SubscriptionColors.brand,
                        disabledContainerColor = Color(0xFF9CA3AF)
                    )
                ) {
                    Text(
                        text = when {
                            esActual -> stringResource(R.string.plan_current_btn)
                            esPrueba -> stringResource(R.string.free_month_btn)
                            else -> stringResource(R.string.choose_plan_btn)
                        },
                        fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White
                    )
                }
            }
        }

        if (plan.isPopular) {
            Surface(
                color = Color(0xFF008EFF),
                shape = RoundedCornerShape(topStart = 14.dp, topEnd = 12.dp, bottomEnd = 10.dp),
                modifier = Modifier.align(Alignment.TopStart).offset(y = (-10).dp)
            ) {
                Text(
                    stringResource(R.string.most_popular),
                    color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }
    }
}