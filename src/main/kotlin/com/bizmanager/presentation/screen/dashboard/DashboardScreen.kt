package com.bizmanager.presentation.screen.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bizmanager.domain.service.CustomerReceivableSnapshot
import com.bizmanager.domain.service.DashboardReport
import com.bizmanager.domain.service.MonthlyTrendPoint
import com.bizmanager.domain.service.ReportService
import com.bizmanager.presentation.ui.toCurrencyLabel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import kotlin.math.abs

private val RevenueColor = Color(0xFF0F766E)
private val CollectionColor = Color(0xFFB45309)
private val ProfitColor = Color(0xFF2563EB)
private val PaidColor = Color(0xFF15803D)
private val OutstandingColor = Color(0xFFD97706)
private val UnpaidColor = Color(0xFFB91C1C)
private val NeutralTrendColor = Color(0xFF64748B)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    reportService: ReportService,
    onNavigateToInvoices: () -> Unit,
    onNavigateToReceivables: () -> Unit,
    onNavigateToLedger: () -> Unit
) {
    var dashboardReport by remember { mutableStateOf<DashboardReport?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        loading = true
        dashboardReport = withContext(Dispatchers.IO) { reportService.getDashboardReport() }
        loading = false
    }

    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val report = dashboardReport ?: return
    val growthInsights = remember(report) { buildGrowthInsights(report.monthlyTrend) }
    val invoiceHealthSlices = remember(report) {
        listOf(
            PieSliceData("Lunas", report.paidInvoiceCount.toFloat(), PaidColor, "${report.paidInvoiceCount} invoice"),
            PieSliceData("Outstanding", report.outstandingInvoiceCount.toFloat(), OutstandingColor, "${report.outstandingInvoiceCount} invoice"),
            PieSliceData("Belum Dibayar", report.unpaidInvoiceCount.toFloat(), UnpaidColor, "${report.unpaidInvoiceCount} invoice")
        )
    }
    val salesMixSlices = remember(report) {
        listOf(
            PieSliceData("Lunas", report.settledSales.toFloat(), RevenueColor, report.settledSales.toCurrencyLabel()),
            PieSliceData("Masih Hutang", report.creditSales.toFloat(), CollectionColor, report.creditSales.toCurrencyLabel())
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text("Dashboard", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Ringkasan performa penjualan, pembayaran, piutang, dan perubahan dari bulan lalu agar kenaikan dan penurunan langsung terlihat.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onNavigateToInvoices) { Text("Buka Invoice") }
            Button(onClick = onNavigateToReceivables) { Text("Monitor Piutang") }
            Button(onClick = onNavigateToLedger) { Text("Buku Besar Customer") }
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardMetricCard(
                title = "Omzet Bulan Ini",
                value = report.periodSummary.totalOmzet.toCurrencyLabel(),
                description = "Nilai total penjualan pada bulan berjalan",
                growth = growthInsights.find { it.label == "Omzet" }
            )
            DashboardMetricCard(
                title = "Laba Bersih",
                value = report.periodSummary.totalNetProfit.toCurrencyLabel(),
                description = "Keuntungan bersih yang tersisa di bulan ini",
                growth = growthInsights.find { it.label == "Profit" }
            )
            DashboardMetricCard(
                title = "Pembayaran Masuk",
                value = report.periodSummary.totalPaymentsReceived.toCurrencyLabel(),
                description = "Dana yang benar-benar masuk pada bulan ini",
                growth = growthInsights.find { it.label == "Pembayaran" }
            )
            DashboardMetricCard(
                title = "Piutang Aktif",
                value = report.activeReceivables.toCurrencyLabel(),
                description = "${report.outstandingInvoiceCount + report.unpaidInvoiceCount} invoice masih perlu ditagih"
            )
            DashboardMetricCard(
                title = "Penjualan Lunas",
                value = report.settledSales.toCurrencyLabel(),
                description = "${report.paidInvoiceCount} invoice sudah lunas"
            )
            DashboardMetricCard(
                title = "Penjualan Masih Hutang",
                value = report.creditSales.toCurrencyLabel(),
                description = "${report.outstandingInvoiceCount + report.unpaidInvoiceCount} invoice masih punya sisa tagihan"
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            InsightCard(
                title = "Tren 6 Bulan Terakhir",
                modifier = Modifier.weight(1.55f)
            ) {
                GrowthHighlights(growthInsights)
                HorizontalDivider()
                LineTrendChart(report.monthlyTrend)
            }
            InsightCard(
                title = "Status Invoice",
                modifier = Modifier.weight(1f)
            ) {
                PieChartSection(
                    slices = invoiceHealthSlices,
                    centerTitle = "Invoice",
                    centerValue = (report.paidInvoiceCount + report.outstandingInvoiceCount + report.unpaidInvoiceCount).toString()
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    "Piutang aktif saat ini ${report.activeReceivables.toCurrencyLabel()}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            InsightCard(
                title = "Komposisi Penjualan",
                modifier = Modifier.weight(1f)
            ) {
                PieChartSection(
                    slices = salesMixSlices,
                    centerTitle = "Sales",
                    centerValue = report.periodSummary.totalOmzet.toCurrencyLabel()
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                BreakdownRow("Penjualan Lunas", report.settledSales)
                BreakdownRow("Penjualan Masih Hutang", report.creditSales)
                BreakdownRow("Piutang Aktif", report.activeReceivables)
            }
            InsightCard(
                title = "Keuntungan Lengkap",
                modifier = Modifier.weight(1f)
            ) {
                ProfitBreakdown(report)
            }
        }

        InsightCard(
            title = "Customer Dengan Piutang Terbesar",
            modifier = Modifier.fillMaxWidth()
        ) {
            TopReceivableCustomers(report.topReceivableCustomers)
        }
    }
}

@Composable
private fun DashboardMetricCard(
    title: String,
    value: String,
    description: String,
    growth: GrowthInsight? = null
) {
    Card(
        modifier = Modifier.width(228.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            if (growth != null) {
                Surface(
                    color = growth.badgeColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = growth.badgeText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        color = growth.badgeColor,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun InsightCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            content()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GrowthHighlights(growthInsights: List<GrowthInsight>) {
    if (growthInsights.isEmpty()) {
        Text(
            "Belum ada data pembanding dari bulan sebelumnya.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        growthInsights.forEach { insight ->
            Surface(
                color = insight.badgeColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(insight.label, style = MaterialTheme.typography.labelLarge)
                    Text(insight.badgeText, color = insight.badgeColor, fontWeight = FontWeight.Bold)
                    Text(
                        "${insight.previousValue.toCurrencyLabel()} -> ${insight.currentValue.toCurrencyLabel()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LineTrendChart(points: List<MonthlyTrendPoint>) {
    if (points.isEmpty()) {
        Text("Belum ada data tren untuk ditampilkan.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }

    val series = listOf(
        ChartSeries("Omzet", RevenueColor, points.map { it.revenue.toFloat() }),
        ChartSeries("Pembayaran", CollectionColor, points.map { it.collections.toFloat() }),
        ChartSeries("Profit", ProfitColor, points.map { it.netProfit.toFloat() })
    )
    val maxValue = series.flatMap { it.values }.maxOrNull()?.takeIf { it > 0f } ?: 1f
    val gridColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            series.forEach { item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(item.color, RoundedCornerShape(999.dp))
                    )
                    Text(item.label, color = textColor, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) {
            val leftPadding = 18.dp.toPx()
            val rightPadding = 18.dp.toPx()
            val topPadding = 18.dp.toPx()
            val bottomPadding = 30.dp.toPx()
            val chartWidth = size.width - leftPadding - rightPadding
            val chartHeight = size.height - topPadding - bottomPadding
            val gridLines = 4

            repeat(gridLines + 1) { index ->
                val y = topPadding + (chartHeight / gridLines) * index
                drawLine(
                    color = gridColor.copy(alpha = 0.7f),
                    start = Offset(leftPadding, y),
                    end = Offset(size.width - rightPadding, y),
                    strokeWidth = 1f
                )
            }

            series.forEach { chart ->
                val path = Path()
                chart.values.forEachIndexed { index, value ->
                    val x = if (chart.values.size == 1) {
                        leftPadding + chartWidth / 2f
                    } else {
                        leftPadding + (chartWidth * index / (chart.values.lastIndex.coerceAtLeast(1)))
                    }
                    val normalized = (value / maxValue).coerceIn(0f, 1f)
                    val y = topPadding + chartHeight - (normalized * chartHeight)

                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }

                drawPath(
                    path = path,
                    color = chart.color,
                    style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                chart.values.forEachIndexed { index, value ->
                    val x = if (chart.values.size == 1) {
                        leftPadding + chartWidth / 2f
                    } else {
                        leftPadding + (chartWidth * index / (chart.values.lastIndex.coerceAtLeast(1)))
                    }
                    val normalized = (value / maxValue).coerceIn(0f, 1f)
                    val y = topPadding + chartHeight - (normalized * chartHeight)
                    drawCircle(color = chart.color, radius = 5f, center = Offset(x, y))
                    drawCircle(color = Color.White, radius = 2f, center = Offset(x, y))
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            points.forEach { point ->
                Text(
                    text = point.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = textColor
                )
            }
        }
    }
}

@Composable
private fun PieChartSection(
    slices: List<PieSliceData>,
    centerTitle: String,
    centerValue: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DonutChart(
            slices = slices,
            centerTitle = centerTitle,
            centerValue = centerValue
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            slices.forEach { slice ->
                PieLegendRow(slice, slices.sumOf { it.value.toDouble() }.toFloat())
            }
        }
    }
}

@Composable
private fun DonutChart(
    slices: List<PieSliceData>,
    centerTitle: String,
    centerValue: String
) {
    val total = slices.sumOf { it.value.toDouble() }.toFloat().takeIf { it > 0f } ?: 1f
    val emptyColor = MaterialTheme.colorScheme.surfaceVariant

    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(180.dp)) {
            val strokeWidth = 28.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val arcSize = Size(diameter, diameter)
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            var startAngle = -90f

            if (slices.all { it.value <= 0f }) {
                drawArc(
                    color = emptyColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )
            } else {
                slices.forEach { slice ->
                    val sweepAngle = (slice.value / total) * 360f
                    drawArc(
                        color = slice.color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                    )
                    startAngle += sweepAngle
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(centerTitle, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(centerValue, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PieLegendRow(slice: PieSliceData, total: Float) {
    val percent = if (total <= 0f) 0f else (slice.value / total) * 100f
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(slice.color, RoundedCornerShape(999.dp))
            )
            Column {
                Text(slice.label, fontWeight = FontWeight.SemiBold)
                Text(slice.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text("${formatPercent(percent.toDouble())}%", fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ProfitBreakdown(report: DashboardReport) {
    BreakdownRow("Profit invoice lunas", report.settledProfit)
    BreakdownRow("Profit invoice ongoing", report.creditProfit)
    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    BreakdownRow("Laba kotor bulan ini", report.periodSummary.totalGrossProfit)
    BreakdownRow("Laba bersih bulan ini", report.periodSummary.totalNetProfit)
    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    BreakdownRow("Pembayaran masuk", report.periodSummary.totalPaymentsReceived)
    BreakdownRow("Piutang baru bulan ini", report.periodSummary.totalReceivables)
    BreakdownRow("Piutang aktif keseluruhan", report.activeReceivables)
}

@Composable
private fun BreakdownRow(label: String, amount: BigDecimal) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(amount.toCurrencyLabel(), fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TopReceivableCustomers(customers: List<CustomerReceivableSnapshot>) {
    if (customers.isEmpty()) {
        Text("Belum ada customer dengan piutang aktif.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }

    val maxValue = customers.maxOf { it.totalReceivable }.takeIf { it > BigDecimal.ZERO } ?: BigDecimal.ONE
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        customers.forEach { customer ->
            val ratio = (customer.totalReceivable.toFloat() / maxValue.toFloat()).coerceIn(0f, 1f)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(customer.customerName, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${customer.invoiceCount} invoice aktif",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(customer.totalReceivable.toCurrencyLabel(), fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(999.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(ratio)
                            .height(10.dp)
                            .background(RevenueColor, RoundedCornerShape(999.dp))
                    )
                }
            }
        }
    }
}

private fun buildGrowthInsights(points: List<MonthlyTrendPoint>): List<GrowthInsight> {
    if (points.size < 2) return emptyList()

    val current = points.last()
    val previous = points[points.lastIndex - 1]

    return listOf(
        GrowthInsight("Omzet", current.revenue, previous.revenue, RevenueColor),
        GrowthInsight("Pembayaran", current.collections, previous.collections, CollectionColor),
        GrowthInsight("Profit", current.netProfit, previous.netProfit, ProfitColor)
    )
}

private data class ChartSeries(
    val label: String,
    val color: Color,
    val values: List<Float>
)

private data class PieSliceData(
    val label: String,
    val value: Float,
    val color: Color,
    val detail: String
)

private data class GrowthInsight(
    val label: String,
    val currentValue: BigDecimal,
    val previousValue: BigDecimal,
    val accentColor: Color
) {
    private val changeValue: BigDecimal = currentValue.subtract(previousValue)
    private val changePercent: Double? =
        if (previousValue.compareTo(BigDecimal.ZERO) == 0) null
        else (changeValue.toDouble() / previousValue.toDouble()) * 100.0

    val badgeColor: Color
        get() = when {
            changePercent == null && currentValue > BigDecimal.ZERO -> accentColor
            changeValue > BigDecimal.ZERO -> PaidColor
            changeValue < BigDecimal.ZERO -> UnpaidColor
            else -> NeutralTrendColor
        }

    val badgeText: String
        get() = when {
            changePercent == null && currentValue > BigDecimal.ZERO -> "Baru aktif"
            changePercent == null -> "Belum ada pembanding"
            abs(changePercent) < 0.05 -> "Stabil"
            changePercent > 0 -> "Naik ${formatPercent(changePercent)}%"
            else -> "Turun ${formatPercent(abs(changePercent))}%"
        }
}

private fun formatPercent(value: Double): String = String.format("%.1f", value)
