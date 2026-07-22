package com.market.presentation.screen.household

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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
    var logText by remember { mutableStateOf("Esperando...") }
    var showAlert by remember { mutableStateOf(false) }
    var alertMsg by remember { mutableStateOf("") }

    // Use a custom scope with CoroutineExceptionHandler to catch EVERYTHING
    val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        val msg = "HANDLER: ${throwable.javaClass.simpleName}: ${throwable.message}"
        Log.e(TAG, msg, throwable)
        logText += "\n$msg"
    }
    val scope = rememberCoroutineScope { SupervisorJob() + exceptionHandler }

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
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // On-screen log
        Text(
            text = logText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.tertiary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                logText = ""
                scope.launch {
                    try {
                        logText += "1: coroutine started\n"

                        logText += "2: FirebaseAuth.getInstance()...\n"
                        val auth = FirebaseAuth.getInstance()
                        val user = auth.currentUser
                        logText += "3: user=${user?.uid ?: "NULL"}\n"

                        logText += "4: FirebaseFirestore.getInstance()...\n"
                        val db = FirebaseFirestore.getInstance()
                        logText += "5: db OK\n"

                        logText += "6: creating doc ref...\n"
                        val ref = db.collection("households").document()
                        logText += "7: ref.id=${ref.id}\n"

                        logText += "8: calling .set().await()...\n"
                        ref.set(
                            mapOf(
                                "name" to householdName.trim(),
                                "createdAt" to System.currentTimeMillis(),
                                "createdBy" to (user?.uid ?: "")
                            )
                        ).await()
                        logText += "9: .await() returned OK!\n"

                        logText += "10: SUCCESS! navigating...\n"
                        onHouseholdCreated()

                    } catch (e: Throwable) {
                        val msg = "CATCH: ${e.javaClass.simpleName}: ${e.message}"
                        Log.e(TAG, msg, e)
                        logText += "\n$msg"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = householdName.isNotBlank()
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
