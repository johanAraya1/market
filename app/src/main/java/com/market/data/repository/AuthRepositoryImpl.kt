package com.market.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.market.data.remote.AuthDataSource
import com.market.domain.model.User
import com.market.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: AuthDataSource,
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override fun getCurrentUser(): User? {
        val firebaseUser = firebaseAuth.currentUser ?: return null
        return User(
            uid = firebaseUser.uid,
            displayName = firebaseUser.displayName ?: "",
            email = firebaseUser.email ?: "",
            photoUrl = firebaseUser.photoUrl?.toString() ?: ""
        )
    }

    override suspend fun signIn(): Result<User> = runCatching {
        // SignIn intent is handled by the UI layer
        // This method is called after Google returns the account
        throw NotImplementedError("Use signInWithAccount instead")
    }

    override fun signOut() {
        authDataSource.signOut()
    }
}
