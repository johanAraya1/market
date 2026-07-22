package com.market.presentation.screen.household

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "CreateHousehold"

@Composable
fun CreateHouseholdScreen(
    onHouseholdCreated: () -> Unit,
    onNavigateToJoin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var householdName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Text(
            text = "Crear hogar",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Crea un nuevo hogar para compartir tu lista de compras",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = householdName,
            onValueChange = {
                if (it.length <= 50) householdName = it
                errorMessage = null
            },
            label = { Text("Nombre del hogar") },
            supportingText = {
                Text("${householdName.length}/50 caracteres")
            },
            isError = errorMessage != null,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        )

        errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                // DEBUG TEST: just show status, no Firestore
                errorMessage = "Boton clickeado! Nombre: '$householdName'"
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = householdName.isNotBlank() && !isLoading
        ) {
            Text("Crear hogar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onNavigateToJoin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("¿Tienes un código? Unirse a un hogar")
        }
    }
}
