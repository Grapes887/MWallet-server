package com.example.data.repository

import com.example.data.local.database.UsersTable
import com.example.data.mapper.normalizePhone
import com.example.domain.model.UserSearchResult
import com.example.domain.repository.UserRepository
import org.jetbrains.exposed.sql.LikePattern
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class UserRepositoryImpl : UserRepository {

    override suspend fun searchUsers(query: String, excludeUserId: Long): List<UserSearchResult> =
        newSuspendedTransaction {
            val trimmed = query.trim()
            if (trimmed.length < 2) {
                throw com.example.domain.exception.ApiException(400, "Введите минимум 2 символа")
            }

            val digitsOnly = trimmed.filter { it.isDigit() }
            val isPhoneQuery = digitsOnly.length >= trimmed.length / 2 && digitsOnly.length >= 3

            val rows = if (isPhoneQuery) {
                val phonePattern = "%${digitsOnly}%"
                UsersTable.selectAll().where {
                    (UsersTable.phone like LikePattern(phonePattern)) and
                        (UsersTable.id neq excludeUserId)
                }
            } else {
                val nicknamePattern = "%${trimmed.lowercase()}%"
                UsersTable.selectAll().where {
                    (UsersTable.username.lowerCase() like LikePattern(nicknamePattern)) and
                        (UsersTable.id neq excludeUserId)
                }
            }

            rows.limit(20).map { row ->
                UserSearchResult(
                    id = row[UsersTable.id].value,
                    username = row[UsersTable.username],
                    phone = row[UsersTable.phone]
                )
            }
        }
}
