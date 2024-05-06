package com.example.budgetnatorv2.transaction

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.budgetnatorv2.data.model.Transactions
import com.example.budgetnatorv2.data.model.TransactionsDao
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

//Copy the code from the BillViewModel.kt file but modify it to work with transactions
class TransactionViewModel(private val dao: TransactionsDao) : ViewModel() {

    private val _state = MutableStateFlow(TransactionState())
    val state: StateFlow<TransactionState> = _state.asStateFlow()

    private val _transactions = dao.getAllTransactions().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    val transactions: StateFlow<List<Transactions>> = _transactions

    fun onEvent(event: TransactionEvent) {
        when (event) {
            is TransactionEvent.SetTransactionName -> {
                _state.update {
                    it.copy(transactionName = event.transactionName)
                }
            }

            is TransactionEvent.SetTransactionAmount -> {
                _state.update {
                    it.copy(transactionAmount = event.transactionAmount)
                }
            }

            is TransactionEvent.SetTransactionDate -> {
                _state.update {
                    it.copy(transactionDateString = event.transactionDateString)
                }
            }

            is TransactionEvent.SetTransactionType -> {
                _state.update {
                    it.copy(transactionType = event.transactionType)
                }
            }

            is TransactionEvent.DeleteTransaction -> {
                viewModelScope.launch {
                    dao.deleteTransaction(event.transactions)
                }
            }

            is TransactionEvent.SaveTransaction -> {
                val transactionName = _state.value.transactionName
                val transactionAmount = _state.value.transactionAmount
                val transactionDateString = _state.value.transactionDateString
                val transactionType = _state.value.transactionType

                if (transactionName.isBlank() || transactionAmount == 0.0 || transactionDateString.isBlank()) {
                    Log.d(
                        "TransactionViewModel",
                        "Invalid transaction data: Name is blank, " + "Amount is 0," + " or Date is blank."
                    )
                    return
                }
                val format = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                val transactionDate: Date? = try {
                    format.parse(transactionDateString)
                } catch (e: Exception) {
                    Log.e("TransactionViewModel", "Failed to parse date: $transactionDateString", e)
                    null
                }

                val transaction = Transactions(
                    transactionName = transactionName,
                    transactionAmount = transactionAmount,
                    transactionDate = transactionDate,
                    transactionType = transactionType
                )

                viewModelScope.launch {
                    try {
                        Log.d(
                            "TransactionViewModel",
                            "Attempting to save transaction: $transaction"
                        )
                        dao.upsertTransaction(transaction)
                        Log.d("TransactionViewModel", "Transaction saved successfully")

                        _state.update {
                            it.copy(
                                empty = true,
                                transactionName = "",
                                transactionAmount = 0.0,
                                transactionDateString = TransactionState().transactionDateString,
                                transactionType = ""
                            )
                        }
                    } catch (e: Exception) {
                        Log.e("TransactionViewModel", "Error saving transaction: ${e.message}", e)
                    }
                }
            }
        }
    }

    fun getTransactionById(transactionId: String?): Flow<Transactions?> {
        return if (transactionId == null) {
            flowOf(null)
        } else {
            dao.getTransactionById(transactionId)
        }
    }

    fun saveTransaction(updatedTransaction: Transactions) {
        viewModelScope.launch {
            try {
                dao.upsertTransaction(updatedTransaction)
                Log.d("TransactionViewModel", "Transaction saved successfully")
            } catch (e: Exception) {
                Log.e("TransactionViewModel", "Error saving transaction: ${e.message}", e)
            }
        }
    }

    fun getMonthlyTransactionsSum(): Flow<Map<String, Double>> {
        return _transactions.map { transactions ->
            transactions.groupBy {

                SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(it.transactionDate)
            }.mapValues { entry ->

                entry.value.sumOf { transaction ->
                    transaction.transactionAmount
                }
            }
        }
    }

    fun getMonthlyTransactionsSumByType(): Flow<Map<String, Map<String, Double>>> {
        return _transactions.map { transactions ->
            transactions.groupBy {
                SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(it.transactionDate)
            }.mapValues { entry ->
                entry.value.groupBy {
                    it.transactionType
                }.mapValues { typeEntry ->
                    typeEntry.value.sumOf { transaction ->
                        transaction.transactionAmount
                    }
                }
            }
        }
    }


    class TransactionViewModelFactory(private val dao: TransactionsDao) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST") return TransactionViewModel(dao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }


}