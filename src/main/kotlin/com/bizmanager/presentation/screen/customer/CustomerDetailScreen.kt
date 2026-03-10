package com.bizmanager.presentation.screen.customer

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
import com.bizmanager.domain.model.Customer
import com.bizmanager.domain.model.Invoice
import com.bizmanager.presentation.ui.toCurrencyLabel
import java.math.BigDecimal

@Composable
fun CustomerDetailScreen(
    customerId: Int,
    customerRepository: CustomerRepository,
    invoiceRepository: InvoiceRepository,
    onBack: () -> Unit
) {
    var customer by remember { mutableStateOf<Customer?>(null) }
    var invoices by remember { mutableStateOf(emptyList<Invoice>()) }

    var totalOmzet by remember { mutableStateOf(BigDecimal.ZERO) }
    var totalPiutang by remember { mutableStateOf(BigDecimal.ZERO) }

    LaunchedEffect(customerId) {
        customer = customerRepository.findById(customerId)
        val customerInvoices = invoiceRepository.findByCustomerId(customerId)
        invoices = customerInvoices
        
        // Analytical Aggregation
        var omzet = BigDecimal.ZERO
        var piutang = BigDecimal.ZERO
        
        customerInvoices.forEach { inv ->
            if (inv.invoiceStatus.name != "Cancelled") {
                omzet = omzet.add(inv.grandTotal)
                piutang = piutang.add(inv.balanceDue)
            }
        }
        totalOmzet = omzet
        totalPiutang = piutang
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Detail Customer: ${customer?.name ?: "Loading..."}", style = MaterialTheme.typography.h4)
            Button(onClick = onBack) { Text("Kembali") }
        }
        Spacer(Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            CustomerSummaryCard("Total Omzet", totalOmzet)
            CustomerSummaryCard("Sisa Piutang", totalPiutang)
        }
        
        Spacer(Modifier.height(24.dp))
        Text("Histori Invoice", style = MaterialTheme.typography.h6)
        Spacer(Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Text("Nomor", modifier = Modifier.weight(1f), style = MaterialTheme.typography.subtitle2)
            Text("Tanggal", modifier = Modifier.weight(1f), style = MaterialTheme.typography.subtitle2)
            Text("Grand Total", modifier = Modifier.weight(1f), style = MaterialTheme.typography.subtitle2)
            Text("Sisa Piutang", modifier = Modifier.weight(1f), style = MaterialTheme.typography.subtitle2)
            Text("Status", modifier = Modifier.weight(1f), style = MaterialTheme.typography.subtitle2)
        }
        Divider()

        LazyColumn {
            items(invoices) { inv ->
                Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(inv.invoiceNumber, modifier = Modifier.weight(1f))
                    Text(inv.date.toLocalDate().toString(), modifier = Modifier.weight(1f))
                    Text(inv.grandTotal.toCurrencyLabel(), modifier = Modifier.weight(1f))
                    Text(inv.balanceDue.toCurrencyLabel(), modifier = Modifier.weight(1f), color = if (inv.balanceDue > BigDecimal.ZERO) MaterialTheme.colors.error else MaterialTheme.colors.onSurface)
                    Text(inv.paymentStatus.name, modifier = Modifier.weight(1f))
                }
                Divider()
            }
        }
    }
}

@Composable
private fun CustomerSummaryCard(title: String, amount: BigDecimal) {
    Card(modifier = Modifier.width(220.dp).padding(4.dp), elevation = 4.dp) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.subtitle2)
            Spacer(modifier = Modifier.height(8.dp))
            Text(amount.toCurrencyLabel(), style = MaterialTheme.typography.h6)
        }
    }
}
