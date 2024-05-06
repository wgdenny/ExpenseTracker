package com.example.budgetnatorv2.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "Transactions")
data class Transactions(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val transactionName: String,
    val transactionAmount: Double,
    val transactionDate: Date?,
    val transactionType: String
)