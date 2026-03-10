package com.bizmanager.presentation.screen.dashboard

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
import androidx.compose.ui.graphics.Color
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text("Dashboard", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Ringkasan performa bulanan, kualitas pembayaran, hutang aktif, dan profit yang bisa langsung dibaca tim operasional.",
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
            DashboardMetricCard("Omzet Bulan Ini", report.periodSummary.totalOmzet.toCurrencyLabel(), "Nilai invoice pada periode berjalan")
            DashboardMetricCard("Laba Bersih", report.periodSummary.totalNetProfit.toCurrencyLabel(), "Keuntungan neto bulan berjalan")
            DashboardMetricCard("Pembayaran Masuk", report.periodSummary.totalPaymentsReceived.toCurrencyLabel(), "Cash-in berdasarkan transaksi pembayaran")
            DashboardMetricCard("Piutang Aktif", report.activeReceivables.toCurrencyLabel(), "Outstanding dari invoice yang belum lunas")
            DashboardMetricCard("Penjualan Non-Hutang", report.settledSales.toCurrencyLabel(), "Invoice yang sudah lunas penuh")
            DashboardMetricCard("Penjualan Hutang", report.creditSales.toCurrencyLabel(), "Invoice yang masih memiliki balance")
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            InsightCard(
                title = "Tren Omzet, Pembayaran, dan Profit",
                modifier = Modifier.weight(1.45f)
            ) {
                TrendChart(report.monthlyTrend)
            }
            InsightCard(
                title = "Kualitas Invoice",
                modifier = Modifier.weight(1f)
            ) {
                InvoiceHealth(report)
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            InsightCard(
                title = "Keuntungan Lengkap",
                modifier = Modifier.weight(1f)
            ) {
                ProfitBreakdown(report)
            }
            InsightCard(
                title = "Customer Dengan Piutang Terbesar",
                modifier = Modifier.weight(1f)
            ) {
                TopReceivableCustomers(report.topReceivableCustomers)
            }
        }
    }
}

@Composable
private fun DashboardMetricCard(title: String, value: String, description: String) {
    Card(
        modifier = Modifier.width(220.dp),
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
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = {
                Text(title, style = MaterialTheme.typography.titleLarge)
                content()
            }
        )
    }
}

@Composable
private fun TrendChart(points: List<MonthlyTrendPoint>) {
    val maxValue = points.flatMap { listOf(it.revenue, it.collections, it.netProfit) }
        .maxByOrNull { it }
        ?.takeIf { it > BigDecimal.ZERO }
        ?: BigDecimal.ONE

    Row(
        modifier = Modifier.fillMaxWidth().height(260.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        points.forEach { point ->
            Column(
                modifier = Modifier.weight(1f).fillMaxSize(),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ChartBar(point.revenue, maxValue, Color(0xFF0F766E), "Omzet")
                Spacer(modifier = Modifier.height(8.dp))
                ChartBar(point.collections, maxValue, Color(0xFFB45309), "Bayar")
                Spacer(modifier = Modifier.height(8.dp))
                ChartBar(point.netProfit, maxValue, Color(0xFF2563EB), "Profit")
                Spacer(modifier = Modifier.height(12.dp))
                Text(point.label, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun ChartBar(amount: BigDecimal, maxValue: BigDecimal, color: Color, caption: String) {
    val ratio = (amount.toFloat() / maxValue.toFloat()).coerceIn(0f, 1f)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.width(22.dp).height((36 + (ratio * 110f)).dp),
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 6.dp, bottomEnd = 6.dp),
            color = color
        ) {}
        Spacer(modifier = Modifier.height(4.dp))
        Text(caption, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun InvoiceHealth(report: DashboardReport) {
    val total = (report.paidInvoiceCount + report.outstandingInvoiceCount + report.unpaidInvoiceCount).coerceAtLeast(1)

    HealthRow("Lunas", report.paidInvoiceCount, total, Color(0xFF0F766E))
    HealthRow("Outstanding", report.outstandingInvoiceCount, total, Color(0xFFB45309))
    HealthRow("Belum Dibayar", report.unpaidInvoiceCount, total, Color(0xFFB91C1C))
    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
    Text("Piutang aktif saat ini: ${report.activeReceivables.toCurrencyLabel()}", style = MaterialTheme.typography.bodyLarge)
}

@Composable
private fun HealthRow(label: String, value: Int, total: Int, color: Color) {
    val ratio = value.toFloat() / total.toFloat()
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value.toString(), fontWeight = FontWeight.SemiBold)
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
                    .background(color, RoundedCornerShape(999.dp))
            )
        }
    }
}

@Composable
private fun ProfitBreakdown(report: DashboardReport) {
    BreakdownRow("Profit dari invoice lunas", report.settledProfit)
    BreakdownRow("Profit dari invoice ongoing", report.creditProfit)
    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    BreakdownRow("Laba kotor bulan ini", report.periodSummary.totalGrossProfit)
    BreakdownRow("Laba bersih bulan ini", report.periodSummary.totalNetProfit)
    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    BreakdownRow("Piutang baru periode ini", report.periodSummary.totalReceivables)
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
                        Text("${customer.invoiceCount} invoice aktif", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                            .background(Color(0xFF0F766E), RoundedCornerShape(999.dp))
                    )
                }
            }
        }
    }
}
