import android.text.TextUtils
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.yml.charts.common.components.Legends
import co.yml.charts.common.model.PlotType
import co.yml.charts.common.utils.DataUtils
import co.yml.charts.ui.piechart.charts.PieChart
import co.yml.charts.ui.piechart.models.PieChartConfig
import co.yml.charts.ui.piechart.models.PieChartData
import com.example.budgetnatorv2.data.database.AppDatabase
import com.example.budgetnatorv2.transaction.TransactionViewModel

@Composable
fun createPieChart() {
    val context = LocalContext.current
    val transactionsDao = AppDatabase.getDatabase(context).transactionsDao

    // ViewModel for handling transaction data, initialized with a factory for the DAO
    val transactionViewModel: TransactionViewModel =
        viewModel(factory = TransactionViewModel.TransactionViewModelFactory(transactionsDao))

    // Collecting and observing the sum of transactions monthly by type, initialized with an empty map
    val transactionsSumByType by transactionViewModel.getMonthlyTransactionsSumByType()
        .collectAsState(initial = emptyMap())

    Surface(
        modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
    ) {
        if (transactionsSumByType.isNotEmpty()) {
            val summedEntries = transactionsSumByType.values.flatMap { it.entries }
                .groupBy({ it.key }, { it.value }).map { (key, values) -> key to values.sum() }

            val pieChartData = PieChartData(
                slices = summedEntries.map { (key, value) ->
                    PieChartData.Slice(key, value.toFloat(), Color(nextColor()))
                }, plotType = PlotType.Pie
            )

            val pieChartConfig = PieChartConfig(
                labelVisible = true,
                strokeWidth = 120f,
                labelColor = Color.Black,
                activeSliceAlpha = .9f,
                isEllipsizeEnabled = true,
                sliceLabelEllipsizeAt = TextUtils.TruncateAt.MIDDLE,
                isAnimationEnable = true,
                showSliceLabels = true,
                chartPadding = 25,
            )

            Column(modifier = Modifier) {
                PieChart(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(500.dp), pieChartData, pieChartConfig
                ) { slice ->
                    Toast.makeText(context, slice.label, Toast.LENGTH_SHORT).show()
                }
                Column(
                    modifier = Modifier.padding(15.dp)
                ) {
                    Legends(
                        legendsConfig = DataUtils.getLegendsConfigFromPieChartData(
                            pieChartData, 3
                        )
                    )
                }
            }
        } else {
            // Display a message or a placeholder when there is no data
            Text(text = "No data available", modifier = Modifier.padding(16.dp))
        }
    }
}

var colorsIndex = 0

fun nextColor(): Int {
    // Toggle through 10 different high-contrast colors
    val colors = listOf(
        Color(0xFFD32F2F),
        Color(0xFF1976D2),
        Color(0xFF388E3C),
        Color(0xFF7B1FA2),
        Color(0xFFFFA000),
        Color(0xFFC2185B),
        Color(0xFF512DA8),
        Color(0xFF303F9F),
        Color(0xFF689F38),
        Color(0xFF455A64)
    )
    if (colorsIndex >= colors.size) colorsIndex = 0
    return colors[colorsIndex++].toArgb()
}

@Composable
@Preview(showSystemUi = true, showBackground = true)
fun PieChartPreview() {
    createPieChart()
}
