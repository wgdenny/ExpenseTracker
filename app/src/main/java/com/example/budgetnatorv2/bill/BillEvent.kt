package com.example.budgetnatorv2.bill

import com.example.budgetnatorv2.data.model.Bill

sealed interface BillEvent {
    data object SaveBill: BillEvent
    data class SetBillName(val billName: String): BillEvent
    data class SetBillAmount(val billAmount: Double): BillEvent
    data class SetDueDate(val dueDateString: String): BillEvent
    data class DeleteBill(val bill: Bill): BillEvent
}