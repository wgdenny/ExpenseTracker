package com.example.budgetnatorv2

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.budgetnatorv2.data.database.AppDatabase
import com.example.budgetnatorv2.data.model.Transactions
import com.example.budgetnatorv2.transaction.TransactionEvent
import com.example.budgetnatorv2.transaction.TransactionViewModel
import java.text.DateFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

// fun TransactionsScreen is an exact copy of BillsListScreen, except with Bills replaced with
// Transactions
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsListScreen(navController: NavHostController) {
    val context = LocalContext.current
    val appDatabase = AppDatabase.getDatabase(context)
    val transactionsDao = appDatabase.transactionsDao
    val transactionViewModel: TransactionViewModel = viewModel(
        factory = TransactionViewModel.TransactionViewModelFactory(transactionsDao)
    )

    Scaffold(topBar = { TopAppBar(title = { Text("Transactions") }) }, floatingActionButton = {
        FloatingActionButton(onClick = {
            // Navigate to the screen for adding a new transaction.
            navController.navigate("add_transaction")
        }) {
            Icon(Icons.Filled.Add, contentDescription = "Add Transaction")
        }
    }) { paddingValues ->
        // Now passing paddingValues to DisplayTransactions
        DisplayTransactions(
            transactionViewModel = transactionViewModel,
            navController = navController,
            contentPadding = paddingValues
        )
    }
}


@Composable
fun TransactionItem(
    transaction: Transactions,
    viewModel: TransactionViewModel,
    navController: NavHostController,
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .shadow(2.dp),

        ) {
        Row(
            modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Name: " + transaction.transactionName,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Amount $${
                        NumberFormat.getInstance().format(transaction.transactionAmount)
                    }", style = MaterialTheme.typography.bodyMedium
                )
                Text(text = "Date: ${
                    transaction.transactionDate?.let {
                        DateFormat.getDateInstance(DateFormat.MEDIUM).format(it)
                    } ?: "Not set"
                }", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "Type: ${transaction.transactionType}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            IconButton(onClick = {
                navController.navigate("edit_transaction/${transaction.id}")
            }) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit Transaction")
            }
            IconButton(onClick = {
                viewModel.onEvent(
                    TransactionEvent.DeleteTransaction(
                        transaction
                    )
                )
            }) {

            }
        }
    }
}

@Composable
fun EditTransactionDetailsScreen(transactionId: String?, navController: NavHostController) {
    val appContext = LocalContext.current.applicationContext
    val database = AppDatabase.getDatabase(appContext)
    val transactionsDao = database.transactionsDao
    val transactionViewModel: TransactionViewModel = viewModel(
        factory = TransactionViewModel.TransactionViewModelFactory(transactionsDao)
    )
    val transactionToEdit by transactionViewModel.getTransactionById(transactionId)
        .collectAsState(initial = null)
    val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())

    transactionToEdit?.let { transaction ->
        var transactionName by remember { mutableStateOf(transaction.transactionName) }
        var transactionAmount by remember { mutableStateOf(transaction.transactionAmount.toString()) }
        var transactionDateString by remember {
            mutableStateOf(transaction.transactionDate?.let {
                dateFormat.format(
                    it
                )
            } ?: "")
        }
        var transactionType by remember { mutableStateOf(transaction.transactionType) }

        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(value = transactionName,
                onValueChange = { transactionName = it },
                label = { Text("Name") })
            OutlinedTextField(value = transactionAmount,
                onValueChange = { transactionAmount = it },
                placeholder = { Text("Amount ") },
                label = { Text("Amount") })
            OutlinedTextField(value = transactionDateString,
                onValueChange = { transactionDateString = it },
                label = { Text("Transaction Date (MM/dd/yyyy)") })
            OutlinedTextField(value = transactionType,
                onValueChange = { transactionType = it },
                label = { Text("Transaction Type") })
            Button(onClick = {
                val updatedTransaction = transaction.copy(
                    transactionName = transactionName,
                    transactionAmount = transactionAmount.toDoubleOrNull()
                        ?: transaction.transactionAmount,
                    transactionDate = try {
                        dateFormat.parse(transactionDateString)
                    } catch (
                        e: Exception, ) {
                        null
                    },
                    transactionType = transactionType
                )
                transactionViewModel.saveTransaction(updatedTransaction)
                navController.popBackStack()
            }) {
                Text("Save Changes")
            }
        }
    } ?: run {
        Text("Transaction not found or loading...")
    }
}