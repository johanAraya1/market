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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

private const val TAG = "CreateHousehold"

@Composable
fun CreateHouseholdScreen(
    onHouseholdCreated: () -> Unit,
    onNavigateToJoin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var householdName by remember { mutableStateOf("") }
    var logText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var triggerCreate by remember { mutableStateOf(0) }

    LaunchedEffect(triggerCreate) {
        if (triggerCreate == 0) return@LaunchedEffect
        isLoading = true
        logText = ""
        try {
            withContext(Dispatchers.Main) { logText = "1: Starting coroutine..." }

            val user = withContext(Dispatchers.IO) { FirebaseAuth.getInstance().currentUser }
            withContext(Dispatchers.Main) { logText = "2: user=${user?.uid ?: "NULL"}" }

            if (user == null) {
                withContext(Dispatchers.Main) { logText = "ERROR: No user"; isLoading = false }
                return@LaunchedEffect
            }

            val db = withContext(Dispatchers.IO) { FirebaseFirestore.getInstance() }
            withContext(Dispatchers.Main) { logText = "3: Firestore OK" }

            val ref = withContext(Dispatchers.IO) { db.collection("households").document() }
            withContext(Dispatchers.Main) { logText = "4: ref=${ref.id}" }

            withContext(Dispatchers.Main) { logText = "5: calling set().await()..." }
            withContext(Dispatchers.IO) {
                withTimeout(15_000L) {
                    ref.set(
                        mapOf(
                            "name" to householdName.trim(),
                            "createdAt" to System.currentTimeMillis(),
                            "createdBy" to user.uid
                        )
                    ).await()
                }
            }
            withContext(Dispatchers.Main) { logText = "6: Firestore write DONE!" }

            withContext(Dispatchers.Main) {
                isLoading = false
                onHouseholdCreated()
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Log.e(TAG, "TIMEOUT", e)
            withContext(Dispatchers.Main) { logText = "TIMEOUT after 15s — Firestore not responding"; isLoading = false }
        } catch (e: Throwable) {
            Log.e(TAG, "ERROR: ${e.javaClass.simpleName}: ${e.message}", e)
            withContext(Dispatchers.Main) { logText = "ERROR: ${e.javaClass.simpleName}: ${e.message}"; isLoading = false }
        }
    }

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
            onValueChange = { if (it.length <= 50) householdName = it },
            label = { Text("Nombre del hogar") },
            supportingText = { Text("${householdName.length}/50 caracteres") },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (logText.isNotEmpty()) {
            Text(
                text = logText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { triggerCreate++ },
            modifier = Modifier.fillMaxWidth(),
            enabled = householdName.isNotBlank() && !isLoading
        ) {
            Text(if (isLoading) "Creando..." else "Crear hogar")
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
