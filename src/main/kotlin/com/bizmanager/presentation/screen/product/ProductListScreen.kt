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
import com.bizmanager.presentation.ui.toCurrencyLabel

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

    LaunchedEffect(refreshTrigger) {
        products = productRepository.findAll(includeInactive = false)
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
            items(products) { p ->
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
