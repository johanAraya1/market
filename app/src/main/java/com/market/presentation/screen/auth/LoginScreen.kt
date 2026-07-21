package com.market.presentation.screen.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.market.R
import com.market.data.remote.AuthDataSource
import com.market.presentation.component.LoadingIndicator
import com.market.presentation.theme.Teal
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onSignInSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                isLoading = true
                errorMessage = null
                scope.launch {
                    try {
                        // TODO: inject AuthDataSource properly via ViewModel
                        val authDataSource = AuthDataSource(
                            com.google.firebase.auth.FirebaseAuth.getInstance(),
                            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        )
                        authDataSource.firebaseAuthWithGoogle(account)
                        isLoading = false
                        onSignInSuccess()
                    } catch (e: Exception) {
                        isLoading = false
                        errorMessage = "Error: ${e.message ?: e.javaClass.simpleName}"
                    }
                }
            } catch (e: ApiException) {
                errorMessage = "Google error: ${e.statusCode} - ${e.message}"
            }
        }
    }

    if (isLoading) {
        LoadingIndicator()
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App logo
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

        // Google Sign-In button
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
            colors = ButtonDefaults.buttonColors(
                containerColor = Teal
            )
        ) {
            Text("Iniciar sesión con Google")
        }

        errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { errorMessage = null }
            ) {
                Text("Reintentar")
            }
        }
    }
}
