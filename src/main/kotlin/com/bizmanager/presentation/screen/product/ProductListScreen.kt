package com.bizmanager.presentation.screen.product

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
import com.bizmanager.data.repository.ProductRepository
import com.bizmanager.domain.model.Product
import com.bizmanager.presentation.ui.PaginationControl
import com.bizmanager.presentation.ui.paginateList
import com.bizmanager.presentation.ui.toCurrencyLabel
import kotlin.math.ceil

@Composable
fun ProductListScreen(
    productRepository: ProductRepository,
    onNavigateToForm: (Int?) -> Unit,
    onNavigateToDetail: (Int) -> Unit
) {
    var products by remember { mutableStateOf(emptyList<Product>()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }
    var refreshTrigger by remember { mutableStateOf(0) }

    // Filter & Pagination States
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("Semua") } // "Semua", "Aktif", "Nonaktif"
    
    var currentPage by remember { mutableStateOf(1) }
    var pageSize by remember { mutableStateOf(20) }

    LaunchedEffect(refreshTrigger) {
        products = productRepository.findAll(includeInactive = true)
    }

    // Advanced in-memory filtering
    val filteredProducts by remember(products, searchQuery, statusFilter) {
        derivedStateOf {
            products.filter { p ->
                val matchesSearch = p.name.contains(searchQuery, ignoreCase = true) ||
                        p.code.contains(searchQuery, ignoreCase = true)
                
                val matchesStatus = when (statusFilter) {
                    "Aktif" -> p.isActive
                    "Nonaktif" -> !p.isActive
                    else -> true
                }
                
                matchesSearch && matchesStatus
            }
        }
    }

    // Pagination Logic
    val totalElements = filteredProducts.size
    val totalPages = if (totalElements == 0) 1 else ceil(totalElements.toDouble() / pageSize).toInt()
    
    // Auto-adjust page if out of bounds after filtering
    LaunchedEffect(totalPages) {
        if (currentPage > totalPages) currentPage = totalPages
        if (currentPage < 1) currentPage = 1
    }

    val paginatedProducts by remember(filteredProducts, currentPage, pageSize) {
        derivedStateOf {
            paginateList(filteredProducts, currentPage, pageSize)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Daftar Produk", style = MaterialTheme.typography.h4)
            Button(onClick = { onNavigateToForm(null) }) {
                Icon(Icons.Default.Add, contentDescription = "Tambah")
                Spacer(Modifier.width(8.dp))
                Text("Tambah Produk")
            }
        }
        Spacer(Modifier.height(16.dp))

        // Filter Controls
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it; currentPage = 1 },
                label = { Text("Cari (Nama, Kode)") },
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

        Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Text("Kode", modifier = Modifier.weight(1f), style = MaterialTheme.typography.subtitle2)
            Text("Nama", modifier = Modifier.weight(2f), style = MaterialTheme.typography.subtitle2)
            Text("Harga Beli", modifier = Modifier.weight(1f), style = MaterialTheme.typography.subtitle2)
            Text("Harga Jual", modifier = Modifier.weight(1f), style = MaterialTheme.typography.subtitle2)
            Text("Status", modifier = Modifier.width(100.dp), style = MaterialTheme.typography.subtitle2)
            Text("Aksi", modifier = Modifier.width(150.dp), style = MaterialTheme.typography.subtitle2)
        }
        Divider()

        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
            items(paginatedProducts) { p ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigateToDetail(p.id) }.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(p.code, modifier = Modifier.weight(1f))
                    Text(p.name, modifier = Modifier.weight(2f))
                    Text(p.costPrice.toCurrencyLabel(), modifier = Modifier.weight(1f))
                    Text(p.sellPrice.toCurrencyLabel(), modifier = Modifier.weight(1f))
                    Text(if (p.isActive) "Aktif" else "Nonaktif", color = if (p.isActive) MaterialTheme.colors.primary else MaterialTheme.colors.error, modifier = Modifier.width(100.dp))
                    
                    Row(modifier = Modifier.width(150.dp)) {
                        TextButton(onClick = { onNavigateToForm(p.id) }) { Text("Edit") }
                        TextButton(
                            onClick = { 
                                productToDelete = p
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
        
        if (showDeleteDialog && productToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Hapus Produk") },
                text = { Text("Apakah Anda yakin ingin menghapus ${productToDelete?.name}? Data yang terkait dengan faktur tidak dapat dihapus.") },
                confirmButton = {
                    Button(
                        onClick = {
                            productToDelete?.let {
                                try {
                                    productRepository.delete(it.id)
                                    refreshTrigger++
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            showDeleteDialog = false
                            productToDelete = null
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
