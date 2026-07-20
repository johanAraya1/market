package com.market.presentation.screen.household

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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.market.domain.usecase.household.JoinHouseholdUseCase
import kotlinx.coroutines.launch

@Composable
fun JoinHouseholdScreen(
    initialCode: String = "",
    onHouseholdJoined: () -> Unit,
    onNavigateToCreate: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inviteCode by remember { mutableStateOf(initialCode) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(initialCode) {
        if (initialCode.isNotBlank()) {
            inviteCode = initialCode
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Text(
            text = "Unirse a un hogar",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Ingresa el código de invitación de 6 dígitos",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = inviteCode,
            onValueChange = {
                if (it.length <= 6) inviteCode = it
                errorMessage = null
            },
            label = { Text("Código de invitación") },
            supportingText = {
                Text("${inviteCode.length}/6 caracteres")
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
                scope.launch {
                    isLoading = true
                    errorMessage = null
                    val result = JoinHouseholdUseCase(
                        com.market.data.repository.HouseholdRepositoryImpl(
                            com.market.data.remote.HouseholdDataSource(
                                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            ),
                            com.market.data.remote.AuthDataSource(
                                com.google.firebase.auth.FirebaseAuth.getInstance(),
                                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            )
                        )
                    )(inviteCode)
                    isLoading = false
                    if (result.isSuccess) {
                        onHouseholdJoined()
                    } else {
                        errorMessage = result.exceptionOrNull()?.message
                            ?: "Código inválido o expirado"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = inviteCode.length == 6 && !isLoading
        ) {
            Text("Unirse al hogar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onNavigateToCreate,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("¿No tienes código? Crear un hogar")
        }
    }
}
