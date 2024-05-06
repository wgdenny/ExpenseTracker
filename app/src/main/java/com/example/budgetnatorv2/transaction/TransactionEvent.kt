package com.example.budgetnatorv2.transaction

import com.example.budgetnatorv2.data.model.Transactions

sealed interface TransactionEvent {
    data object SaveTransaction: TransactionEvent
    data class SetTransactionName(val transactionName: String): TransactionEvent
    data class SetTransactionAmount(val transactionAmount: Double): TransactionEvent
    data class SetTransactionDate(val transactionDateString: String): TransactionEvent
    data class SetTransactionType(val transactionType: String): TransactionEvent
    data class DeleteTransaction(val transactions: Transactions): TransactionEvent
}