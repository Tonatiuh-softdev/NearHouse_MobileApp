package com.equipo4.nearhome.ui.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.equipo4.nearhome.data.remote.dto.GoogleProofResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletarPerfilGoogleScreen(
    proofData: GoogleProofResponse,
    viewModel: GoogleAuthViewModel,
    onRegistroExitoso: () -> Unit,
    onVolverAlLogin: () -> Unit
) {
    val context = LocalContext.current
    var nombre by remember { mutableStateOf(proofData.nombre) }
    var apellidos by remember { mutableStateOf(proofData.apellidos) }
    var telefono by remember { mutableStateOf("") }
    var smsTokenInput by remember { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        when (uiState) {
            is GoogleAuthState.SessionSuccess -> {
                Toast.makeText(context, "Bienvenido a NearHouse", Toast.LENGTH_SHORT).show()
                onRegistroExitoso()
            }
            is GoogleAuthState.Error -> {
                val errorMsg = (uiState as GoogleAuthState.Error).mensaje
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                // Si el comprobante expiró o es inválido, regresa al login
                if (errorMsg.contains("expirado", ignoreCase = true) || errorMsg.contains("inválido", ignoreCase = true)) {
                    viewModel.reiniciarEstado()
                    onVolverAlLogin()
                }
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Completar Perfil") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Casi listo, confirma tus datos para finalizar el registro",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Correo en solo lectura (sin poder modificar)
            OutlinedTextField(
                value = proofData.correo,
                onValueChange = {},
                label = { Text("Correo electrónico") },
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )

            // Nombre pre-llenado (editable)
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )

            // Apellidos pre-llenados (editables)
            OutlinedTextField(
                value = apellidos,
                onValueChange = { apellidos = it },
                label = { Text("Apellidos") },
                modifier = Modifier.fillMaxWidth()
            )

            // Teléfono obligatorio
            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = { Text("Teléfono (+52...)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

            // Token/Código SMS
            OutlinedTextField(
                value = smsTokenInput,
                onValueChange = { smsTokenInput = it },
                label = { Text("Código de verificación SMS") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (nombre.isBlank() || apellidos.isBlank() || telefono.isBlank() || smsTokenInput.isBlank()) {
                        Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    viewModel.registrarConGoogleFinal(
                        nombre = nombre,
                        apellidos = apellidos,
                        telefono = telefono,
                        googleProofToken = proofData.googleProofToken,
                        smsToken = smsTokenInput
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = uiState !is GoogleAuthState.Loading
            ) {
                if (uiState is GoogleAuthState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Finalizar e Iniciar Sesión")
                }
            }
        }
    }
}