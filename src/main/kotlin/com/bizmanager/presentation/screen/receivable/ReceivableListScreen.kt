package com.bizmanager.presentation.screen.receivable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bizmanager.data.repository.CustomerRepository
import com.bizmanager.data.repository.InvoiceRepository
import com.bizmanager.domain.model.Invoice
import com.bizmanager.domain.service.AgingCalculator
import java.math.BigDecimal

@Composable
fun ReceivableListScreen(
    invoiceRepository: InvoiceRepository,
    customerRepository: CustomerRepository
) {
    var overdueInvoices by remember { mutableStateOf(emptyList<Invoice>()) }
    var customerMap by remember { mutableStateOf(emptyMap<Int, String>()) }

    var totalReceivables by remember { mutableStateOf(BigDecimal.ZERO) }
    var total0To30 by remember { mutableStateOf(BigDecimal.ZERO) }
    var total31To90 by remember { mutableStateOf(BigDecimal.ZERO) }
    var totalMore90 by remember { mutableStateOf(BigDecimal.ZERO) }

    LaunchedEffect(Unit) {
        val cust = customerRepository.findAll(includeInactive = true).associateBy({it.id}, {it.name})
        customerMap = cust

        val invoicesWithBalance = invoiceRepository.findAllWithPositiveBalance()
        
        var totAll = BigDecimal.ZERO
        var tot0_30 = BigDecimal.ZERO
        var tot31_90 = BigDecimal.ZERO
        var totM90 = BigDecimal.ZERO

        val sortedList = invoicesWithBalance.sortedBy { it.dueDate }
        sortedList.forEach { inv ->
            totAll = totAll.add(inv.balanceDue)
            val bucket = AgingCalculator.calculateAging(inv.dueDate, inv.balanceDue)
            when (bucket?.name) {
                "Overdue0To30" -> tot0_30 = tot0_30.add(inv.balanceDue)
                "Overdue31To90" -> tot31_90 = tot31_90.add(inv.balanceDue)
                "OverdueMoreThan90" -> totM90 = totM90.add(inv.balanceDue)
            }
        }

        overdueInvoices = sortedList
        totalReceivables = totAll
        total0To30 = tot0_30
        total31To90 = tot31_90
        totalMore90 = totM90
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Monitor Piutang (Aging)", style = MaterialTheme.typography.h4)
        Spacer(Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Card(modifier = Modifier.padding(4.dp).weight(1f)) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Total Piutang Berjalan", style = MaterialTheme.typography.caption)
                    Text("Rp ${totalReceivables.toPlainString()}", style = MaterialTheme.typography.h6)
                }
            }
            Card(modifier = Modifier.padding(4.dp).weight(1f), backgroundColor = MaterialTheme.colors.secondary.copy(alpha=0.1f)) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Overdue 1-30 Hari", style = MaterialTheme.typography.caption)
                    Text("Rp ${total0To30.toPlainString()}", style = MaterialTheme.typography.h6)
                }
            }
            Card(modifier = Modifier.padding(4.dp).weight(1f), backgroundColor = MaterialTheme.colors.primary.copy(alpha=0.1f)) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Overdue 31-90 Hari", style = MaterialTheme.typography.caption)
                    Text("Rp ${total31To90.toPlainString()}", style = MaterialTheme.typography.h6)
                }
            }
            Card(modifier = Modifier.padding(4.dp).weight(1f), backgroundColor = MaterialTheme.colors.error.copy(alpha=0.1f)) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Overdue > 90 Hari", style = MaterialTheme.typography.caption)
                    Text("Rp ${totalMore90.toPlainString()}", style = MaterialTheme.typography.h6, color = MaterialTheme.colors.error)
                }
            }
        }
        
        Spacer(Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Text("No. Invoice", modifier = Modifier.weight(1f), style = MaterialTheme.typography.subtitle2)
            Text("Customer", modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.subtitle2)
            Text("Jatuh Tempo", modifier = Modifier.weight(1f), style = MaterialTheme.typography.subtitle2)
            Text("Sisa Piutang", modifier = Modifier.weight(1f), style = MaterialTheme.typography.subtitle2)
            Text("Bucket Aging", modifier = Modifier.weight(1f), style = MaterialTheme.typography.subtitle2)
        }
        Divider()

        LazyColumn {
            items(overdueInvoices) { inv ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(inv.invoiceNumber, modifier = Modifier.weight(1f))
                    Text(customerMap[inv.customerId] ?: "Unknown", modifier = Modifier.weight(1.5f))
                    Text(inv.dueDate.toLocalDate().toString(), modifier = Modifier.weight(1f))
                    Text("Rp ${inv.balanceDue.toPlainString()}", modifier = Modifier.weight(1f))

                    val aging = AgingCalculator.calculateAging(inv.dueDate, inv.balanceDue)
                    val agingStr = when(aging?.name) {
                        "Current" -> "Belum Jatuh Tempo"
                        "Overdue0To30" -> "1 - 30 Hari"
                        "Overdue31To90" -> "31 - 90 Hari"
                        "OverdueMoreThan90" -> "> 90 Hari"
                        else -> "-"
                    }
                    val color = if (aging?.name?.startsWith("Overdue") == true) MaterialTheme.colors.error else MaterialTheme.colors.onSurface
                    
                    Text(agingStr, modifier = Modifier.weight(1f), color = color)
                }
                Divider()
            }
        }
    }
}
