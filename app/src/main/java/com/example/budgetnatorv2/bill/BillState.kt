package com.example.budgetnatorv2.bill

import com.example.budgetnatorv2.data.model.Bill

data class BillState(
    val bills: List<Bill> = emptyList(),
    val billName: String = "",
    val billAmount: Double = 0.0,
    val dueDateString: String = "MM-DD-YYYY",
    val empty: Boolean = true
)
