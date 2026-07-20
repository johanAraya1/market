package com.market.presentation.screen.price

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PriceEntryDialog(
    itemName: String,
    storeName: String,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var hasError by remember { mutableStateOf(false) }

    val crcFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("es", "CR")).apply {
            maximumFractionDigits = 0
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar precio") },
        text = {
            OutlinedTextField(
                value = amountText,
                onValueChange = { newValue ->
                    // Only allow digits
                    if (newValue.all { it.isDigit() }) {
                        amountText = newValue
                        hasError = false
                    }
                },
                label = { Text("Precio en colones") },
                placeholder = {
                    Text(crcFormat.format(0).replace("₡", "").trim())
                },
                prefix = { Text("₡ ") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = hasError,
                supportingText = if (hasError) {
                    { Text("Ingrese un precio mayor a ₡0") }
                } else {
                    { Text("$itemName — $storeName") }
                },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount == null || amount <= 0.0) {
                        hasError = true
                    } else {
                        onConfirm(amount)
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
