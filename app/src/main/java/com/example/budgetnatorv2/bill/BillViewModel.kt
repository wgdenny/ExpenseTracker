package com.example.budgetnatorv2.bill

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.budgetnatorv2.data.model.Bill
import com.example.budgetnatorv2.data.model.BillDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

    class BillViewModel(private val dao: BillDao) : ViewModel() {


    private val _state = MutableStateFlow(BillState())
    val state: StateFlow<BillState> = _state.asStateFlow()

    private val _bills = dao.getAllBills()
        .map { bills ->
            bills.sortedBy { bill ->
                bill.dueDate ?: Date(0) //
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        val bills: StateFlow<List<Bill>> = _bills
    fun onEvent(event: BillEvent) {
        when (event) {
            is BillEvent.SetBillName -> {
                _state.update {
                    it.copy(billName = event.billName)
                }
            }
            is BillEvent.SetBillAmount -> {
                _state.update {
                    it.copy(billAmount = event.billAmount)
                }
            }
            is BillEvent.SetDueDate -> {
                _state.update {
                    it.copy(dueDateString = event.dueDateString)
                }
            }
            is BillEvent.DeleteBill -> {
                viewModelScope.launch {
                    dao.deleteBill(event.bill)
                    // No need to manually update the bills list, it will auto update due to the Flow from the database
                }
            }
            is BillEvent.SaveBill -> {
                val billName = _state.value.billName
                val billAmount = _state.value.billAmount
                val dueDateString = _state.value.dueDateString
                // Check for validity before attempting to save.
                if (billName.isBlank() || billAmount == 0.0 || dueDateString.isBlank()) {
                    Log.d("BillViewModel", "Invalid bill data: Name is blank, Amount is 0, or Due Date is blank.")
                    return
                }
                val format = SimpleDateFormat("MM-dd-yyyy", Locale.getDefault())
                val dueDate: Date? = try {
                    format.parse(dueDateString)
                } catch (e: ParseException) {
                    Log.e("BillViewModel", "Failed to parse date: $dueDateString", e)
                    null
                }
                val bill = Bill(billName = billName, billAmount = billAmount, dueDate = dueDate)

                viewModelScope.launch {
                    try {
                        Log.d("BillViewModel", "Attempting to save bill: $bill")
                        dao.upsertBill(bill)
                        Log.d("BillViewModel", "Bill saved successfully")

                        // Reset the input state after saving.
                        _state.update {
                            it.copy(empty = true, billName = "", billAmount = 0.0, dueDateString = BillState().dueDateString)
                        }
                    } catch (e: Exception) {
                        Log.e("BillViewModel", "Error saving bill: ${e.message}", e)
                    }
                }
            }
        }
    }

    fun getBillById(billId: String?): Flow<Bill?> {
        return if (billId == null) {
            flowOf(null)
        } else {
            dao.getBillById(billId)
        }
    }

    fun saveBill(updatedBill: Bill) {
        viewModelScope.launch {
            try {
                dao.upsertBill(updatedBill)
                Log.d("BillViewModel", "Bill saved successfully")
            } catch (e: Exception) {
                Log.e("BillViewModel", "Error saving bill: ${e.message}", e)
            }
        }
    }

    class BillViewModelFactory(private val billDao: BillDao) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BillViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return BillViewModel(billDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}
