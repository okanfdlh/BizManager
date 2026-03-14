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
import java.math.BigDecimal
import java.time.LocalDateTime

@Composable
fun ProductFormScreen(
    productId: Int?,
    productRepository: ProductRepository,
    onBack: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var costPriceStr by remember { mutableStateOf("") }
    var sellPriceStr by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(true) }
    
    var existingProduct by remember { mutableStateOf<Product?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(productId) {
        if (productId != null) {
            existingProduct = productRepository.findById(productId)
            existingProduct?.let {
                code = it.code
                name = it.name
                category = it.category ?: ""
                unit = it.unit ?: ""
                costPriceStr = it.costPrice.toPlainString()
                sellPriceStr = it.sellPrice.toPlainString()
                isActive = it.isActive
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(if (productId == null) "Tambah Produk Baru" else "Edit Produk", style = MaterialTheme.typography.h4)
            Button(onClick = onBack) { Text("Kembali") }
        }
        Spacer(Modifier.height(16.dp))

        if (errorMessage != null) {
            Text(errorMessage!!, color = MaterialTheme.colors.error)
            Spacer(Modifier.height(8.dp))
        }

        OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Kode Produk *") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Produk *") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Kategori") }, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Satuan (pcs, kg, dll)") }, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(value = costPriceStr, onValueChange = { costPriceStr = it }, label = { Text("Harga Modal (HPP)*") }, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(value = sellPriceStr, onValueChange = { sellPriceStr = it }, label = { Text("Harga Jual*") }, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Checkbox(checked = isActive, onCheckedChange = { isActive = it })
            Text("Produk Aktif (Muncul saat buat faktur)")
        }
        
        Spacer(Modifier.height(24.dp))

        Button(
            modifier = Modifier.fillMaxWidth().height(50.dp),
            onClick = {
                if (code.isBlank() || name.isBlank() || costPriceStr.isBlank() || sellPriceStr.isBlank()) {
                    errorMessage = "Mohon isi semua field ber-bintang (*)"
                    return@Button
                }
                try {
                    val costBD = BigDecimal(costPriceStr)
                    val sellBD = BigDecimal(sellPriceStr)

                    val product = Product(
                        id = productId ?: 0,
                        code = code,
                        name = name,
                        category = category.ifBlank { null },
                        unit = unit.ifBlank { null },
                        costPrice = costBD,
                        sellPrice = sellBD,
                        isActive = isActive,
                        createdAt = existingProduct?.createdAt ?: LocalDateTime.now(),
                        updatedAt = LocalDateTime.now()
                    )

                    if (productId == null) {
                        productRepository.insert(product)
                    } else {
                        productRepository.update(product)
                    }
                    onBack()
                } catch (e: NumberFormatException) {
                    errorMessage = "Format angka harga tidak valid."
                } catch (e: Exception) {
                    errorMessage = "Gagal menyimpan: ${e.message}"
                }
            }
        ) {
            Text("Simpan")
        }
    }
}
