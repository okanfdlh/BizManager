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
import com.bizmanager.presentation.ui.PaginationControl
import com.bizmanager.presentation.ui.paginateList
import com.bizmanager.presentation.ui.toCurrencyLabel
import java.math.BigDecimal
import kotlin.math.ceil

@Composable
fun InvoiceListScreen(
    invoiceRepository: InvoiceRepository,
    customerRepository: CustomerRepository,
    onNavigateToForm: (Int?) -> Unit,
    onNavigateToDetail: (Int) -> Unit
) {
    var invoices by remember { mutableStateOf(emptyList<Invoice>()) }
    var customers by remember { mutableStateOf(emptyMap<Int, String>()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var invoiceToDelete by remember { mutableStateOf<Invoice?>(null) }
    var refreshTrigger by remember { mutableStateOf(0) }

    // Filter & Pagination States
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("Semua") } // "Semua", "Draft", "Posted", "Cancelled"
    
    var currentPage by remember { mutableStateOf(1) }
    var pageSize by remember { mutableStateOf(20) }

    LaunchedEffect(refreshTrigger) {
        val inv = invoiceRepository.findAll()
        val cust = customerRepository.findAll(includeInactive = true).associateBy({ it.id }, { it.name })
        invoices = inv
        customers = cust
    }

    // Advanced in-memory filtering
    val filteredInvoices by remember(invoices, customers, searchQuery, statusFilter) {
        derivedStateOf {
            invoices.filter { inv ->
                val customerName = customers[inv.customerId] ?: ""
                val matchesSearch = inv.invoiceNumber.contains(searchQuery, ignoreCase = true) ||
                        customerName.contains(searchQuery, ignoreCase = true)
                
                val matchesStatus = when (statusFilter) {
                    "Draft" -> inv.invoiceStatus == InvoiceStatus.Draft
                    "Posted" -> inv.invoiceStatus == InvoiceStatus.Posted
                    "Cancelled" -> inv.invoiceStatus == InvoiceStatus.Cancelled
                    else -> true
                }
                
                matchesSearch && matchesStatus
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
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Daftar Faktur", style = MaterialTheme.typography.h4)
            Button(onClick = { onNavigateToForm(null) }) {
                Icon(Icons.Default.Add, contentDescription = "Buat Faktur")
                Spacer(Modifier.width(8.dp))
                Text("Buat Faktur")
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
                    Text("Status: $statusFilter")
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    listOf("Semua", "Draft", "Posted", "Cancelled").forEach { opt ->
                        DropdownMenuItem(onClick = {
                            statusFilter = opt
                            currentPage = 1
                            expanded = false
                        }) {
                            Text(opt)
                        }
                    }
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Text("Nomor", modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.subtitle2)
            Text("Tanggal", modifier = Modifier.weight(1f), style = MaterialTheme.typography.subtitle2)
            Text("Customer", modifier = Modifier.weight(2f), style = MaterialTheme.typography.subtitle2)
            Text("Grand Total", modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.subtitle2)
            Text("Status", modifier = Modifier.weight(1f), style = MaterialTheme.typography.subtitle2)
            Text("Total Dibayar", modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.subtitle2)
            Text("Sisa Piutang", modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.subtitle2)
            Text("Aksi", modifier = Modifier.width(150.dp), style = MaterialTheme.typography.subtitle2)
        }
        Divider()

        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
            items(paginatedInvoices) { inv ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigateToDetail(inv.id) }.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(inv.invoiceNumber, modifier = Modifier.weight(1.5f))
                    Text(inv.date.toLocalDate().toString(), modifier = Modifier.weight(1f))
                    Text(customers[inv.customerId] ?: "Unknown", modifier = Modifier.weight(2f))
                    Text(inv.grandTotal.toCurrencyLabel(), modifier = Modifier.weight(1.5f))
                    
                    val statusColor = when(inv.invoiceStatus) {
                        InvoiceStatus.Draft -> MaterialTheme.colors.onSurface.copy(alpha=0.6f)
                        InvoiceStatus.Posted -> MaterialTheme.colors.primary
                        InvoiceStatus.Cancelled -> MaterialTheme.colors.error
                    }
                    Text(inv.invoiceStatus.name, color = statusColor, modifier = Modifier.weight(1f))
                    
                    Text(inv.totalPaid.toCurrencyLabel(), modifier = Modifier.weight(1.5f))
                    Text(inv.balanceDue.toCurrencyLabel(), modifier = Modifier.weight(1.5f), color = if (inv.balanceDue > BigDecimal.ZERO) MaterialTheme.colors.error else MaterialTheme.colors.onSurface)
                    
                    Row(modifier = Modifier.width(150.dp)) {
                        if (inv.invoiceStatus == InvoiceStatus.Draft) {
                            TextButton(onClick = { onNavigateToForm(inv.id) }) { Text("Edit") }
                        } else {
                            TextButton(onClick = { onNavigateToDetail(inv.id) }) { Text("Lihat") }
                        }
                        TextButton(
                            onClick = { 
                                invoiceToDelete = inv
                                showDeleteDialog = true 
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colors.error)
                        ) { Text("Hapus") }
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
        
        if (showDeleteDialog && invoiceToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Hapus Faktur") },
                text = { Text("Apakah Anda yakin ingin menghapus Faktur ${invoiceToDelete?.invoiceNumber}?") },
                confirmButton = {
                    Button(
                        onClick = {
                            invoiceToDelete?.let {
                                try {
                                    invoiceRepository.deleteItemsForInvoice(it.id)
                                    invoiceRepository.delete(it.id)
                                    refreshTrigger++
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            showDeleteDialog = false
                            invoiceToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error)
                    ) {
                        Text("Hapus", color = MaterialTheme.colors.onError)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Batal")
                    }
                }
            )
        }
    }
}
