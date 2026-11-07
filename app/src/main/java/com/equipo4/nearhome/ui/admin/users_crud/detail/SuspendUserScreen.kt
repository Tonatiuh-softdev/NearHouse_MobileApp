package com.equipo4.nearhome.ui.admin.users_crud.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equipo4.nearhome.R
import androidx.compose.foundation.layout.navigationBarsPadding

private val OPCIONES_DIAS = listOf(1, 7, 30, 90, 365)

/** Pantalla de confirmación para suspender. dias = null significa indefinida. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SuspendUserScreen(
    userName: String,
    isSubmitting: Boolean,
    errorMessage: String?,
    onBackClick: () -> Unit,
    onConfirm: (dias: Int?, motivo: String) -> Unit
) {
    var diasSeleccionados by remember { mutableStateOf<Int?>(7) }
    var indefinida by remember { mutableStateOf(false) }
    var motivo by remember { mutableStateOf("") }

    val motivoValido = motivo.trim().isNotEmpty() && motivo.length <= 300
    val diasValidos = indefinida || diasSeleccionados != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.admin_user_suspend_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Button(
                    onClick = { onConfirm(if (indefinida) null else diasSeleccionados, motivo.trim()) },
                    enabled = motivoValido && diasValidos && !isSubmitting,
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text(stringResource(R.string.admin_user_suspend_confirm), fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.admin_user_suspend_body, userName),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(stringResource(R.string.admin_user_suspend_duration), fontWeight = FontWeight.Bold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OPCIONES_DIAS.forEach { dias ->
                    FilterChip(
                        selected = !indefinida && diasSeleccionados == dias,
                        onClick = {
                            indefinida = false
                            diasSeleccionados = dias
                        },
                        label = { Text(stringResource(R.string.admin_user_suspend_days_format, dias)) }
                    )
                }
                FilterChip(
                    selected = indefinida,
                    onClick = { indefinida = true },
                    label = { Text(stringResource(R.string.admin_user_suspend_indefinite)) }
                )
            }

            OutlinedTextField(
                value = motivo,
                onValueChange = { if (it.length <= 300) motivo = it },
                label = { Text(stringResource(R.string.admin_user_suspend_reason)) },
                supportingText = { Text("${motivo.length}/300") },
                minLines = 3,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            if (errorMessage != null) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }
        }
    }
}