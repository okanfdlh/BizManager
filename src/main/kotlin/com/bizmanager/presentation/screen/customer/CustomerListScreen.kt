package com.bizmanager.presentation.screen.customer

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
import com.bizmanager.domain.model.Customer
import com.bizmanager.presentation.ui.PaginationControl
import com.bizmanager.presentation.ui.paginateList
import kotlin.math.ceil

@Composable
fun CustomerListScreen(
    customerRepository: CustomerRepository,
    onNavigateToForm: (Int?) -> Unit,
    onNavigateToDetail: (Int) -> Unit
) {
    var customers by remember { mutableStateOf(emptyList<Customer>()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var customerToDelete by remember { mutableStateOf<Customer?>(null) }
    var refreshTrigger by remember { mutableStateOf(0) }

    // Filter & Pagination States
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("Semua") } // "Semua", "Aktif", "Nonaktif"
    
    var currentPage by remember { mutableStateOf(1) }
    var pageSize by remember { mutableStateOf(20) }

    LaunchedEffect(refreshTrigger) {
        customers = customerRepository.findAll(includeInactive = true)
    }

    // Advanced in-memory filtering
    val filteredCustomers by remember(customers, searchQuery, statusFilter) {
        derivedStateOf {
            customers.filter { c ->
                val matchesSearch = c.name.contains(searchQuery, ignoreCase = true) ||
                        c.code.contains(searchQuery, ignoreCase = true) ||
                        (c.phone?.contains(searchQuery, ignoreCase = true) == true) ||
                        (c.company?.contains(searchQuery, ignoreCase = true) == true)
                
                val matchesStatus = when (statusFilter) {
                    "Aktif" -> c.isActive
                    "Nonaktif" -> !c.isActive
                    else -> true
                }
                
                matchesSearch && matchesStatus
            }
        }
    }

    // Pagination Logic
    val totalElements = filteredCustomers.size
    val totalPages = if (totalElements == 0) 1 else ceil(totalElements.toDouble() / pageSize).toInt()
    
    // Auto-adjust page if out of bounds after filtering
    LaunchedEffect(totalPages) {
        if (currentPage > totalPages) currentPage = totalPages
        if (currentPage < 1) currentPage = 1
    }

    val paginatedCustomers by remember(filteredCustomers, currentPage, pageSize) {
        derivedStateOf {
            paginateList(filteredCustomers, currentPage, pageSize)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Daftar Customer", style = MaterialTheme.typography.h4)
            Button(onClick = { onNavigateToForm(null) }) {
                Icon(Icons.Default.Add, contentDescription = "Tambah")
                Spacer(Modifier.width(8.dp))
                Text("Tambah Customer")
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Filter Controls
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it; currentPage = 1 },
                label = { Text("Cari (Nama, Kode, Telp)") },
                modifier = Modifier.weight(1f)
            )
            
            var expanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.width(200.dp)) {
                OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                    Text("Status: $statusFilter")
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    listOf("Semua", "Aktif", "Nonaktif").forEach { opt ->
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

        // Simple table header
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Text("Kode", modifier = Modifier.weight(1f), style = MaterialTheme.typography.subtitle2)
            Text("Nama", modifier = Modifier.weight(2f), style = MaterialTheme.typography.subtitle2)
            Text("Telepon", modifier = Modifier.weight(1f), style = MaterialTheme.typography.subtitle2)
            Text("Status", modifier = Modifier.width(100.dp), style = MaterialTheme.typography.subtitle2)
            Text("Aksi", modifier = Modifier.width(150.dp), style = MaterialTheme.typography.subtitle2)
        }
        Divider()

        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
            items(paginatedCustomers) { c ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigateToDetail(c.id) }.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(c.code, modifier = Modifier.weight(1f))
                    Text(c.name, modifier = Modifier.weight(2f))
                    Text(c.phone ?: "-", modifier = Modifier.weight(1f))
                    Text(if (c.isActive) "Aktif" else "Nonaktif", color = if (c.isActive) MaterialTheme.colors.primary else MaterialTheme.colors.error, modifier = Modifier.width(100.dp))
                    
                    Row(modifier = Modifier.width(150.dp)) {
                        TextButton(onClick = { onNavigateToForm(c.id) }) { Text("Edit") }
                        TextButton(
                            onClick = { 
                                customerToDelete = c
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

        if (showDeleteDialog && customerToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Hapus Customer") },
                text = { Text("Apakah Anda yakin ingin menghapus ${customerToDelete?.name}? Data terkait (faktur/pembayaran) juga dapat bermasalah jika ada dependensi.") },
                confirmButton = {
                    Button(
                        onClick = {
                            customerToDelete?.let {
                                try {
                                    customerRepository.delete(it.id)
                                    refreshTrigger++
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            showDeleteDialog = false
                            customerToDelete = null
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
