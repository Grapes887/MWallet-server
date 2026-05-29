package com.example.data.repository

import com.example.data.local.database.TransactionsTable
import com.example.data.local.database.UsersTable
import com.example.data.local.database.WalletsTable
import com.example.data.mapper.toTransactionDetails
import com.example.data.mapper.toWallet
import com.example.domain.exception.ApiException
import com.example.domain.model.TransactionDetails
import com.example.domain.model.TransactionType
import com.example.domain.model.Wallet
import com.example.domain.repository.WalletRepository
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import java.math.BigDecimal

class WalletRepositoryImpl : WalletRepository {

    override suspend fun getWalletByUserId(userId: Long): Wallet? = newSuspendedTransaction {
        WalletsTable.selectAll()
            .where { WalletsTable.userId eq userId }
            .singleOrNull()
            ?.toWallet()
    }

    override suspend fun deposit(userId: Long, amount: BigDecimal): Wallet = newSuspendedTransaction {
        val wallet = WalletsTable.selectAll()
            .where { WalletsTable.userId eq userId }
            .singleOrNull()
            ?.toWallet()
            ?: throw ApiException(404, "Кошелёк не найден")

        val newBalance = wallet.balance + amount
        WalletsTable.update({ WalletsTable.id eq wallet.id }) {
            it[balance] = newBalance
        }

        TransactionsTable.insert {
            it[fromWalletId] = null
            it[toWalletId] = wallet.id
            it[TransactionsTable.amount] = amount
            it[type] = TransactionType.DEPOSIT.name
            it[description] = "Пополнение счёта"
        }

        wallet.copy(balance = newBalance)
    }

    override suspend fun transfer(
        fromUserId: Long,
        toUsername: String,
        amount: BigDecimal,
        description: String?
    ): Wallet = newSuspendedTransaction {
        val fromWallet = WalletsTable.selectAll()
            .where { WalletsTable.userId eq fromUserId }
            .singleOrNull()
            ?.toWallet()
            ?: throw ApiException(404, "Кошелёк отправителя не найден")

        val recipient = UsersTable.selectAll()
            .where { UsersTable.username eq toUsername }
            .singleOrNull()
            ?: throw ApiException(404, "Получатель не найден")

        if (recipient[UsersTable.id].value == fromUserId) {
            throw ApiException(400, "Нельзя переводить самому себе")
        }

        val toWallet = WalletsTable.selectAll()
            .where { WalletsTable.userId eq recipient[UsersTable.id].value }
            .singleOrNull()
            ?.toWallet()
            ?: throw ApiException(404, "Кошелёк получателя не найден")

        if (fromWallet.balance < amount) {
            throw ApiException(400, "Недостаточно средств")
        }

        val senderBalance = fromWallet.balance - amount
        val recipientBalance = toWallet.balance + amount

        WalletsTable.update({ WalletsTable.id eq fromWallet.id }) {
            it[balance] = senderBalance
        }
        WalletsTable.update({ WalletsTable.id eq toWallet.id }) {
            it[balance] = recipientBalance
        }

        TransactionsTable.insert {
            it[fromWalletId] = fromWallet.id
            it[toWalletId] = toWallet.id
            it[TransactionsTable.amount] = amount
            it[type] = TransactionType.TRANSFER.name
            it[TransactionsTable.description] = description ?: "Перевод пользователю $toUsername"
        }

        fromWallet.copy(balance = senderBalance)
    }

    override suspend fun getTransactions(userId: Long): List<TransactionDetails> = newSuspendedTransaction {
        val wallet = WalletsTable.selectAll()
            .where { WalletsTable.userId eq userId }
            .singleOrNull()
            ?.toWallet()
            ?: throw ApiException(404, "Кошелёк не найден")

        val counterpartyByWalletId = buildMap {
            WalletsTable.innerJoin(UsersTable).selectAll().forEach { row ->
                put(row[WalletsTable.id].value, row[UsersTable.username])
            }
        }

        TransactionsTable.selectAll()
            .where {
                (TransactionsTable.fromWalletId eq wallet.id) or
                    (TransactionsTable.toWalletId eq wallet.id)
            }
            .orderBy(TransactionsTable.createdAt to SortOrder.DESC)
            .map { row ->
                row.toTransactionDetails(wallet.id, counterpartyByWalletId)
            }
    }
}
