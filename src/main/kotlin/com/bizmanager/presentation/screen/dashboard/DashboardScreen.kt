package com.bizmanager.presentation.screen.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bizmanager.domain.service.ReportService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.time.LocalDateTime

@Composable
fun DashboardScreen(
    reportService: ReportService,
    onNavigateToInvoices: () -> Unit,
    onNavigateToReceivables: () -> Unit
) {
    var totalOmzet by remember { mutableStateOf(BigDecimal.ZERO) }
    var totalNetProfit by remember { mutableStateOf(BigDecimal.ZERO) }
    var totalReceivables by remember { mutableStateOf(BigDecimal.ZERO) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            // For example, fetch this month's data
            val startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0)
            val endOfMonth = startOfMonth.plusMonths(1).minusNanos(1)
            val summary = reportService.getSalesSummary(startOfMonth, endOfMonth)
            
            totalOmzet = summary.totalOmzet
            totalNetProfit = summary.totalNetProfit
            totalReceivables = summary.totalReceivables
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("DASHBOARD", style = MaterialTheme.typography.h4)
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            SummaryCard("Total Omzet Bulan Ini", totalOmzet)
            SummaryCard("Laba Bersih Bulan Ini", totalNetProfit)
            SummaryCard("Total Piutang Aktif", totalReceivables)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row {
            Button(onClick = onNavigateToInvoices) {
                Text("Buka Semua Invoice")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = onNavigateToReceivables) {
                Text("Monitor Piutang (Aging)")
            }
        }
    }
}

@Composable
fun SummaryCard(title: String, amount: BigDecimal) {
    Card(modifier = Modifier.width(200.dp).padding(8.dp), elevation = 4.dp) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.subtitle2)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Rp ${amount.toPlainString()}", style = MaterialTheme.typography.h6)
        }
    }
}
