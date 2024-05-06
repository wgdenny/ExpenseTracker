package com.example.budgetnatorv2

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import com.example.budgetnatorv2.bill.BillEvent
import com.example.budgetnatorv2.bill.BillViewModel
import com.example.budgetnatorv2.data.database.AppDatabase
import com.example.budgetnatorv2.data.model.Bill
import java.text.DateFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillsListScreen(navController: NavHostController){
    val context = LocalContext.current
    val appDatabase = AppDatabase.getDatabase(context)
    val billDao = appDatabase.billDao
    val billViewModel: BillViewModel = viewModel(factory = BillViewModel.BillViewModelFactory(billDao))

    Scaffold(
        topBar = { TopAppBar(title = { Text("Bills") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate("add_bill")
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Bill")
            }
        }
    ) { paddingValues ->
        // Now passing paddingValues to DisplayBills
        DisplayBills(billViewModel = billViewModel, navController = navController, contentPadding = paddingValues)
    }
}


//@Composable
//fun EditBillScreen(bill: Bill, onSave: (Bill) -> Unit) {
//    var billName by remember { mutableStateOf(bill.billName) }
//    var billAmount by remember { mutableStateOf(bill.billAmount.toString()) }
//    var dueDate by remember { mutableStateOf(bill.dueDate ?: "") }
//
//    Column {
//        OutlinedTextField(
//            value = billName,
//            onValueChange = { billName = it },
//            label = { Text("Bill Name") }
//        )
//        OutlinedTextField(
//            value = billAmount,
//            onValueChange = { billAmount = it },
//            label = { Text("Amount") }
//        )
//
//
//        Button(onClick = { onSave(bill.copy(billName = billName, billAmount = billAmount.toDouble())) }) {
//            Text("Save")
//        }
//    }
//}
@Composable
fun EditBillDetailsScreen(billId: String?, navController: NavHostController) {
    val appContext = LocalContext.current.applicationContext
    val database = AppDatabase.getDatabase(appContext)
    val billDao = database.billDao
    // Retrieve ViewModel using Custom Factory
    val billViewModel: BillViewModel = viewModel(
        factory = BillViewModel.BillViewModelFactory(billDao))

    val billToEdit by billViewModel.getBillById(billId).collectAsState(initial = null)
    val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())

    billToEdit?.let { bill ->
        var billName by remember { mutableStateOf(bill.billName)
        }
        var billAmount by remember { mutableStateOf(bill.billAmount.toString())
        }
        var dueDateString by remember { mutableStateOf(bill.dueDate?.let { dateFormat.format(it) } ?: ""
        )}

        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = billName,
                onValueChange = { billName = it },
                label = { Text("Bill Name") }
            )
            OutlinedTextField(
                value = billAmount,
                onValueChange = { billAmount = it },
                label = { Text("Amount") }
            )
            OutlinedTextField(
                value = dueDateString,
                onValueChange = { dueDateString = it },
                label = { Text("Due Date (MM/dd/yyyy)") }
            )
            Button(onClick = {
                val updatedBill = bill.copy(
                    billName = billName,
                    billAmount = billAmount.toDoubleOrNull() ?: bill.billAmount,
                    dueDate = try { dateFormat.parse(dueDateString) } catch (e: Exception) { null }
                )
                billViewModel.saveBill(updatedBill)
                navController.popBackStack()
            }) {
                Text("Save Changes")
            }
        }
    } ?: run {
        Text("Bill not found or loading...")
    }
}

@Composable
fun BillItem(
    bill: Bill,
    viewModel: BillViewModel,
    navController: NavHostController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .shadow(2.dp),
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Payee: ${bill.billName}", style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Amount: $${
                        NumberFormat.getInstance().format(bill.billAmount)
                    }", style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Due Date: ${bill.dueDate?.let { DateFormat.getDateInstance(DateFormat.MEDIUM).format(it) } ?: "Not set"
                    }", style = MaterialTheme.typography.bodySmall
                )
            }
            // Edit button
            IconButton(onClick = {
                navController.navigate("edit_bill/${bill.id}")
            }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            // Delete button
            IconButton(onClick = { viewModel.onEvent(BillEvent.DeleteBill(bill)) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}
