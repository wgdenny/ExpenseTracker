package com.example.budgetnatorv2

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.budgetnatorv2.data.database.AppDatabase
import com.example.budgetnatorv2.transaction.TransactionViewModel
import createPieChart

@Composable
fun InsightsScreen() {
    val context = LocalContext.current
    val transactionsDao = AppDatabase.getDatabase(context).transactionsDao
    val transactionViewModel: TransactionViewModel =
        viewModel(factory = TransactionViewModel.TransactionViewModelFactory(transactionsDao))

    // Collecting and observing the sum of transactions monthly, initialized with an empty map
    val allTransactionsSum by transactionViewModel.getMonthlyTransactionsSum()
        .collectAsState(initial = emptyMap())

    // State to toggle between pie chart and line graph
    var showPieChart by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Toggle buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Button(
                onClick = { showPieChart = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (showPieChart) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Pie Chart")
            }
            Button(
                onClick = { showPieChart = false },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!showPieChart) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Line Graph")
            }
        }

        // Dynamic display area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (showPieChart) {
                createPieChart() // Function to display the pie chart
            } else {
                Column {
                    Text("Line Graph")
                    Spacer(modifier = Modifier.height(10.dp))
                    // Display the line graph and bar chart
                    CustomLineChart(allTransactionsSum)
                    CustomBarChart(data = allTransactionsSum)
                }
//                CustomLineChart(allTransactionsSum)
//                CustomBarChart(data = allTransactionsSum)
            }
        }
    }
}

@Composable
@Preview
fun InsightsScreenPreview() {
    InsightsScreen()
}


@Composable
fun CustomBarChart(data: Map<String, Double>) {
    val maxValue = data.maxOfOrNull { it.value } ?: 1.0
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    ) {
        val barWidth = size.width / data.size
        data.entries.forEachIndexed { index, entry ->
            val barHeight = (entry.value / maxValue) * size.height
            drawRoundRect(
                color = Color.Blue,
                topLeft = Offset(x = index * barWidth, y = (size.height - barHeight).toFloat()),
                size = Size(width = barWidth * 0.8f, height = barHeight.toFloat()),
                cornerRadius = CornerRadius(20f, 20f)
            )

            drawContext.canvas.nativeCanvas.apply {
                save()
                rotate(45f, index * barWidth + barWidth * 0.4f, size.height)
                drawText(entry.key, // Month label
                    index * barWidth + barWidth * 0.5f, size.height + 10f, Paint().apply {
                        color = android.graphics.Color.BLACK
                        textAlign = Paint.Align.LEFT
                        textSize = 30f
                    })
                restore()
            }
        }
    }
}

@Composable
fun CustomLineChart(monthlyTransactions: Map<String, Double>) {
    val maxValue = (monthlyTransactions.maxOfOrNull { it.value }
        ?: 1.0).toFloat()  // Determine the max transaction value for scaling

    // Create a list of points scaled based on the max value
    val points = monthlyTransactions.entries.map { it.value.toFloat() / maxValue }

    // Drawing the line chart using Canvas
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(16.dp)
    ) {
        val widthPerPoint =
            size.width / (monthlyTransactions.size - 1)  // Calculate the width per point
        val height = size.height

        // Building the path for the line chart
        val path = Path().apply {
            if (points.isNotEmpty()) {
                moveTo(0f, height - (points.first() * height))
                for (i in 1 until points.size) {
                    val x = widthPerPoint * i
                    val y = height - (points[i] * height)
                    lineTo(x, y)
                }
            }
        }

        // Draw the path with specific styling
        drawPath(
            path = path, color = Color.Blue, style = Stroke(width = 4f, cap = StrokeCap.Round)
        )

        // Annotate each point with a circle and text
        monthlyTransactions.entries.forEachIndexed { index, entry ->
            val x = widthPerPoint * index
            val y = height - (entry.value.toFloat() / maxValue * height)
            drawCircle(color = Color.Red, center = Offset(x, y), radius = 8f)

            // Drawing transaction values as text above the points
            drawContext.canvas.nativeCanvas.drawText("$${entry.value}", x, y - 20f, Paint().apply {
                color = android.graphics.Color.BLACK
                textAlign = Paint.Align.CENTER
                textSize = 30f
            })
        }
    }
}

