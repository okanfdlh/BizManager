package com.bizmanager.presentation.screen.invoice

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bizmanager.data.repository.CustomerRepository
import com.bizmanager.data.repository.ProductRepository
import com.bizmanager.domain.model.Customer
import com.bizmanager.domain.model.Product
import com.bizmanager.domain.service.InvoiceItemInput
import com.bizmanager.domain.service.InvoiceService
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun InvoiceFormScreen(
    invoiceId: Int?,
    invoiceService: InvoiceService,
    customerRepository: CustomerRepository,
    productRepository: ProductRepository,
    onBack: () -> Unit
) {
    var customers by remember { mutableStateOf(emptyList<Customer>()) }
    var products by remember { mutableStateOf(emptyList<Product>()) }
    
    var selectedCustomerId by remember { mutableStateOf<Int?>(null) }
    var dueDate by remember { mutableStateOf(LocalDateTime.now().plusDays(30)) }
    var additionalCostStr by remember { mutableStateOf("0") }
    var notes by remember { mutableStateOf("") }
    
    // UI state for items
    val items = remember { mutableStateListOf<UiInvoiceItem>() }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        customers = customerRepository.findAll()
        products = productRepository.findAll()
        // If editing an existing draft, we would load existing logic here.
        // For simplicity of this structure, we assume new creation if not fully wired up.
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(if (invoiceId == null) "Buat Invoice Baru" else "Edit Draft Invoice", style = MaterialTheme.typography.h4)
            Row {
                Button(onClick = onBack, colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)) { Text("Batal") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { saveInvoice(invoiceId, true, selectedCustomerId, dueDate, additionalCostStr, notes, items, invoiceService, onBack, { errorMessage = it }) }) {
                    Text("Post Invoice (Kunci)")
                }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { saveInvoice(invoiceId, false, selectedCustomerId, dueDate, additionalCostStr, notes, items, invoiceService, onBack, { errorMessage = it }) }) {
                    Text("Simpan Draft")
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        if (errorMessage != null) {
            Text(errorMessage!!, color = MaterialTheme.colors.error)
            Spacer(Modifier.height(8.dp))
        }

        // Customer Selection (Simplified as dropdown or ID input for now)
        var custDropdownExpanded by remember { mutableStateOf(false) }
        Box {
            val selectedName = customers.find { it.id == selectedCustomerId }?.name ?: "Pilih Customer *"
            OutlinedButton(onClick = { custDropdownExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text(selectedName)
            }
            DropdownMenu(expanded = custDropdownExpanded, onDismissRequest = { custDropdownExpanded = false }) {
                customers.forEach { c ->
                    DropdownMenuItem(onClick = { selectedCustomerId = c.id; custDropdownExpanded = false }) {
                        Text("${c.code} - ${c.name}")
                    }
                }
            }
        }
        
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = dueDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                onValueChange = {}, // Readonly simplified
                label = { Text("Jatuh Tempo (YYYY-MM-DD)") },
                modifier = Modifier.weight(1f),
                readOnly = true
            )
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = additionalCostStr,
                onValueChange = { additionalCostStr = it },
                label = { Text("Biaya Tambahan Internal (Rp)") },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Catatan") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
        
        Spacer(Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Item Invoice", style = MaterialTheme.typography.h6)
            Button(onClick = { items.add(UiInvoiceItem(null, "1", "0")) }) { 
                Icon(Icons.Default.Add, contentDescription = "Tambah Item")
                Spacer(Modifier.width(4.dp))
                Text("Tambah Item") 
            }
        }
        Spacer(Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
            itemsIndexed(items) { index, item ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    var prodDropdownExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(2f)) {
                        val prodName = products.find { it.id == item.productId }?.name ?: "Pilih Produk"
                        OutlinedButton(onClick = { prodDropdownExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(prodName)
                        }
                        DropdownMenu(expanded = prodDropdownExpanded, onDismissRequest = { prodDropdownExpanded = false }) {
                            products.forEach { p ->
                                DropdownMenuItem(onClick = { 
                                    items[index] = item.copy(productId = p.id)
                                    prodDropdownExpanded = false 
                                }) { Text("${p.code} - ${p.name}") }
                            }
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = item.qtyStr, 
                        onValueChange = { items[index] = item.copy(qtyStr = it) },
                        label = { Text("Qty") },
                        modifier = Modifier.weight(0.5f)
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = item.discountStr, 
                        onValueChange = { items[index] = item.copy(discountStr = it) },
                        label = { Text("Diskon (Rp)") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = { items.removeAt(index) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colors.error)
                    }
                }
            }
        }
    }
}

data class UiInvoiceItem(
    val productId: Int?,
    val qtyStr: String,
    val discountStr: String
)

private fun saveInvoice(
    invoiceId: Int?,
    isPosted: Boolean,
    selectedCustomerId: Int?,
    dueDate: LocalDateTime,
    additionalCostStr: String,
    notes: String,
    uiItems: List<UiInvoiceItem>,
    invoiceService: InvoiceService,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    if (selectedCustomerId == null) {
        onError("Pilih customer terlebih dahulu.")
        return
    }
    if (uiItems.isEmpty()) {
        onError("Invoice minimal harus memiliki 1 item.")
        return
    }
    if (uiItems.any { it.productId == null }) {
        onError("Pastikan semua item sudah memilih produk.")
        return
    }

    try {
        val addCost = if (additionalCostStr.isBlank()) BigDecimal.ZERO else BigDecimal(additionalCostStr)
        val inputs = uiItems.map { 
            InvoiceItemInput(
                productId = it.productId!!,
                qty = it.qtyStr.toIntOrNull() ?: 1,
                discount = if (it.discountStr.isBlank()) BigDecimal.ZERO else BigDecimal(it.discountStr)
            )
        }

        if (invoiceId == null) {
            invoiceService.createInvoice(
                customerId = selectedCustomerId,
                dueDate = dueDate,
                additionalCost = addCost,
                notes = notes.ifBlank { null },
                isDraft = !isPosted,
                itemsInput = inputs
            )
        } else {
            invoiceService.updateDraftInvoice(
                invoiceId = invoiceId,
                dueDate = dueDate,
                additionalCost = addCost,
                notes = notes.ifBlank { null },
                postInvoice = isPosted,
                itemsInput = inputs
            )
        }
        onSuccess()
    } catch (e: Exception) {
        onError(e.message ?: "Telah terjadi kesalahan")
    }
}
