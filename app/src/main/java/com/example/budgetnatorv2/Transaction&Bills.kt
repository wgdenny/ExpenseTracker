package com.example.budgetnatorv2

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.budgetnatorv2.bill.BillViewModel
import com.example.budgetnatorv2.data.database.AppDatabase
import com.example.budgetnatorv2.transaction.TransactionViewModel



@Composable
fun HomeScreen(
    navController: NavHostController,
    billViewModel: BillViewModel,
    transactionViewModel: TransactionViewModel // Now passed as a parameter
) {

    var currentScreen by remember { mutableStateOf("upcoming_bills") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Navigation bar with buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = { currentScreen = "upcoming_bills" }) {
                Text("Upcoming Bills")
            }
            Button(onClick = { currentScreen = "recent_transactions" }) {
                Text("Recent Transactions")
            }
        }

        // Display content based on the current screen
        when (currentScreen) {
            "upcoming_bills" -> DisplayBills(
                billViewModel = billViewModel,
                navController = navController,
                contentPadding = PaddingValues()
            )
            "recent_transactions" -> DisplayTransactions(
                transactionViewModel = transactionViewModel,
                navController = navController,
                contentPadding = PaddingValues()
            )
        }
    }
}


@Composable
fun DisplayBills(
    billViewModel: BillViewModel,
    navController: NavHostController,
    contentPadding: PaddingValues
) {
    val bills by billViewModel.bills.collectAsState()

    LazyColumn(contentPadding = contentPadding) {
        items(bills) { bill ->
            BillItem(bill = bill, viewModel = billViewModel, navController = navController)
        }
    }
}

@Composable
fun DisplayTransactions(
    transactionViewModel: TransactionViewModel,
    navController: NavHostController,
    contentPadding: PaddingValues
) {
    val transactions by transactionViewModel.transactions.collectAsState()

    LazyColumn(contentPadding = contentPadding) {
        items(transactions) { transaction ->
            TransactionItem(transaction = transaction, viewModel = transactionViewModel,
                navController = navController)
        }
    }
}

    @Composable
    fun UpcomingBillsScreen(navController: NavHostController) {
        val context = LocalContext.current
        val billDao = AppDatabase.getDatabase(context).billDao
        val factory = BillViewModel.BillViewModelFactory(billDao)
        val billViewModel: BillViewModel = viewModel(factory = factory)

        Scaffold {
            DisplayBills(
                billViewModel = billViewModel,
                navController = navController,
                contentPadding = it
            )
        }
    }



    @Composable
    fun RecentTransactionsScreen(navController: NavHostController) {
        val context = LocalContext.current
        val transactionsDao = AppDatabase.getDatabase(context).transactionsDao
        val factory = TransactionViewModel.TransactionViewModelFactory(transactionsDao)
        val transactionViewModel: TransactionViewModel = viewModel(factory = factory)

        Scaffold(


        ) { paddingValues ->
            DisplayTransactions(
                transactionViewModel = transactionViewModel,
                navController = navController,
                contentPadding = paddingValues
            )
        }
    }
