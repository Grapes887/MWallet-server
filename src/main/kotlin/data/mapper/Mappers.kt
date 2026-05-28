package com.example.data.mapper

import com.example.domain.model.TransactionDetails
import com.example.domain.model.TransactionType
import com.example.domain.model.User
import com.example.domain.model.Wallet
import org.jetbrains.exposed.sql.ResultRow
import com.example.data.local.database.TransactionsTable
import com.example.data.local.database.UsersTable
import com.example.data.local.database.WalletsTable

fun ResultRow.toUser(): User = User(
    id = this[UsersTable.id].value,
    firebaseUid = this[UsersTable.firebaseUid],
    username = this[UsersTable.username],
    email = this[UsersTable.email],

    phone = this[UsersTable.phone]

)

fun ResultRow.toWallet(): Wallet = Wallet(
    id = this[WalletsTable.id].value,
    userId = this[WalletsTable.userId],
    balance = this[WalletsTable.balance],
    currency = this[WalletsTable.currency]
)

fun ResultRow.toTransactionDetails(
    walletId: Long,
    counterpartyByWalletId: Map<Long, String>
): TransactionDetails {
    val type = TransactionType.valueOf(this[TransactionsTable.type])
    val fromWalletId = this[TransactionsTable.fromWalletId]
    val toWalletId = this[TransactionsTable.toWalletId]
    val amount = this[TransactionsTable.amount]

    val counterparty = when {
        type == TransactionType.DEPOSIT -> "Пополнение"
        fromWalletId == walletId -> counterpartyByWalletId[toWalletId]?.let { "Кому: $it" }
        toWalletId == walletId && fromWalletId != null ->
            counterpartyByWalletId[fromWalletId]?.let { "От: $it" }
        else -> null
    }

    return TransactionDetails(
        id = this[TransactionsTable.id].value,
        amount = amount,
        type = type,
        description = this[TransactionsTable.description],
        counterparty = counterparty,
        createdAt = this[TransactionsTable.createdAt]
    )
}

fun normalizePhone(phone: String): String =
    phone.filter { it.isDigit() || it == '+' }.let { if (it.startsWith("+")) it else "+$it" }
