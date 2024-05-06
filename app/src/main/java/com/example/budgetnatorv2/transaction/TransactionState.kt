package com.example.budgetnatorv2.transaction

import com.example.budgetnatorv2.BottomNavItem


data class TransactionState (
    val transactions: List<BottomNavItem.Transactions> = emptyList(),
    val transactionName: String = "",
    val transactionAmount: Double = 0.0,
    val transactionDateString: String = "",
    val transactionType: String = "",
    val empty: Boolean = true
)