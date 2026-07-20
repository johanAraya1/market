package com.market.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CheckOffDialog(
    itemName: String,
    isAdmin: Boolean,
    onConfirm: (reason: String) -> Unit,
    onDismiss: () -> Unit
) {
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Marcar como comprado") },
        text = {
            Column {
                Text(
                    text = "\"$itemName\" será marcado como comprado.",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (isAdmin) {
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Motivo (opcional)") },
                        placeholder = { Text("Ej: estaba en oferta") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        maxLines = 2
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(reason) }) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
