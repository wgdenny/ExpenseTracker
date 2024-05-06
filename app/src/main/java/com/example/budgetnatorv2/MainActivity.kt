package com.example.budgetnatorv2

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.budgetnatorv2.bill.BillEvent
import com.example.budgetnatorv2.bill.BillViewModel
import com.example.budgetnatorv2.data.database.AppDatabase
import com.example.budgetnatorv2.transaction.TransactionEvent
import com.example.budgetnatorv2.transaction.TransactionViewModel


class MainActivity : ComponentActivity() {
//    private val db by lazy {
//        Room.databaseBuilder(
//            applicationContext, AppDatabase::class.java, "contacts.db"
//        ).build()
//    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefsHelper = PreferencesHelper(this)
        var intent: Intent
        if (!prefsHelper.isLoggedIn()) {
            intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        else
            prefsHelper.setLoggedIn(false)

        setContent {
            MainScreen()
        }


//
//        setContent {
//            MainScreen()
    }
}

//}

//NavController
//@Preview(showBackground = true)
//@Composable
//fun MainScreenPreview() {
//    MainScreen()
//}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val billDao = AppDatabase.getDatabase(context).billDao
    val billViewModelFactory = BillViewModel.BillViewModelFactory(billDao)
    val billViewModel: BillViewModel = viewModel(factory = billViewModelFactory)
    val transactionDao = AppDatabase.getDatabase(context).transactionsDao
    val transactionViewModelFactory =
        TransactionViewModel.TransactionViewModelFactory(transactionDao)
    val transactionViewModel: TransactionViewModel =
        viewModel(factory = transactionViewModelFactory)

    Scaffold(topBar = { }, bottomBar = { BottomNavigationBar(navController) }) { innerPadding ->

//              NavHost: This is a container for navigation within a Compose application.
//              It hosts a navigation graph, which is a set of composable destinations that can
//              be navigated to.

        NavHost(

//                navController = navController: This is the NavController that will
//                be used to navigate between composables in the NavHost.

            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)

//                composable("home") { HomeScreen(navController, billViewModel, transactionViewModel) }:
//                This defines a composable destination in your navigation graph. When you navigate to
//                "home", the HomeScreen composable will be shown.

        ) {
            composable("home") {
                HomeScreen(navController, billViewModel, transactionViewModel)
            }

//                The other composable calls define other destinations in your navigation graph. Each
//                destination is associated with a route (a string) and a composable function that will
//                be shown when you navigate to that route.

            composable(BottomNavItem.Transactions.route) { TransactionsListScreen(navController) }
            composable(BottomNavItem.Insights.route) { InsightsScreen() }
            composable(BottomNavItem.Bills.route) { BillsListScreen(navController) }
            composable("add_bill") { AddBillScreen(navController) }

//                The arguments parameter in some composable calls is used to define arguments that can be
//                passed to the destination. For example, in "edit_bill/{billId}", billId is an argument
//                that can be passed to the EditBillDetailsScreen destination.

            composable(
                "edit_bill/{billId}",
                arguments = listOf(navArgument("billId") { type = NavType.StringType })
            ) { backStackEntry ->
                val billId = backStackEntry.arguments?.getString("billId")
                EditBillDetailsScreen(billId = billId, navController = navController)
            }
            composable("add_transaction") { AddTransactionScreen(navController) }

//                The backStackEntry parameter in the lambda function of some composable calls is used to
//                access the arguments passed to the destination. For example, val billId =
//                backStackEntry.arguments?.getString("billId") is used to get the billId argument passed
//                to the EditBillDetailsScreen destination.

            composable(
                "edit_transaction/{transactionId}",
                arguments = listOf(navArgument("transactionId") { type = NavType.StringType })
            ) { backStackEntry ->
                val transactionId = backStackEntry.arguments?.getString("transactionId")
                EditTransactionDetailsScreen(
                    transactionId = transactionId, navController = navController
                )
            }
            composable("upcoming_bills") { UpcomingBillsScreen(navController) }
            composable("Recent_Transactions") { RecentTransactionsScreen(navController) }
        }
    }
}

@Composable
fun AddBillScreen(navController: NavHostController) {
    val appContext = LocalContext.current.applicationContext
    val billDao = AppDatabase.getDatabase(appContext).billDao
    val billViewModel: BillViewModel =
        viewModel(factory = BillViewModel.BillViewModelFactory(billDao))
    val state by billViewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(value = state.billName,
            onValueChange = { newName -> billViewModel.onEvent(BillEvent.SetBillName(newName)) },
            label = { Text("Bill Name") })

        OutlinedTextField(value = state.billAmount.toString(), onValueChange = { newAmount ->
            billViewModel.onEvent(
                BillEvent.SetBillAmount(newAmount.toDoubleOrNull() ?: 0.0)
            )
        }, label = { Text("Bill Amount") })

        OutlinedTextField(value = state.dueDateString,
            onValueChange = { newDate -> billViewModel.onEvent(BillEvent.SetDueDate(newDate)) },
            label = { Text("Due Date") })

        Button(
            onClick = {
                billViewModel.onEvent(BillEvent.SaveBill)
                // Navigate back to the Bills List Screen after saving
                navController.popBackStack()

            }, modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Save Bill")
        }
    }
}

@Composable
fun AddTransactionScreen(navController: NavHostController) {
    val appContext = LocalContext.current.applicationContext
    val transactionsDao = AppDatabase.getDatabase(appContext).transactionsDao
    val transactionsViewModel: TransactionViewModel =
        viewModel(factory = TransactionViewModel.TransactionViewModelFactory(transactionsDao))
    val state by transactionsViewModel.state.collectAsState()

    val transactionTypes = listOf(
        "Rent/Mortgage", "Groceries", "Entertainment", "Transportation", "Retail Purchase"
    )

    // State for the selected transaction type and dropdown menu visibility
    var selectedType by remember { mutableStateOf(transactionTypes[0]) }
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(value = state.transactionName, onValueChange = { newName ->
            transactionsViewModel.onEvent(
                TransactionEvent.SetTransactionName(newName)
            )
        }, label = { Text("Transaction Name") })

        OutlinedTextField(value = state.transactionAmount.toString(), onValueChange = { newAmount ->
            transactionsViewModel.onEvent(
                TransactionEvent.SetTransactionAmount(newAmount.toDoubleOrNull() ?: 0.0)
            )
        }, label = { Text("Transaction Amount") })

        OutlinedTextField(value = state.transactionDateString, onValueChange = { newDate ->
            transactionsViewModel.onEvent(
                TransactionEvent.SetTransactionDate(newDate)
            )
        }, label = { Text("Date MM/DD/YYY") })

        // Dropdown menu for transaction type
        OutlinedTextField(value = state.transactionType, onValueChange = { newType ->
            transactionsViewModel.onEvent(
                TransactionEvent.SetTransactionType(newType)
            )
        }, label = { Text("Transaction Type") })

        Button(
            onClick = {
                transactionsViewModel.onEvent(TransactionEvent.SaveTransaction)
                // Navigate back to the Bills List Screen after saving
                navController.popBackStack()

            }, modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Save Transaction")
        }
    }
}

