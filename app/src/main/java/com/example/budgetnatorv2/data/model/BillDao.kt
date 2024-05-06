package com.example.budgetnatorv2.data.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBill(bill: Bill)

    // Delete a bill
    @Delete
    suspend fun deleteBill(bill: Bill)

    // Get all bills
    @Query("SELECT * FROM Bill")
    fun getAllBills(): Flow<List<Bill>>

    // Order bills by due date
    @Query("SELECT * FROM Bill ORDER BY dueDate")
    fun getBillsByDueDate(): Flow<List<Bill>>

    @Query("SELECT * FROM bill WHERE id = :id")
    fun getBillById(id: String): Flow<Bill?>
}