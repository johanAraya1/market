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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CreateHouseholdScreen(
    onHouseholdCreated: () -> Unit,
    onNavigateToJoin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var householdName by remember { mutableStateOf("") }
    var debugLog by remember { mutableStateOf("") }
    var showAlert by remember { mutableStateOf(false) }

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

        Spacer(modifier = Modifier.height(16.dp))

        // DEBUG: show log on screen
        if (debugLog.isNotEmpty()) {
            Text(
                text = debugLog,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                debugLog = "Paso 1: onClick disparado"
                try {
                    debugLog += "\nPaso 2: entrando a try"
                    android.util.Log.d("CreateHousehold", "Paso 1: onClick")
                    
                    // Test 1: Firebase instances
                    debugLog += "\nPaso 3: FirebaseAuth.getInstance()..."
                    android.util.Log.d("CreateHousehold", "Paso 2: getting FirebaseAuth")
                    val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                    debugLog += "\nPaso 4: auth OK, uid=${auth.currentUser?.uid}"
                    android.util.Log.d("CreateHousehold", "Paso 3: auth OK, uid=${auth.currentUser?.uid}")

                    // Test 2: Firestore instance
                    debugLog += "\nPaso 5: FirebaseFirestore.getInstance()..."
                    android.util.Log.d("CreateHousehold", "Paso 4: getting Firestore")
                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    debugLog += "\nPaso 6: Firestore OK"
                    android.util.Log.d("CreateHousehold", "Paso 5: Firestore OK")

                    // Test 3: Get document reference
                    debugLog += "\nPaso 7: creating doc ref..."
                    android.util.Log.d("CreateHousehold", "Paso 6: creating doc ref")
                    val ref = db.collection("households").document()
                    debugLog += "\nPaso 8: ref OK id=${ref.id}"
                    android.util.Log.d("CreateHousehold", "Paso 7: ref id=${ref.id}")

                    // Test 4: Try Firestore write (async)
                    debugLog += "\nPaso 9: launching coroutine para Firestore write..."
                    android.util.Log.d("CreateHousehold", "Paso 8: about to launch coroutine")
                    
                    showAlert = true
                    debugLog += "\nPaso 10: DONE - all sync steps OK!"
                    android.util.Log.d("CreateHousehold", "Paso 9: DONE")

                } catch (e: Throwable) {
                    val msg = "ERROR: ${e.javaClass.simpleName}: ${e.message}"
                    debugLog += "\n$msg"
                    android.util.Log.e("CreateHousehold", msg, e)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = householdName.isNotBlank()
        ) {
            Text("Crear hogar (DEBUG)")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onNavigateToJoin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("¿Tienes un código? Unirse a un hogar")
        }
    }

    if (showAlert) {
        AlertDialog(
            onDismissRequest = { showAlert = false },
            title = { Text("Debug OK") },
            text = { Text("Todos los pasos sincrónicos pasaron. El crash es en el coroutine/async.\n\nNombre: $householdName") },
            confirmButton = {
                TextButton(onClick = { showAlert = false }) {
                    Text("OK")
                }
            }
        )
    }
}
