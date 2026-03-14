package com.bizmanager.presentation.screen.product

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bizmanager.data.repository.ProductRepository
import com.bizmanager.domain.model.Product
import com.bizmanager.presentation.ui.toCurrencyLabel

@Composable
fun ProductDetailScreen(
    productId: Int,
    productRepository: ProductRepository,
    onBack: () -> Unit
) {
    var product by remember { mutableStateOf<Product?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    // In a real scenario we'd query InvoiceItems table filtering by productId to get total sold
    // We will leave placeholder state for the aggregations to be filled next as part of reports.
    var totalQtySold by remember { mutableStateOf(0) }
    var totalOmzet by remember { mutableStateOf(java.math.BigDecimal.ZERO) }
    var totalProfit by remember { mutableStateOf(java.math.BigDecimal.ZERO) }

    LaunchedEffect(productId) {
        product = productRepository.findById(productId)
        // TODO: Aggregate from InvoiceItems joined with Invoices where status != Cancelled
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Detail Produk: ${product?.name ?: "Loading..."}", style = MaterialTheme.typography.h4)
            Row {
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colors.error)
                ) {
                    Text("Hapus")
                }
                Spacer(Modifier.width(8.dp))
                Button(onClick = onBack) { Text("Kembali") }
            }
        }
        Spacer(Modifier.height(16.dp))
        
        Text("Total Terjual (Qty): $totalQtySold", style = MaterialTheme.typography.h6)
        Text("Total Omzet Historis: ${totalOmzet.toCurrencyLabel()}")
        Text("Total Laba Historis: ${totalProfit.toCurrencyLabel()}")
        
        Spacer(Modifier.height(24.dp))
        Text("Disini nantinya akan ada tabel Faktur yang menampung barang ini (opsional)")
        
        if (showDeleteDialog && product != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Hapus Produk") },
                text = { Text("Apakah Anda yakin ingin menghapus ${product?.name}? Data produk yang sudah di-faktur tidak akan bisa dihapus jika ada dependensi.") },
                confirmButton = {
                    Button(
                        onClick = {
                            product?.let {
                                try {
                                    productRepository.delete(it.id)
                                    onBack()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            showDeleteDialog = false
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
