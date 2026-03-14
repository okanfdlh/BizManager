package com.bizmanager.presentation.screen.receivable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bizmanager.data.repository.CustomerRepository
import com.bizmanager.data.repository.InvoiceRepository
import com.bizmanager.domain.model.Customer
import com.bizmanager.domain.model.Invoice
import com.bizmanager.domain.model.InvoiceStatus
import com.bizmanager.presentation.ui.PaginationControl
import com.bizmanager.presentation.ui.paginateList
import com.bizmanager.presentation.ui.toCurrencyLabel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.math.ceil

data class CustomerReceivableSummary(
    val customer: Customer,
    val totalHutang: BigDecimal,
    val totalDibayar: BigDecimal,
    val sisaHutang: BigDecimal
) {
    val isPaid: Boolean get() = sisaHutang.compareTo(BigDecimal.ZERO) <= 0 && totalHutang > BigDecimal.ZERO
}

@Composable
fun CustomerReceivableScreen(
    invoiceRepository: InvoiceRepository,
    customerRepository: CustomerRepository,
    onNavigateToCustomerLedger: (Int) -> Unit
) {
    var invoices by remember { mutableStateOf(emptyList<Invoice>()) }
    var customers by remember { mutableStateOf(emptyList<Customer>()) }
    var loading by remember { mutableStateOf(true) }

    var searchQuery by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf("Outstanding") } // Options: "All", "Outstanding", "Paid"
    var startDateStr by remember { mutableStateOf("") }
    var endDateStr by remember { mutableStateOf("") }
    
    var currentPage by remember { mutableStateOf(1) }
    var pageSize by remember { mutableStateOf(20) }

    LaunchedEffect(Unit) {
        val inv = withContext(Dispatchers.IO) { invoiceRepository.findAll() }
        val cust = withContext(Dispatchers.IO) { customerRepository.findAll(includeInactive = true) }
        invoices = inv
        customers = cust
        loading = false
    }

    val startDate = try { if (startDateStr.isNotBlank()) LocalDate.parse(startDateStr) else null } catch (e: Exception) { null }
    val endDate = try { if (endDateStr.isNotBlank()) LocalDate.parse(endDateStr) else null } catch (e: Exception) { null }

    val filteredInvoices = invoices.filter { inv ->
        inv.invoiceStatus != InvoiceStatus.Cancelled &&
        (startDate == null || !inv.date.toLocalDate().isBefore(startDate)) &&
        (endDate == null || !inv.date.toLocalDate().isAfter(endDate))
    }

    val invoiceGroups = filteredInvoices.groupBy { it.customerId }

    val rawSummaries = customers.map { cust ->
        val custInvs = invoiceGroups[cust.id] ?: emptyList()
        var totHutang = BigDecimal.ZERO
        var totBayar = BigDecimal.ZERO
        var sisa = BigDecimal.ZERO
        custInvs.forEach { inv ->
            totHutang = totHutang.add(inv.grandTotal)
            totBayar = totBayar.add(inv.totalPaid)
            sisa = sisa.add(inv.balanceDue)
        }
        CustomerReceivableSummary(cust, totHutang, totBayar, sisa)
    }

    val displayList by remember(rawSummaries, searchQuery, filterStatus) {
        derivedStateOf {
            rawSummaries.filter { summary ->
                val matchesSearch = summary.customer.name.contains(searchQuery, ignoreCase = true) ||
                                    summary.customer.company?.contains(searchQuery, ignoreCase = true) == true
                val matchesStatus = when (filterStatus) {
                    "Outstanding" -> summary.sisaHutang > BigDecimal.ZERO
                    "Paid" -> summary.isPaid
                    else -> true
                }
                val hasData = summary.totalHutang > BigDecimal.ZERO || summary.totalDibayar > BigDecimal.ZERO || filterStatus == "All"
                
                matchesSearch && matchesStatus && hasData
            }.sortedByDescending { it.sisaHutang }
        }
    }

    // Pagination Logic
    val totalElements = displayList.size
    val totalPages = if (totalElements == 0) 1 else ceil(totalElements.toDouble() / pageSize).toInt()
    
    LaunchedEffect(totalPages) {
        if (currentPage > totalPages) currentPage = totalPages
        if (currentPage < 1) currentPage = 1
    }

    val paginatedList by remember(displayList, currentPage, pageSize) {
        derivedStateOf {
            paginateList(displayList, currentPage, pageSize)
        }
    }

    var grandTotalHutang = BigDecimal.ZERO
    var grandTotalDibayar = BigDecimal.ZERO
    var grandTotalSisa = BigDecimal.ZERO
    displayList.forEach {
        grandTotalHutang = grandTotalHutang.add(it.totalHutang)
        grandTotalDibayar = grandTotalDibayar.add(it.totalDibayar)
        grandTotalSisa = grandTotalSisa.add(it.sisaHutang)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Rekap Piutang Customer", style = MaterialTheme.typography.h4)
        Spacer(Modifier.height(16.dp))

        // Filters
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it; currentPage = 1 },
                label = { Text("Cari Customer") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = startDateStr,
                onValueChange = { startDateStr = it },
                label = { Text("Dari Tgl (YYYY-MM-DD)") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = endDateStr,
                onValueChange = { endDateStr = it },
                label = { Text("Sampai Tgl (YYYY-MM-DD)") },
                modifier = Modifier.weight(1f)
            )
            
            // Dropdown Status Filter
            var expandedStatus by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = filterStatus,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Status") },
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownMenu(expanded = expandedStatus, onDismissRequest = { expandedStatus = false }) {
                    listOf("All", "Outstanding", "Paid").forEach { st ->
                        DropdownMenuItem(onClick = { filterStatus = st; currentPage = 1; expandedStatus = false }) { Text(st) }
                    }
                }
                Spacer(modifier = Modifier.matchParentSize().clickable { expandedStatus = true })
            }
        }

        Spacer(Modifier.height(16.dp))

        // Totals Card
        Card(modifier = Modifier.fillMaxWidth(), backgroundColor = MaterialTheme.colors.surface) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceAround) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Hutang", style = MaterialTheme.typography.caption)
                    Text(grandTotalHutang.toCurrencyLabel(), style = MaterialTheme.typography.h6)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Sudah Dibayar", style = MaterialTheme.typography.caption)
                    Text(grandTotalDibayar.toCurrencyLabel(), style = MaterialTheme.typography.h6, color = MaterialTheme.colors.primary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Sisa Hutang", style = MaterialTheme.typography.caption)
                    Text(grandTotalSisa.toCurrencyLabel(), style = MaterialTheme.typography.h6, color = MaterialTheme.colors.error)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Table Header
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Text("Nama Customer", modifier = Modifier.weight(2f), style = MaterialTheme.typography.subtitle2)
            Text("Total Hutang", modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.subtitle2)
            Text("Sudah Dibayar", modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.subtitle2)
            Text("Sisa Hutang", modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.subtitle2)
            Text("Status", modifier = Modifier.weight(1f), style = MaterialTheme.typography.subtitle2)
            Text("Aksi", modifier = Modifier.width(100.dp), style = MaterialTheme.typography.subtitle2)
        }
        Divider()

        if (loading) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                items(paginatedList) { summary ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(2f)) {
                            Text(summary.customer.name, fontWeight = FontWeight.SemiBold)
                            if (!summary.customer.company.isNullOrBlank()) {
                                Text(summary.customer.company, style = MaterialTheme.typography.caption)
                            }
                        }
                        Text(summary.totalHutang.toCurrencyLabel(), modifier = Modifier.weight(1.5f))
                        Text(summary.totalDibayar.toCurrencyLabel(), modifier = Modifier.weight(1.5f))
                        Text(summary.sisaHutang.toCurrencyLabel(), modifier = Modifier.weight(1.5f), color = if (summary.sisaHutang > BigDecimal.ZERO) MaterialTheme.colors.error else MaterialTheme.colors.onSurface)
                        
                        val statusText = if (summary.sisaHutang > BigDecimal.ZERO) "Outstanding" else if (summary.isPaid) "Paid" else "-"
                        val statusColor = if (summary.sisaHutang > BigDecimal.ZERO) MaterialTheme.colors.error else if (summary.isPaid) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface
                        Text(statusText, color = statusColor, modifier = Modifier.weight(1f))

                        TextButton(
                            onClick = { onNavigateToCustomerLedger(summary.customer.id) },
                            modifier = Modifier.width(100.dp)
                        ) {
                            Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Lihat")
                        }
                    }
                    Divider()
                }
            }
            
            Divider()
            PaginationControl(
                currentPage = currentPage,
                totalPages = totalPages,
                pageSize = pageSize,
                totalElements = totalElements,
                onPageChanged = { currentPage = it },
                onPageSizeChanged = { pageSize = it; currentPage = 1 }
            )
        }
    }
}
