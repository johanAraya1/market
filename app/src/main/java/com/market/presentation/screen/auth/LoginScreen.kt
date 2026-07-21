package com.market.presentation.screen.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.market.R
import com.market.data.remote.AuthDataSource
import com.market.presentation.theme.Teal
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onSignInSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                isLoading = true
                status = "Cuenta seleccionada: ${account.email}"

                scope.launch {
                    try {
                        status = "Creando credencial..."
                        val authDataSource = AuthDataSource(
                            com.google.firebase.auth.FirebaseAuth.getInstance(),
                            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        )
                        status = "Conectando con Firebase..."
                        authDataSource.firebaseAuthWithGoogle(account)
                        status = "Login exitoso!"
                        isLoading = false
                        onSignInSuccess()
                    } catch (e: Exception) {
                        isLoading = false
                        status = "ERROR: ${e.message ?: e.javaClass.simpleName}"
                    }
                }
            } catch (e: ApiException) {
                isLoading = false
                status = "Google error code: ${e.statusCode}"
            }
        } else {
            status = "Cancelado por el usuario (resultCode: ${result.resultCode})"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "M",
            style = MaterialTheme.typography.displayLarge,
            color = Teal
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Market",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tu lista de compras compartida",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {
                val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
                    com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
                )
                    .requestIdToken(context.getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                launcher.launch(googleSignInClient.signInIntent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Teal),
            enabled = !isLoading
        ) {
            Text("Iniciar sesión con Google")
        }

        if (isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }

        if (status.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = status,
                style = MaterialTheme.typography.bodySmall,
                color = if (status.startsWith("ERROR") || status.startsWith("Google error"))
                    MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
