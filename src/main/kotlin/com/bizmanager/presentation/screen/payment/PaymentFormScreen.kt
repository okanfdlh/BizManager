package com.bizmanager.presentation.screen.payment

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bizmanager.data.repository.InvoiceRepository
import com.bizmanager.domain.model.Invoice
import com.bizmanager.domain.service.PaymentService
import java.math.BigDecimal

@Composable
fun PaymentFormScreen(
    initialInvoiceId: Int?,
    invoiceRepository: InvoiceRepository,
    paymentService: PaymentService,
    onBack: () -> Unit
) {
    var invoices by remember { mutableStateOf(emptyList<Invoice>()) }
    var selectedInvoiceId by remember { mutableStateOf<Int?>(initialInvoiceId) }
    
    var amountStr by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Transfer Bank") }
    var reference by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        // Load only invoices that can be paid
        invoices = invoiceRepository.findAllWithPositiveBalance().filter { it.invoiceStatus.name == "Posted" }
        
        // Auto-fill amount if pre-selected
        if (selectedInvoiceId != null) {
            val inv = invoices.find { it.id == selectedInvoiceId }
            if (inv != null) {
                amountStr = inv.balanceDue.toPlainString()
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
            Text("Penerimaan Pembayaran", style = MaterialTheme.typography.h4)
            Button(onClick = onBack) { Text("Batal") }
        }
        Spacer(Modifier.height(16.dp))

        if (errorMessage != null) {
            Text(errorMessage!!, color = MaterialTheme.colors.error)
            Spacer(Modifier.height(8.dp))
        }

        // Invoice Selection
        var invDropdownExpanded by remember { mutableStateOf(false) }
        val activeInv = invoices.find { it.id == selectedInvoiceId }
        Box {
            OutlinedButton(onClick = { invDropdownExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                val label = activeInv?.let { "${it.invoiceNumber} - Sisa Tagihan Rp${it.balanceDue}" } ?: "Pilih Faktur Belum Lunas *"
                Text(label)
            }
            DropdownMenu(expanded = invDropdownExpanded, onDismissRequest = { invDropdownExpanded = false }) {
                invoices.forEach { itm ->
                    DropdownMenuItem(onClick = { 
                        selectedInvoiceId = itm.id
                        amountStr = itm.balanceDue.toPlainString()
                        invDropdownExpanded = false 
                    }) {
                        Text("${itm.invoiceNumber} (Sisa: Rp${itm.balanceDue})")
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = amountStr,
            onValueChange = { amountStr = it },
            label = { Text("Nominal Pembayaran (Rp) *") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        
        OutlinedTextField(
            value = paymentMethod,
            onValueChange = { paymentMethod = it },
            label = { Text("Metode Pembayaran *") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        
        OutlinedTextField(
            value = reference,
            onValueChange = { reference = it },
            label = { Text("Nomor Referensi Transfer / Cek") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Catatan") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )
        
        Spacer(Modifier.height(24.dp))
        Button(
            modifier = Modifier.fillMaxWidth().height(50.dp),
            onClick = {
                val invId = selectedInvoiceId
                if (invId == null) {
                    errorMessage = "Pilih faktur terlebih dahulu."
                    return@Button
                }
                val bdAmount = try { BigDecimal(amountStr) } catch(e: Exception) { null }
                if (bdAmount == null || bdAmount <= BigDecimal.ZERO) {
                    errorMessage = "Nominal pembayaran tidak valid."
                    return@Button
                }
                
                try {
                    paymentService.addPayment(
                        invoiceId = invId,
                        amount = bdAmount,
                        paymentMethod = paymentMethod.ifBlank { "Unknown" },
                        reference = reference.ifBlank { null },
                        notes = notes.ifBlank { null }
                    )
                    onBack()
                } catch (e: Exception) {
                    errorMessage = e.message ?: "Gagal memproses pembayaran"
                }
            }
        ) {
            Text("Simpan Pembayaran")
        }
    }
}
