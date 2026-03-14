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
import com.bizmanager.presentation.ui.PaginationControl
import com.bizmanager.presentation.ui.paginateList
import com.bizmanager.presentation.ui.toCurrencyLabel
import java.math.BigDecimal
import kotlin.math.ceil

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

    // Filter & Pagination States
    var searchQuery by remember { mutableStateOf("") }
    var agingFilter by remember { mutableStateOf("Semua") }
    
    var currentPage by remember { mutableStateOf(1) }
    var pageSize by remember { mutableStateOf(20) }

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

    // Advanced in-memory filtering
    val filteredInvoices by remember(overdueInvoices, customerMap, searchQuery, agingFilter) {
        derivedStateOf {
            overdueInvoices.filter { inv ->
                val customerName = customerMap[inv.customerId] ?: ""
                val matchesSearch = inv.invoiceNumber.contains(searchQuery, ignoreCase = true) ||
                        customerName.contains(searchQuery, ignoreCase = true)
                
                val aging = AgingCalculator.calculateAging(inv.dueDate, inv.balanceDue)?.name
                val matchesAging = when (agingFilter) {
                    "Semua" -> true
                    "Current" -> aging == "Current"
                    "1-30 Hari" -> aging == "Overdue0To30"
                    "31-90 Hari" -> aging == "Overdue31To90"
                    "> 90 Hari" -> aging == "OverdueMoreThan90"
                    else -> true
                }
                
                matchesSearch && matchesAging
            }
        }
    }

    // Pagination Logic
    val totalElements = filteredInvoices.size
    val totalPages = if (totalElements == 0) 1 else ceil(totalElements.toDouble() / pageSize).toInt()
    
    // Auto-adjust page if out of bounds after filtering
    LaunchedEffect(totalPages) {
        if (currentPage > totalPages) currentPage = totalPages
        if (currentPage < 1) currentPage = 1
    }

    val paginatedInvoices by remember(filteredInvoices, currentPage, pageSize) {
        derivedStateOf {
            paginateList(filteredInvoices, currentPage, pageSize)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Monitor Piutang (Aging)", style = MaterialTheme.typography.h4)
        Spacer(Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Card(modifier = Modifier.padding(4.dp).weight(1f)) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Total Piutang Berjalan", style = MaterialTheme.typography.caption)
                    Text(totalReceivables.toCurrencyLabel(), style = MaterialTheme.typography.h6)
                }
            }
            Card(modifier = Modifier.padding(4.dp).weight(1f), backgroundColor = MaterialTheme.colors.secondary.copy(alpha=0.1f)) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Overdue 1-30 Hari", style = MaterialTheme.typography.caption)
                    Text(total0To30.toCurrencyLabel(), style = MaterialTheme.typography.h6)
                }
            }
            Card(modifier = Modifier.padding(4.dp).weight(1f), backgroundColor = MaterialTheme.colors.primary.copy(alpha=0.1f)) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Overdue 31-90 Hari", style = MaterialTheme.typography.caption)
                    Text(total31To90.toCurrencyLabel(), style = MaterialTheme.typography.h6)
                }
            }
            Card(modifier = Modifier.padding(4.dp).weight(1f), backgroundColor = MaterialTheme.colors.error.copy(alpha=0.1f)) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Overdue > 90 Hari", style = MaterialTheme.typography.caption)
                    Text(totalMore90.toCurrencyLabel(), style = MaterialTheme.typography.h6, color = MaterialTheme.colors.error)
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))

        // Filter Controls
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it; currentPage = 1 },
                label = { Text("Cari (No Faktur, Nama Customer)") },
                modifier = Modifier.weight(1f)
            )
            
            var expanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.width(200.dp)) {
                OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                    Text("Aging: $agingFilter")
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    listOf("Semua", "Current", "1-30 Hari", "31-90 Hari", "> 90 Hari").forEach { opt ->
                        DropdownMenuItem(onClick = {
                            agingFilter = opt
                            currentPage = 1
                            expanded = false
                        }) {
                            Text(opt)
                        }
                    }
                }
            }
        }
        
        Spacer(Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Text("No. Faktur", modifier = Modifier.weight(1f), style = MaterialTheme.typography.subtitle2)
            Text("Customer", modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.subtitle2)
            Text("Jatuh Tempo", modifier = Modifier.weight(1f), style = MaterialTheme.typography.subtitle2)
            Text("Sisa Piutang", modifier = Modifier.weight(1f), style = MaterialTheme.typography.subtitle2)
            Text("Bucket Aging", modifier = Modifier.weight(1f), style = MaterialTheme.typography.subtitle2)
        }
        Divider()

        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
            items(paginatedInvoices) { inv ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(inv.invoiceNumber, modifier = Modifier.weight(1f))
                    Text(customerMap[inv.customerId] ?: "Unknown", modifier = Modifier.weight(1.5f))
                    Text(inv.dueDate.toLocalDate().toString(), modifier = Modifier.weight(1f))
                    Text(inv.balanceDue.toCurrencyLabel(), modifier = Modifier.weight(1f))

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
