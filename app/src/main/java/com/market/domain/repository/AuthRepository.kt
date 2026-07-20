package com.market.domain.repository

import com.market.domain.model.User

interface AuthRepository {
    fun getCurrentUser(): User?
    suspend fun signIn(): Result<User>
    fun signOut()
}
