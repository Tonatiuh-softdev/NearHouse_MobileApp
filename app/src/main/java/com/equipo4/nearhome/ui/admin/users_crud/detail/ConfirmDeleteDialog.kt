package com.equipo4.nearhome.ui.admin.users_crud.detail

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.equipo4.nearhome.R

/** Confirmación para eliminar. Es irreversible, así que pide un segundo toque. */
@Composable
fun ConfirmDeleteDialog(
    userName: String,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text(stringResource(R.string.admin_user_delete_title)) },
        text = { Text(stringResource(R.string.admin_user_delete_body, userName)) },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isSubmitting) {
                Text(stringResource(R.string.admin_user_delete_confirm), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}