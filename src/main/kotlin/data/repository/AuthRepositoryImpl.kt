package com.example.data.repository

import com.example.data.firebase.FirebaseService
import com.example.data.local.database.UsersTable
import com.example.data.local.database.WalletsTable
import com.example.data.mapper.normalizePhone
import com.example.data.mapper.toUser
import com.example.domain.exception.ApiException
import com.example.domain.model.FirebaseAuthResult
import com.example.domain.model.LoginResolveResult
import com.example.domain.model.ResetLookupResult
import com.example.domain.model.User
import com.example.domain.repository.AuthRepository
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import java.math.BigDecimal

class AuthRepositoryImpl : AuthRepository {

    override suspend fun authenticateWithFirebase(
        idToken: String,
        nickname: String?,
        phone: String?
    ): FirebaseAuthResult = newSuspendedTransaction {
        val token = FirebaseService.verifyIdToken(idToken)
        val firebaseUid = token.uid
        val email = token.email
        val firebasePhone = (token.claims["phone_number"] as? String)?.let { normalizePhone(it) }
        val resolvedPhone = phone?.takeIf { it.isNotBlank() }?.let { normalizePhone(it) } ?: firebasePhone

        val existing = UsersTable.selectAll()
            .where { UsersTable.firebaseUid eq firebaseUid }
            .singleOrNull()
            ?.toUser()

        if (existing != null) {
            return@newSuspendedTransaction FirebaseAuthResult(
                user = existing,
                profileComplete = true
            )
        }

        val trimmedNickname = nickname?.trim()
        if (trimmedNickname.isNullOrBlank()) {
            throw ApiException(400, "Укажите никнейм для нового аккаунта")
        }
        validateNickname(trimmedNickname)

        val nicknameTaken = UsersTable.selectAll()
            .where { UsersTable.username eq trimmedNickname }
            .count()
        if (nicknameTaken > 0) {
            throw ApiException(409, "Никнейм уже занят")
        }

        resolvedPhone?.let { p ->
            val phoneTaken = UsersTable.selectAll().where { UsersTable.phone eq p }.count()
            if (phoneTaken > 0) throw ApiException(409, "Номер телефона уже зарегистрирован")
        }

        val userId = UsersTable.insert {
            it[UsersTable.firebaseUid] = firebaseUid
            it[UsersTable.username] = trimmedNickname
            it[UsersTable.email] = email
            it[UsersTable.phone] = resolvedPhone
        } get UsersTable.id

        WalletsTable.insert {
            it[WalletsTable.userId] = userId.value
            it[WalletsTable.balance] = BigDecimal.ZERO
            it[WalletsTable.currency] = "RUB"
        }

        val user = UsersTable.selectAll().where { UsersTable.id eq userId }.single().toUser()
        FirebaseAuthResult(user = user, profileComplete = true)
    }

    override suspend fun completeProfile(userId: Long, nickname: String, phone: String?): User =
        newSuspendedTransaction {
            val trimmedNickname = nickname.trim()
            validateNickname(trimmedNickname)

            val user = UsersTable.selectAll()
                .where { UsersTable.id eq userId }
                .singleOrNull()
                ?.toUser()
                ?: throw ApiException(404, "Пользователь не найден")

            val nicknameTaken = UsersTable.selectAll()
                .where { (UsersTable.username eq trimmedNickname) and (UsersTable.id neq userId) }
                .count()
            if (nicknameTaken > 0) throw ApiException(409, "Никнейм уже занят")

            val normalizedPhone = phone?.takeIf { it.isNotBlank() }?.let { normalizePhone(it) }
            normalizedPhone?.let { p ->
                val phoneTaken = UsersTable.selectAll()
                    .where { (UsersTable.phone eq p) and (UsersTable.id neq userId) }
                    .count()
                if (phoneTaken > 0) throw ApiException(409, "Номер телефона уже зарегистрирован")
            }

            UsersTable.update({ UsersTable.id eq userId }) {
                it[username] = trimmedNickname
                if (normalizedPhone != null) it[UsersTable.phone] = normalizedPhone
            }

            UsersTable.selectAll().where { UsersTable.id eq userId }.single().toUser()
        }

    override suspend fun findById(userId: Long): User? = newSuspendedTransaction {
        UsersTable.selectAll()
            .where { UsersTable.id eq userId }
            .singleOrNull()
            ?.toUser()
    }

    override suspend fun resolveLogin(login: String): LoginResolveResult = newSuspendedTransaction {
        val user = findUserByLogin(login)
            ?: throw ApiException(404, "Пользователь не найден")
        val email = user.email?.takeIf { it.isNotBlank() }
            ?: throw ApiException(400, "У аккаунта не указан email для входа по паролю")
        LoginResolveResult(email = email, username = user.username)
    }

    override suspend fun lookupForPasswordReset(login: String): ResetLookupResult = newSuspendedTransaction {
        val user = findUserByLogin(login)
            ?: throw ApiException(404, "Пользователь не найден")
        val email = user.email?.takeIf { it.isNotBlank() }
        val phone = user.phone?.takeIf { it.isNotBlank() }
        if (email == null && phone == null) {
            throw ApiException(400, "У аккаунта нет email или телефона для восстановления")
        }
        ResetLookupResult(
            email = email,
            phone = phone,
            maskedEmail = email?.let { maskEmail(it) },
            maskedPhone = phone?.let { maskPhone(it) },
            canResetByEmail = email != null,
            canResetByPhone = phone != null
        )
    }

    private fun findUserByLogin(login: String): User? {
        val trimmed = login.trim()
        return when {
            trimmed.contains("@") -> UsersTable.selectAll()
                .where { UsersTable.email eq trimmed }
                .singleOrNull()?.toUser()

            trimmed.filter { it.isDigit() }.length >= trimmed.length / 2 && trimmed.any { it.isDigit() } -> {
                val normalized = normalizePhone(trimmed)
                UsersTable.selectAll()
                    .where { UsersTable.phone eq normalized }
                    .singleOrNull()?.toUser()
            }

            else -> UsersTable.selectAll()
                .where { UsersTable.username eq trimmed }
                .singleOrNull()?.toUser()
        }
    }

    private fun maskEmail(email: String): String {
        val parts = email.split("@")
        if (parts.size != 2) return "***"
        val local = parts[0]
        val maskedLocal = if (local.length <= 1) "*" else "${local.first()}***"
        return "$maskedLocal@${parts[1]}"
    }

    private fun maskPhone(phone: String): String {
        val digits = phone.filter { it.isDigit() }
        return if (digits.length >= 4) "***${digits.takeLast(4)}" else "***"
    }

    private fun validateNickname(nickname: String) {
        if (nickname.length < 3) {
            throw ApiException(400, "Никнейм — минимум 3 символа")
        }
        if (!nickname.matches(Regex("^[a-zA-Z0-9_]+$"))) {
            throw ApiException(400, "Никнейм: только латиница, цифры и _")
        }
    }
}
