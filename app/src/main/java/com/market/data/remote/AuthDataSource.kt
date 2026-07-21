package com.market.data.remote

import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.market.domain.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthDataSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    fun getCurrentFirebaseUser() = firebaseAuth.currentUser

    fun getSignInIntent(activity: android.app.Activity): Intent {
        val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
            com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
        )
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(activity, gso).signInIntent
    }

    suspend fun firebaseAuthWithGoogle(account: GoogleSignInAccount): User {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        val authResult = firebaseAuth.signInWithCredential(credential).await()
        val firebaseUser = authResult.user
            ?: throw IllegalStateException("Firebase auth returned null user")

        val now = System.currentTimeMillis()
        val user = User(
            uid = firebaseUser.uid,
            displayName = firebaseUser.displayName ?: "",
            email = firebaseUser.email ?: "",
            photoUrl = firebaseUser.photoUrl?.toString() ?: "",
            createdAt = now,
            lastLoginAt = now
        )

        // Create or update user document
        val userDoc = firestore.collection("users").document(firebaseUser.uid)
        val existing = userDoc.get().await()
        if (existing.exists()) {
            userDoc.update("lastLoginAt", now).await()
        } else {
            userDoc.set(mapOf(
                "displayName" to user.displayName,
                "email" to user.email,
                "photoUrl" to user.photoUrl,
                "createdAt" to user.createdAt,
                "lastLoginAt" to user.lastLoginAt
            )).await()
        }

        return user
    }

    fun signOut() {
        firebaseAuth.signOut()
    }
}
