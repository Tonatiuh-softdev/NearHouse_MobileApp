package com.equipo4.nearhome.ui.appeal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.equipo4.nearhome.R
import com.equipo4.nearhome.util.AppStrings

/**
 * Aviso del inicio de sesión (con correo o con Google) cuando la cuenta está suspendida.
 * Mismo estilo que el aviso de cuenta desactivada. Si todavía no apela esta suspensión, ofrece "Apelar".
 */
@Composable
fun CuentaSuspendidaDialog(
    info: CuentaSuspendida,
    onAppeal: () -> Unit,
    onDismiss: () -> Unit
) {
    val hasta = info.hastaTexto(AppStrings.locale())
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.suspended_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (info.motivo.isNotBlank()) {
                    Text(stringResource(R.string.suspended_reason, info.motivo))
                }
                Text(
                    if (hasta != null) stringResource(R.string.suspended_until, hasta)
                    else stringResource(R.string.suspended_indefinite)
                )
                val estado = when {
                    info.puedeApelar -> stringResource(R.string.suspended_can_appeal)
                    info.apelacionEstado == "PENDIENTE" -> stringResource(R.string.suspended_appeal_pending)
                    info.apelacionEstado == "RECHAZADA" -> {
                        val respuesta = info.apelacionNota?.takeIf { it.isNotBlank() }
                        if (respuesta != null) {
                            stringResource(R.string.suspended_appeal_rejected_note, respuesta)
                        } else {
                            stringResource(R.string.suspended_appeal_rejected)
                        }
                    }
                    else -> null
                }
                estado?.let {
                    Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        confirmButton = {
            if (info.puedeApelar) {
                Button(onClick = onAppeal) { Text(stringResource(R.string.appeal_action)) }
            } else {
                Button(onClick = onDismiss) { Text(stringResource(R.string.report_success_button)) }
            }
        },
        dismissButton = if (info.puedeApelar) {
            { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_close)) } }
        } else {
            null
        }
    )
}
