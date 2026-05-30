package com.example.di

import com.example.data.repository.AuthRepositoryImpl
import com.example.data.repository.UserRepositoryImpl
import com.example.data.repository.WalletRepositoryImpl
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.UserRepository
import com.example.domain.repository.WalletRepository
import com.example.domain.usecase.CompleteProfileUseCase
import com.example.domain.usecase.DepositUseCase
import com.example.domain.usecase.FirebaseAuthUseCase
import com.example.domain.usecase.GetTransactionsUseCase
import com.example.domain.usecase.GetWalletUseCase
import com.example.domain.usecase.LookupPasswordResetUseCase
import com.example.domain.usecase.ResolveLoginUseCase
import com.example.domain.usecase.SearchUsersUseCase
import com.example.domain.usecase.TransferUseCase

class AppModule {
    val authRepository: AuthRepository = AuthRepositoryImpl()
    val userRepository: UserRepository = UserRepositoryImpl()
    val walletRepository: WalletRepository = WalletRepositoryImpl()

    val firebaseAuthUseCase = FirebaseAuthUseCase(authRepository)
    val completeProfileUseCase = CompleteProfileUseCase(authRepository)
    val resolveLoginUseCase = ResolveLoginUseCase(authRepository)
    val lookupPasswordResetUseCase = LookupPasswordResetUseCase(authRepository)
    val searchUsersUseCase = SearchUsersUseCase(userRepository)
    val getWalletUseCase = GetWalletUseCase(walletRepository)
    val depositUseCase = DepositUseCase(walletRepository)
    val transferUseCase = TransferUseCase(walletRepository)
    val getTransactionsUseCase = GetTransactionsUseCase(walletRepository)
}
