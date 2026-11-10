package com.equipo4.nearhome.ui.subscription

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equipo4.nearhome.R
import com.equipo4.nearhome.data.session.CurrentUserStore
import com.equipo4.nearhome.ui.notifications.NotificacionesHelper
import com.equipo4.nearhome.ui.profile.UserRole
import com.equipo4.nearhome.util.LogroSonidoEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    planId: Int,
    onBackClick: () -> Unit = {},
    onSuccessFinished: () -> Unit = {}
) {
    val context = LocalContext.current
    val plan = remember(planId) {
        SubscriptionPlanCache.find(planId)?.let {
            it.copy(isFreeTrial = it.isFreeTrialFor(CurrentUserStore.user.value?.pruebaDisponible == true))
        }
    }
    if (plan == null) {
        LaunchedEffect(Unit) { onBackClick() }
        return
    }

    val eraComprador = remember { CurrentUserStore.roleOrNull() == UserRole.COMPRADOR }
    val viewModel: CheckoutViewModel = remember(plan) {
        CheckoutViewModel(plan, SubscriptionRepository(context))
    }

    val cardNumber by viewModel.cardNumber.collectAsState()
    val cardHolder by viewModel.cardHolder.collectAsState()
    val expiryMonth by viewModel.expiryMonth.collectAsState()
    val expiryYear by viewModel.expiryYear.collectAsState()
    val cvv by viewModel.cvv.collectAsState()

    val isComplete by viewModel.isComplete.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val apiError by viewModel.apiError.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (plan.isFreeTrial) stringResource(R.string.free_trial_title) else stringResource(R.string.confirm_payment_title),
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Banner de prueba gratuita
                if (plan.isFreeTrial) {
                    Surface(
                        color = SubscriptionColors.successBannerBg,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF16A34A))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Redeem,
                                contentDescription = null,
                                tint = SubscriptionColors.successIcon,
                                modifier = Modifier.size(32.dp)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = stringResource(R.string.free_trial_banner_title),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SubscriptionColors.textPrimary,
                                    lineHeight = 16.sp
                                )
                                Text(
                                    text = stringResource(R.string.free_trial_banner),
                                    fontSize = 12.sp,
                                    color = SubscriptionColors.textBody,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // Resumen del plan
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.checkout_plan_format, plan.name),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = SubscriptionColors.brand
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "${plan.description} · ${plan.visibility}",
                            fontSize = 13.sp,
                            color = SubscriptionColors.textBody
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = plan.priceText,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = SubscriptionColors.brand,
                            maxLines = 1,
                            softWrap = false
                        )
                        Text(
                            text = stringResource(R.string.price_per_month),
                            fontSize = 14.sp,
                            color = SubscriptionColors.textMuted,
                            modifier = Modifier.padding(bottom = 2.dp),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.payment_method_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SubscriptionColors.textPrimary,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(Modifier.height(12.dp))

                // Número de tarjeta
                Column {
                    Text(stringResource(R.string.card_number_label), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = SubscriptionColors.textLabel)
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = viewModel::onCardNumberChanged,
                        placeholder = { Text("1234567812345678") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Nombre del titular
                Column {
                    Text(stringResource(R.string.card_holder_label), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = SubscriptionColors.textLabel)
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = cardHolder,
                        onValueChange = viewModel::onCardHolderChanged,
                        placeholder = { Text(stringResource(R.string.checkout_cardholder_hint)) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Mes, Año y CVV en una fila
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.card_month_label), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = SubscriptionColors.textLabel)
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(
                            value = expiryMonth,
                            onValueChange = viewModel::onExpiryMonthChanged,
                            placeholder = { Text("01") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.card_year_label), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = SubscriptionColors.textLabel)
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(
                            value = expiryYear,
                            onValueChange = viewModel::onExpiryYearChanged,
                            placeholder = { Text("28") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.card_cvv_label), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = SubscriptionColors.textLabel)
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(
                            value = cvv,
                            onValueChange = viewModel::onCvvChanged,
                            placeholder = { Text("123") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Botón principal
                Button(
                    onClick = { viewModel.submit(onSuccessNavigation = onSuccessFinished) },
                    enabled = isComplete && !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SubscriptionColors.brand,
                        disabledContainerColor = SubscriptionColors.brand.copy(alpha = 0.5f)
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = if (plan.isFreeTrial) stringResource(R.string.activate_free_trial_btn) else stringResource(R.string.pay_amount_btn, plan.priceText),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFFFFF)
                        )
                    }
                }

                // Error de la API (sin internet, etc.)
                apiError?.let { err ->
                    Spacer(Modifier.height(10.dp))
                    val errorText = when (err) {
                        "NETWORK_ERROR" -> stringResource(R.string.check_internet_connection)
                        "UNEXPECTED_ERROR" -> stringResource(R.string.error_unexpected_retry)
                        else -> err
                    }
                    Text(errorText, fontSize = 13.sp, color = SubscriptionColors.errorStrong)
                }

                Spacer(Modifier.height(16.dp))

                // Texto debajo del botón
                Text(
                    text = if (plan.isFreeTrial) {
                        stringResource(R.string.free_trial_footer)
                    } else {
                        stringResource(R.string.confirm_payment_footer)
                    },
                    fontSize = 12.sp,
                    color = SubscriptionColors.textMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                )

                Spacer(Modifier.height(30.dp))
            }

            // Pantalla de éxito
            if (successMessage != null) {
                LogroSonidoEffect()
                LaunchedEffect(Unit) { if (eraComprador) NotificacionesHelper.mostrarBienvenidaVendedor(context) }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = SubscriptionColors.screenBg
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = successMessage!!,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = SubscriptionColors.textPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(40.dp))
                        Box(
                            modifier = Modifier
                                .size(160.dp)
                                .background(Color(0xFF16A34A), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(90.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
