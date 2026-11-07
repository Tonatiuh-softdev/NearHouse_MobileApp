package com.equipo4.nearhome.ui.admin.users_crud.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.equipo4.nearhome.R
import com.equipo4.nearhome.domain.model.UserAccountStatus
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.text.font.FontWeight

/** Botones de acción. Abren una pantalla de confirmación; nada se ejecuta aquí directamente. */
@Composable
fun UserActionsBar(
    status: UserAccountStatus,
    onSuspend: () -> Unit,
    onUnsuspend: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
        Column(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (status == UserAccountStatus.SUSPENDIDO) {
                Button(
                    onClick = onUnsuspend,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringResource(R.string.admin_user_action_unsuspend), fontWeight = FontWeight.Bold)
                }
            } else {
                OutlinedButton(
                    onClick = onSuspend,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.admin_user_action_suspend), fontWeight = FontWeight.Bold)
                }
            }
            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(R.string.admin_user_action_delete), fontWeight = FontWeight.Bold)
            }
        }
    }
}