package com.example.budgetnatorv2.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.budgetnatorv2.data.model.Bill
import com.example.budgetnatorv2.data.model.BillDao
import com.example.budgetnatorv2.data.model.Converters
import com.example.budgetnatorv2.data.model.Transactions
import com.example.budgetnatorv2.data.model.TransactionsDao

@Database(entities = [Bill::class,Transactions::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val billDao: BillDao
    abstract val transactionsDao: TransactionsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budgetnator_database" // Database name
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}