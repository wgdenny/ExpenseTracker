package com.example.budgetnatorv2.data.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTransaction(transactions: Transactions)

    @Delete
    suspend fun deleteTransaction(transactions: Transactions)

    @Query("SELECT * FROM Transactions")
    fun getAllTransactions(): Flow<List<Transactions>>

    @Query("SELECT * FROM Transactions ORDER BY transactionDate")
    fun getTransactionsByDate(): Flow<List<Transactions>>

    @Query("SELECT * FROM Transactions WHERE id = :id")
    fun getTransactionById(id: String): Flow<Transactions?>

}