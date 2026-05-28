package com.example.domain.repository

import com.example.domain.model.FirebaseAuthResult
import com.example.domain.model.LoginResolveResult
import com.example.domain.model.ResetLookupResult
import com.example.domain.model.TransactionDetails
import com.example.domain.model.User
import com.example.domain.model.UserSearchResult
import com.example.domain.model.Wallet

interface AuthRepository {
    suspend fun authenticateWithFirebase(
        idToken: String,
        nickname: String?,
        phone: String?
    ): FirebaseAuthResult

    suspend fun resolveLogin(login: String): LoginResolveResult

    suspend fun lookupForPasswordReset(login: String): ResetLookupResult

    suspend fun completeProfile(userId: Long, nickname: String, phone: String?): User

    suspend fun findById(userId: Long): User?
}

interface UserRepository {
    suspend fun searchUsers(query: String, excludeUserId: Long): List<UserSearchResult>
}

interface WalletRepository {
    suspend fun getWalletByUserId(userId: Long): Wallet?
    suspend fun deposit(userId: Long, amount: java.math.BigDecimal): Wallet
    suspend fun transfer(fromUserId: Long, toUsername: String, amount: java.math.BigDecimal, description: String?): Wallet
    suspend fun getTransactions(userId: Long): List<TransactionDetails>
}
