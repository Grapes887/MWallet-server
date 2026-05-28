package com.example.data.local.database

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.math.BigDecimal
import java.time.Instant

object UsersTable : LongIdTable("users") {
    val firebaseUid = varchar("firebase_uid", 128).nullable().uniqueIndex()
    val username = varchar("username", 50).uniqueIndex()
    val email = varchar("email", 100).nullable()
    val phone = varchar("phone", 20).nullable().uniqueIndex()
    val createdAt = timestamp("created_at").clientDefault { Instant.now() }
}

object WalletsTable : LongIdTable("wallets") {
    val userId = long("user_id").references(UsersTable.id)
    val balance = decimal("balance", 19, 2).default(BigDecimal.ZERO)
    val currency = varchar("currency", 3).default("RUB")
}

object TransactionsTable : LongIdTable("transactions") {
    val fromWalletId = long("from_wallet_id").nullable()
    val toWalletId = long("to_wallet_id").references(WalletsTable.id)
    val amount = decimal("amount", 19, 2)
    val type = varchar("type", 20)
    val description = text("description").nullable()
    val createdAt = timestamp("created_at").clientDefault { Instant.now() }
}
