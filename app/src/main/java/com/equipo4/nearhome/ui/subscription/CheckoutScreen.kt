package com.equipo4.nearhome.ui.subscription

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.ui.res.stringResource
import com.equipo4.nearhome.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    planId: String,
    onBackClick: () -> Unit = {},
    onSuccessFinished: () -> Unit = {}
) {
    val plan = remember(planId) { SubscriptionPlan.getById(planId) }
    val viewModel: CheckoutViewModel = remember(plan) { CheckoutViewModel(plan) }

    val cardNumber by viewModel.cardNumber.collectAsState()
    val cardHolder by viewModel.cardHolder.collectAsState()
    val expiryDate by viewModel.expiryDate.collectAsState()
    val cvv by viewModel.cvv.collectAsState()
    val saveCard by viewModel.saveCard.collectAsState()

    val cardNumberError by viewModel.cardNumberError.collectAsState()
    val cardHolderError by viewModel.cardHolderError.collectAsState()
    val expiryError by viewModel.expiryError.collectAsState()
    val cvvError by viewModel.cvvError.collectAsState()

    val showGeneralError by viewModel.showGeneralError.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

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
                // Banner verde de prueba gratuita (con icono Filled.Redeem para caja de regalo sólida)
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

                // Resumen del plan (limpio sobre el fondo)
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
                            text = plan.priceText.substringBefore("/"),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = SubscriptionColors.brand,
                            maxLines = 1,
                            softWrap = false
                        )
                        Text(
                            text = "/mes",
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
                        placeholder = { Text("1234 1234 1234 1234") },
                        isError = cardNumberError != null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (cardNumberError != null) Color(0xFFEF4444) else SubscriptionColors.brand,
                            unfocusedBorderColor = if (cardNumberError != null) Color(0xFFEF4444) else SubscriptionColors.inputBorder,
                            focusedContainerColor = SubscriptionColors.inputBg,
                            unfocusedContainerColor = SubscriptionColors.inputBg
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (cardNumberError != null) {
                        Text(cardNumberError!!, fontSize = 12.sp, color = SubscriptionColors.errorText, modifier = Modifier.padding(top = 2.dp))
                    }
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
                        isError = cardHolderError != null,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (cardHolderError != null) Color(0xFFEF4444) else SubscriptionColors.brand,
                            unfocusedBorderColor = if (cardHolderError != null) Color(0xFFEF4444) else SubscriptionColors.inputBorder,
                            focusedContainerColor = SubscriptionColors.inputBg,
                            unfocusedContainerColor = SubscriptionColors.inputBg
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (cardHolderError != null) {
                        Text(cardHolderError!!, fontSize = 12.sp, color = SubscriptionColors.errorText, modifier = Modifier.padding(top = 2.dp))
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Vencimiento y CVV
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.card_expiry_label), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = SubscriptionColors.textLabel)
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(
                            value = expiryDate,
                            onValueChange = viewModel::onExpiryChanged,
                            placeholder = { Text("MM / AA") },
                            isError = expiryError != null,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (expiryError != null) Color(0xFFEF4444) else SubscriptionColors.brand,
                                unfocusedBorderColor = if (expiryError != null) Color(0xFFEF4444) else SubscriptionColors.inputBorder,
                                focusedContainerColor = SubscriptionColors.inputBg,
                                unfocusedContainerColor = SubscriptionColors.inputBg
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (expiryError != null) {
                            Text(expiryError!!, fontSize = 12.sp, color = SubscriptionColors.errorText, modifier = Modifier.padding(top = 2.dp))
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.card_cvv_label), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = SubscriptionColors.textLabel)
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(
                            value = cvv,
                            onValueChange = viewModel::onCvvChanged,
                            placeholder = { Text("123") },
                            isError = cvvError != null,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (cvvError != null) Color(0xFFEF4444) else SubscriptionColors.brand,
                                unfocusedBorderColor = if (cvvError != null) Color(0xFFEF4444) else SubscriptionColors.inputBorder,
                                focusedContainerColor = SubscriptionColors.inputBg,
                                unfocusedContainerColor = SubscriptionColors.inputBg
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (cvvError != null) {
                            Text(cvvError!!, fontSize = 12.sp, color = SubscriptionColors.errorText, modifier = Modifier.padding(top = 2.dp))
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Casilla guardar tarjeta
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = saveCard,
                        onCheckedChange = { viewModel.saveCard.value = it },
                        colors = CheckboxDefaults.colors(checkedColor = SubscriptionColors.brand)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.save_card_checkbox),
                        fontSize = 13.sp,
                        color = SubscriptionColors.textBody
                    )
                }

                Spacer(Modifier.height(10.dp))

                // Botón principal
                Button(
                    onClick = { viewModel.submit(onSuccessNavigation = onSuccessFinished) },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SubscriptionColors.brand,
                        disabledContainerColor = SubscriptionColors.brand.copy(alpha = 0.6f)
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = if (plan.isFreeTrial) stringResource(R.string.activate_free_trial_btn) else stringResource(R.string.confirm_and_pay_btn),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFFFFF)
                        )
                    }
                }

                // Mensaje de error general debajo del botón con icono de advertencia
                if (showGeneralError) {
                    Spacer(Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Error,
                            contentDescription = null,
                            tint = SubscriptionColors.errorStrong,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(R.string.error_fill_mandatory_dot),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = SubscriptionColors.errorStrong
                        )
                    }
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

            // Overlay de éxito
            if (successMessage != null) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black.copy(alpha = 0.5f)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .padding(24.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SubscriptionColors.surface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    color = Color(0xFF16A34A),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    text = successMessage!!,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SubscriptionColors.textPrimary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}