package com.example.domain.usecase

import com.example.domain.exception.ApiException
import com.example.domain.model.FirebaseAuthResult
import com.example.domain.model.LoginResolveResult
import com.example.domain.model.ResetLookupResult
import com.example.domain.model.TransactionDetails
import com.example.domain.model.User
import com.example.domain.model.UserSearchResult
import com.example.domain.model.Wallet
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.UserRepository
import com.example.domain.repository.WalletRepository
import java.math.BigDecimal

class FirebaseAuthUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(idToken: String, nickname: String?, phone: String?): FirebaseAuthResult =
        authRepository.authenticateWithFirebase(idToken, nickname, phone)
}

class CompleteProfileUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(userId: Long, nickname: String, phone: String?): User =
        authRepository.completeProfile(userId, nickname, phone)
}

class SearchUsersUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(query: String, excludeUserId: Long): List<UserSearchResult> =
        userRepository.searchUsers(query, excludeUserId)
}

class ResolveLoginUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(login: String): LoginResolveResult {
        if (login.isBlank()) throw ApiException(400, "Введите логин, email или телефон")
        return authRepository.resolveLogin(login.trim())
    }
}

class LookupPasswordResetUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(login: String): ResetLookupResult {
        if (login.isBlank()) throw ApiException(400, "Введите логин, email или телефон")
        return authRepository.lookupForPasswordReset(login.trim())
    }
}

class GetWalletUseCase(private val walletRepository: WalletRepository) {
    suspend operator fun invoke(userId: Long): Wallet =
        walletRepository.getWalletByUserId(userId)
            ?: throw ApiException(404, "Кошелёк не найден")
}

class DepositUseCase(private val walletRepository: WalletRepository) {
    suspend operator fun invoke(userId: Long, amount: BigDecimal): Wallet {
        validateAmount(amount)
        return walletRepository.deposit(userId, amount)
    }
}

class TransferUseCase(private val walletRepository: WalletRepository) {
    suspend operator fun invoke(
        fromUserId: Long,
        toUsername: String,
        amount: BigDecimal,
        description: String?
    ): Wallet {
        validateAmount(amount)
        if (toUsername.isBlank()) throw ApiException(400, "Укажите получателя")
        return walletRepository.transfer(fromUserId, toUsername.trim(), amount, description?.trim())
    }
}

class GetTransactionsUseCase(private val walletRepository: WalletRepository) {
    suspend operator fun invoke(userId: Long): List<TransactionDetails> =
        walletRepository.getTransactions(userId)
}

private fun validateAmount(amount: BigDecimal) {
    if (amount <= BigDecimal.ZERO) {
        throw ApiException(400, "Сумма должна быть больше нуля")
    }
}
