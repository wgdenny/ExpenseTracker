
package com.example.budgetnatorv2.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "Bill")
data class Bill(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val billName: String,
    val billAmount: Double,
    val dueDate: Date?,
)