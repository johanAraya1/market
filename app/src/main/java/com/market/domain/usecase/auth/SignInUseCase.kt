package com.market.domain.usecase.auth

import com.market.domain.model.User
import com.market.domain.repository.AuthRepository
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<User> = authRepository.signIn()
}
