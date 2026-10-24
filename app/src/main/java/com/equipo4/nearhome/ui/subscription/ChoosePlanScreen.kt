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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equipo4.nearhome.R

import androidx.compose.ui.graphics.luminance
import com.equipo4.nearhome.ui.theme.NearHouseBgDark
import com.equipo4.nearhome.ui.theme.NearHouseSurfaceDark
import com.equipo4.nearhome.ui.theme.NearHouseOnSurfaceDark
import com.equipo4.nearhome.ui.theme.NearHouseOnSurfaceVariantDark
import com.equipo4.nearhome.ui.theme.NearHouseOutlineDark
import com.equipo4.nearhome.ui.theme.NearHousePrimaryDark

/** Elige el color claro o el oscuro según el tema activo de la app. */
@Composable
private fun themed(light: Color, dark: Color): Color =
    if (MaterialTheme.colorScheme.background.luminance() < 0.5f) dark else light

/**
 * Colores de las pantallas de suscripción y cobro. En modo claro conservan el aspecto de siempre
 * y en modo oscuro usan la paleta oscura de NearHouse.
 */
internal object SubscriptionColors {
    val screenBg: Color
        @Composable get() = themed(Color(0xFFF9FAFB), NearHouseBgDark)
    val surface: Color
        @Composable get() = themed(Color.White, NearHouseSurfaceDark)
    val textPrimary: Color
        @Composable get() = themed(Color(0xFF1F2937), NearHouseOnSurfaceDark)
    val textLabel: Color
        @Composable get() = themed(Color(0xFF374151), Color(0xFFE2E8F0))
    val textBody: Color
        @Composable get() = themed(Color(0xFF4B5563), Color(0xFFCBD5E1))
    val textMuted: Color
        @Composable get() = themed(Color(0xFF6B7280), NearHouseOnSurfaceVariantDark)
    val border: Color
        @Composable get() = themed(Color(0xFFE5E7EB), NearHouseOutlineDark)
    val inputBorder: Color
        @Composable get() = themed(Color(0xFFD1D5DB), Color(0xFF475569))
    val inputBg: Color
        @Composable get() = themed(Color.White, Color(0xFF1E2630))
    val brand: Color
        @Composable get() = themed(Color(0xFF0B2C4D), NearHousePrimaryDark)
    val successBannerBg: Color
        @Composable get() = themed(Color(0xFFDCFCE7), Color(0xFF12301F))
    val successIcon: Color
        @Composable get() = themed(Color(0xFF15803D), Color(0xFF4ADE80))
    val errorText: Color
        @Composable get() = themed(Color(0xFFB91C1C), Color(0xFFFCA5A5))
    val errorStrong: Color
        @Composable get() = themed(Color(0xFFDC2626), Color(0xFFF87171))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChoosePlanScreen(
    onBackClick: () -> Unit = {},
    onPlanSelected: (String) -> Unit = {}
) {
    val plans = SubscriptionPlan.plans

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.choose_plan_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SubscriptionColors.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                            tint = SubscriptionColors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SubscriptionColors.surface)
            )
        },
        containerColor = SubscriptionColors.screenBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Texto motivacional centrado
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.choose_plan_header_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = SubscriptionColors.textPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.choose_plan_header_subtitle),
                    fontSize = 13.sp,
                    color = SubscriptionColors.textMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            plans.forEach { plan ->
                PlanCard(
                    plan = plan,
                    onClick = { onPlanSelected(plan.id) }
                )
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun PlanCard(
    plan: SubscriptionPlan,
    onClick: () -> Unit
) {
    val isPro = plan.id == "profesional"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = if (isPro) 16.dp else 0.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = if (isPro) 12.dp else 0.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SubscriptionColors.surface),
            border = BorderStroke(
                width = if (isPro) 5.dp else 1.dp,
                color = if (isPro) Color(0xFF008EFF) else SubscriptionColors.border
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Título y Precio (Precio grande: 32.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = plan.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = SubscriptionColors.textPrimary
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = plan.priceText.substringBefore("/"),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = SubscriptionColors.brand
                        )
                        Text(
                            text = stringResource(R.string.price_per_month),
                            fontSize = 14.sp,
                            color = SubscriptionColors.textMuted,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 14.dp),
                    color = SubscriptionColors.border,
                    thickness = 1.dp
                )

                // Descripción y Visibilidad
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Article,
                        contentDescription = null,
                        tint = SubscriptionColors.brand,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = plan.description,
                        fontSize = 14.sp,
                        color = SubscriptionColors.textBody
                    )
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Visibility,
                        contentDescription = null,
                        tint = SubscriptionColors.brand,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = plan.visibility,
                        fontSize = 14.sp,
                        color = SubscriptionColors.textBody
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Botón
                Button(
                    onClick = onClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (plan.isFreeTrial) Color(0xFF16A34A) else SubscriptionColors.brand
                    )
                ) {
                    Text(
                        text = if (plan.isFreeTrial) stringResource(R.string.free_month_btn) else stringResource(R.string.choose_plan_btn),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFFFFF)
                    )
                }
            }
        }

        // Etiqueta "Más popular"
        if (isPro) {
            Surface(
                color = Color(0xFF008EFF),
                shape = RoundedCornerShape(topStart = 14.dp, topEnd = 12.dp, bottomEnd = 10.dp),
                modifier = Modifier
                    .padding(start = 0.dp)
                    .align(Alignment.TopStart)
                    .offset(x = 0.dp, y = (-10.dp))
            ) {
                Text(
                    text = stringResource(R.string.most_popular),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }
    }
}