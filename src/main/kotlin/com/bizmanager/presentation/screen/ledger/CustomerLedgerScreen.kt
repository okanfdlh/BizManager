package com.bizmanager.presentation.screen.ledger

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bizmanager.data.repository.CustomerRepository
import com.bizmanager.domain.model.Customer
import com.bizmanager.domain.service.CustomerLedgerInvoice
import com.bizmanager.domain.service.CustomerLedgerReport
import com.bizmanager.domain.service.CustomerLedgerService
import com.bizmanager.presentation.ui.toCurrencyLabel
import com.bizmanager.presentation.ui.toDateLabel
import com.bizmanager.presentation.ui.toDateTimeLabel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CustomerLedgerScreen(
    customerRepository: CustomerRepository,
    customerLedgerService: CustomerLedgerService
) {
    var customers by remember { mutableStateOf<List<Customer>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var ledgerReport by remember { mutableStateOf<CustomerLedgerReport?>(null) }
    var loading by remember { mutableStateOf(false) }
    val expandedInvoices = remember { mutableStateMapOf<Int, Boolean>() }

    LaunchedEffect(Unit) {
        customers = withContext(Dispatchers.IO) {
            customerRepository.findAll(includeInactive = true)
        }
    }

    LaunchedEffect(selectedCustomer?.id) {
        val customer = selectedCustomer ?: run {
            ledgerReport = null
            return@LaunchedEffect
        }

        loading = true
        ledgerReport = withContext(Dispatchers.IO) {
            customerLedgerService.getCustomerLedger(customer.id)
        }
        loading = false
    }

    val filteredCustomers = customers.filter { customer ->
        query.isBlank() ||
            customer.name.contains(query, ignoreCase = true) ||
            customer.code.contains(query, ignoreCase = true) ||
            (customer.company?.contains(query, ignoreCase = true) == true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Buku Besar Customer", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Cari customer, pilih dari dropdown, lalu review semua invoice, item produk, histori pembayaran, dan posisi hutang per faktur.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    expanded = true
                },
                label = { Text("Cari customer / company / kode") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                filteredCustomers.take(12).forEach { customer ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(customer.name, fontWeight = FontWeight.SemiBold)
                                Text(
                                    listOfNotNull(customer.code, customer.company).joinToString(" • "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        onClick = {
                            selectedCustomer = customer
                            query = customer.name
                            expanded = false
                        }
                    )
                }
            }
        }

        when {
            loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            ledgerReport == null -> {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Belum ada customer yang dipilih.", style = MaterialTheme.typography.titleLarge)
                        Text("Gunakan search di atas untuk memilih customer dan memunculkan ledger lengkap.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            else -> {
                val report = ledgerReport!!
                LedgerSummary(report)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(report.invoices) { entry ->
                        val isExpanded = expandedInvoices[entry.invoice.id] ?: false
                        InvoiceLedgerCard(
                            entry = entry,
                            expanded = isExpanded,
                            onToggle = { expandedInvoices[entry.invoice.id] = !isExpanded }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LedgerSummary(report: CustomerLedgerReport) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(report.customer.name, style = MaterialTheme.typography.headlineSmall)
                Text(
                    listOfNotNull(report.customer.company, report.customer.phone, report.customer.email).joinToString(" • "),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryMiniCard("Total Invoice", report.summary.totalInvoices.toString())
                SummaryMiniCard("Invoice Ongoing", report.summary.openInvoices.toString())
                SummaryMiniCard("Invoice Lunas", report.summary.settledInvoices.toString())
                SummaryMiniCard("Total Tagihan", report.summary.totalInvoiced.toCurrencyLabel())
                SummaryMiniCard("Total Terbayar", report.summary.totalPaid.toCurrencyLabel())
                SummaryMiniCard("Sisa Hutang", report.summary.totalOutstanding.toCurrencyLabel())
                SummaryMiniCard("Laba Kotor", report.summary.totalGrossProfit.toCurrencyLabel())
                SummaryMiniCard("Laba Bersih", report.summary.totalNetProfit.toCurrencyLabel())
                SummaryMiniCard("Baris Produk", report.summary.totalProductLines.toString())
            }
        }
    }
}

@Composable
private fun SummaryMiniCard(title: String, value: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .width(180.dp)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InvoiceLedgerCard(
    entry: CustomerLedgerInvoice,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(entry.invoice.invoiceNumber, style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Tanggal ${entry.invoice.date.toDateLabel()} • Jatuh tempo ${entry.invoice.dueDate.toDateLabel()}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(onClick = onToggle) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (expanded) "Sembunyikan" else "Lihat detail")
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    selected = entry.isSettled,
                    onClick = {},
                    label = { Text(if (entry.isSettled) "Lunas" else "Ongoing") }
                )
                AssistChip(onClick = {}, label = { Text("Status invoice: ${entry.invoice.invoiceStatus.name}") })
                AssistChip(onClick = {}, label = { Text("Status bayar: ${entry.invoice.paymentStatus.name}") })
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryMiniCard("Nilai Faktur", entry.invoice.grandTotal.toCurrencyLabel())
                SummaryMiniCard("Total Terbayar", entry.totalPaid.toCurrencyLabel())
                SummaryMiniCard("Sisa Hutang", entry.outstanding.toCurrencyLabel())
                SummaryMiniCard("Laba Bersih", entry.invoice.netProfit.toCurrencyLabel())
            }

            if (expanded) {
                HorizontalDivider()

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Produk pada faktur", style = MaterialTheme.typography.titleMedium)
                    if (entry.items.isEmpty()) {
                        Text("Tidak ada item produk tercatat.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        entry.items.forEach { item ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(item.productNameSnapshot, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            "${item.qty} ${item.unitSnapshot ?: ""} • ${item.productCodeSnapshot}",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(item.subtotal.toCurrencyLabel(), fontWeight = FontWeight.SemiBold)
                                        Text("Profit ${item.grossProfit.toCurrencyLabel()}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Riwayat transaksi", style = MaterialTheme.typography.titleMedium)
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                            Text("Invoice dibuat", fontWeight = FontWeight.SemiBold)
                            Text(entry.invoice.date.toDateTimeLabel(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (!entry.invoice.notes.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(entry.invoice.notes, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    if (entry.payments.isEmpty()) {
                        Text("Belum ada pembayaran masuk untuk faktur ini.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        entry.payments.forEach { payment ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(payment.paymentNumber, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            "${payment.date.toDateTimeLabel()} • ${payment.paymentMethod}",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (!payment.reference.isNullOrBlank() || !payment.notes.isNullOrBlank()) {
                                            Text(
                                                listOfNotNull(payment.reference, payment.notes).joinToString(" • "),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Text(payment.amount.toCurrencyLabel(), style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
