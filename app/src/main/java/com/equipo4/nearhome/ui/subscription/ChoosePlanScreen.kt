package com.equipo4.nearhome.ui.subscription

import com.equipo4.nearhome.ui.common.PlanCardsSkeleton
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import com.equipo4.nearhome.ui.common.NoConnectionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChoosePlanScreen(
    viewModel: ChoosePlanViewModel = viewModel(),
    motivoCrear: Boolean = false,
    // Llegó desde "Crear anuncio" con el plan lleno: se le pide uno con más anuncios
    motivoLimite: Boolean = false,
    onBackClick: () -> Unit = {},
    onPlanSelected: (SubscriptionPlan) -> Unit = {},
    onFreeTrialActivated: () -> Unit = {}
) {
    val ui by viewModel.uiState.collectAsState()
    var mostrarDialogoCancelar by remember { mutableStateOf(false) }

    if (mostrarDialogoCancelar) {
        AlertDialog(
            onDismissRequest = {
                if (!ui.guardando) mostrarDialogoCancelar = false
            },
            title = {
                Text(
                    text = stringResource(R.string.cancel_subscription_dialog_title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.cancel_subscription_dialog_body),
                        fontSize = 14.sp,
                        color = SubscriptionColors.textBody
                    )
                    if (ui.guardando) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    if (ui.errorAccion != null) {
                        Text(
                            text = ui.errorAccion ?: "",
                            fontSize = 13.sp,
                            color = SubscriptionColors.errorStrong
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cancelarPlan(
                            onDone = {
                                mostrarDialogoCancelar = false
                            }
                        )
                    },
                    enabled = !ui.guardando,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDC2626)
                    )
                ) {
                    Text(
                        stringResource(R.string.cancel_subscription_dialog_confirm),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { mostrarDialogoCancelar = false },
                    enabled = !ui.guardando
                ) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

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
            ui.cargando -> PlanCardsSkeleton(modifier = Modifier.fillMaxSize().padding(padding))

            ui.errorCarga -> NoConnectionState(
                onRetry = viewModel::cargarPlanes,
                isRetrying = ui.reintentando,
                modifier = Modifier.padding(padding)
            )

            else -> Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (motivoCrear || motivoLimite) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(
                                if (motivoLimite) R.string.plan_limit_banner else R.string.plan_required_to_publish
                            ),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
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
                        onSelect = {
                            if (esActual) {
                                mostrarDialogoCancelar = true
                            } else {
                                onPlanSelected(plan)
                            }
                        }
                    )
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun PlanCard(
    plan: SubscriptionPlan,
    esActual: Boolean,
    esPrueba: Boolean,
    onSelect: () -> Unit
) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(top = if (plan.isPopular) 16.dp else 0.dp)
            .clickable(onClick = onSelect)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(top = if (plan.isPopular) 12.dp else 0.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = SubscriptionColors.surface
            ),
            border = BorderStroke(
                width = if (plan.isPopular) 2.dp else 1.dp,
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
                    Column {
                        if (esActual) {
                            Text(
                                text = stringResource(R.string.plan_current_btn),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF16A34A),
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                        Text(plan.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SubscriptionColors.textPrimary)
                    }
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
                    onClick = onSelect,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when {
                            esActual -> Color(0xFFDC2626)
                            esPrueba -> Color(0xFF16A34A)
                            else -> SubscriptionColors.brand
                        }
                    )
                ) {
                    Text(
                        text = when {
                            esActual -> stringResource(R.string.cancel_plan_btn)
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
