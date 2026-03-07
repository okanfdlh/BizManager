package com.bizmanager.presentation.screen.invoice

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bizmanager.data.repository.CustomerRepository
import com.bizmanager.data.repository.InvoiceRepository
import com.bizmanager.domain.model.Customer
import com.bizmanager.domain.model.Invoice
import com.bizmanager.domain.model.InvoiceStatus
import java.math.BigDecimal

@Composable
fun InvoiceListScreen(
    invoiceRepository: InvoiceRepository,
    customerRepository: CustomerRepository,
    onNavigateToForm: (Int?) -> Unit,
    onNavigateToDetail: (Int) -> Unit
) {
    var invoices by remember { mutableStateOf(emptyList<Invoice>()) }
    var customers by remember { mutableStateOf(emptyMap<Int, String>()) }

    LaunchedEffect(Unit) {
        val inv = invoiceRepository.findAll()
        val cust = customerRepository.findAll(includeInactive = true).associateBy({ it.id }, { it.name })
        invoices = inv
        customers = cust
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Daftar Invoice", style = MaterialTheme.typography.h4)
            Button(onClick = { onNavigateToForm(null) }) {
                Icon(Icons.Default.Add, contentDescription = "Buat Invoice")
                Spacer(Modifier.width(8.dp))
                Text("Buat Invoice")
            }
        }
        Spacer(Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Text("Nomor", modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.subtitle2)
            Text("Tanggal", modifier = Modifier.weight(1f), style = MaterialTheme.typography.subtitle2)
            Text("Customer", modifier = Modifier.weight(2f), style = MaterialTheme.typography.subtitle2)
            Text("Grand Total", modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.subtitle2)
            Text("Status", modifier = Modifier.weight(1f), style = MaterialTheme.typography.subtitle2)
            Text("Sisa Piutang", modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.subtitle2)
            Text("Aksi", modifier = Modifier.width(100.dp), style = MaterialTheme.typography.subtitle2)
        }
        Divider()

        LazyColumn {
            items(invoices) { inv ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigateToDetail(inv.id) }.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(inv.invoiceNumber, modifier = Modifier.weight(1.5f))
                    Text(inv.date.toLocalDate().toString(), modifier = Modifier.weight(1f))
                    Text(customers[inv.customerId] ?: "Unknown", modifier = Modifier.weight(2f))
                    Text("Rp ${inv.grandTotal.toPlainString()}", modifier = Modifier.weight(1.5f))
                    
                    val statusColor = when(inv.invoiceStatus) {
                        InvoiceStatus.Draft -> MaterialTheme.colors.onSurface.copy(alpha=0.6f)
                        InvoiceStatus.Posted -> MaterialTheme.colors.primary
                        InvoiceStatus.Cancelled -> MaterialTheme.colors.error
                    }
                    Text(inv.invoiceStatus.name, color = statusColor, modifier = Modifier.weight(1f))
                    
                    Text("Rp ${inv.balanceDue.toPlainString()}", modifier = Modifier.weight(1.5f), color = if (inv.balanceDue > BigDecimal.ZERO) MaterialTheme.colors.error else MaterialTheme.colors.onSurface)
                    
                    Row(modifier = Modifier.width(100.dp)) {
                        if (inv.invoiceStatus == InvoiceStatus.Draft) {
                            TextButton(onClick = { onNavigateToForm(inv.id) }) { Text("Edit") }
                        } else {
                            TextButton(onClick = { onNavigateToDetail(inv.id) }) { Text("Lihat") }
                        }
                    }
                }
                Divider()
            }
        }
    }
}
