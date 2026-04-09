package com.bizmanager.presentation.screen.payment

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bizmanager.data.repository.CustomerRepository
import com.bizmanager.data.repository.InvoiceRepository
import com.bizmanager.domain.model.Customer
import com.bizmanager.domain.model.Invoice
import com.bizmanager.domain.model.InvoiceStatus
import com.bizmanager.domain.service.PaymentService
import com.bizmanager.presentation.ui.DatePickerField
import com.bizmanager.presentation.ui.toCurrencyLabel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

val PAYMENT_METHOD_OPTIONS = listOf("Transfer Bank", "Cash")

@Composable
fun PaymentFormScreen(
    initialInvoiceId: Int?,
    invoiceRepository: InvoiceRepository,
    paymentService: PaymentService,
    onBack: () -> Unit,
    customerRepository: CustomerRepository? = null
) {
    var invoices by remember { mutableStateOf(emptyList<Invoice>()) }
    var customerMap by remember { mutableStateOf(emptyMap<Int, Customer>()) }
    var selectedInvoiceId by remember { mutableStateOf<Int?>(initialInvoiceId) }

    var amountStr by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Transfer Bank") }
    var paymentDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    var reference by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isLoading = true
        val loaded = withContext(Dispatchers.IO) {
            invoiceRepository.findAll()
                .filter { it.invoiceStatus == InvoiceStatus.Posted && it.balanceDue.signum() > 0 }
        }
        invoices = loaded

        if (customerRepository != null) {
            customerMap = withContext(Dispatchers.IO) {
                customerRepository.findAll(includeInactive = true).associateBy { it.id }
            }
        }

        if (initialInvoiceId != null) {
            val inv = loaded.find { it.id == initialInvoiceId }
            if (inv != null) amountStr = inv.balanceDue.toPlainString()
        }
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Penerimaan Pembayaran", style = MaterialTheme.typography.h4)
            Button(onClick = onBack) { Text("Batal") }
        }

        if (errorMessage != null) {
            Text(errorMessage!!, color = MaterialTheme.colors.error)
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            if (invoices.isEmpty()) {
                Text(
                    "Tidak ada faktur yang bisa dibayar. Pastikan faktur sudah di-Post dan masih ada sisa tagihan.",
                    color = MaterialTheme.colors.error
                )
            }

            // ── Pilih Faktur ──────────────────────────────────────────────────
            var invDropdownExpanded by remember { mutableStateOf(false) }
            val activeInv = invoices.find { it.id == selectedInvoiceId }
            Box {
                OutlinedButton(
                    onClick = { if (invoices.isNotEmpty()) invDropdownExpanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = invoices.isNotEmpty()
                ) {
                    val label = activeInv?.let { inv ->
                        val custName = customerMap[inv.customerId]?.name ?: ""
                        "${inv.invoiceNumber}${if (custName.isNotBlank()) " · $custName" else ""} — Sisa: ${inv.balanceDue.toCurrencyLabel()}"
                    } ?: if (invoices.isEmpty()) "Tidak ada faktur tersedia" else "Pilih Faktur Belum Lunas *"
                    Text(label)
                }
                DropdownMenu(
                    expanded = invDropdownExpanded,
                    onDismissRequest = { invDropdownExpanded = false }
                ) {
                    invoices.forEach { inv ->
                        val custName = customerMap[inv.customerId]?.name ?: ""
                        DropdownMenuItem(onClick = {
                            selectedInvoiceId = inv.id
                            amountStr = inv.balanceDue.toPlainString()
                            invDropdownExpanded = false
                        }) {
                            Column {
                                Text(inv.invoiceNumber, style = MaterialTheme.typography.subtitle2)
                                if (custName.isNotBlank()) Text(custName, style = MaterialTheme.typography.caption)
                                Text(
                                    "Sisa: ${inv.balanceDue.toCurrencyLabel()}",
                                    style = MaterialTheme.typography.caption,
                                    color = MaterialTheme.colors.error
                                )
                            }
                        }
                    }
                }
            }

            // ── Nominal ───────────────────────────────────────────────────────
            OutlinedTextField(
                value = amountStr,
                onValueChange = { amountStr = it },
                label = { Text("Nominal Pembayaran (Rp) *") },
                modifier = Modifier.fillMaxWidth()
            )

            // ── Tanggal Pembayaran ────────────────────────────────────────────
            DatePickerField(
                label = "Tanggal Pembayaran",
                date = paymentDate,
                onSelect = { paymentDate = it },
                width = 200.dp
            )

            // ── Metode Pembayaran dropdown ────────────────────────────────────
            var pmExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { pmExpanded = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Metode: $paymentMethod")
                }
                DropdownMenu(expanded = pmExpanded, onDismissRequest = { pmExpanded = false }) {
                    PAYMENT_METHOD_OPTIONS.forEach { opt ->
                        DropdownMenuItem(onClick = { paymentMethod = opt; pmExpanded = false }) {
                            Text(opt)
                        }
                    }
                }
            }

            // ── Referensi & Catatan ───────────────────────────────────────────
            OutlinedTextField(
                value = reference,
                onValueChange = { reference = it },
                label = { Text("Nomor Referensi Transfer / Cek") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Catatan") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(Modifier.height(16.dp))
            Button(
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = invoices.isNotEmpty(),
                onClick = {
                    val invId = selectedInvoiceId
                    if (invId == null) { errorMessage = "Pilih faktur terlebih dahulu."; return@Button }
                    val bdAmount = try { BigDecimal(amountStr) } catch (e: Exception) { null }
                    if (bdAmount == null || bdAmount <= BigDecimal.ZERO) { errorMessage = "Nominal pembayaran tidak valid."; return@Button }
                    scope.launch {
                        try {
                            withContext(Dispatchers.IO) {
                                paymentService.addPayment(
                                    invoiceId = invId,
                                    amount = bdAmount,
                                    paymentMethod = paymentMethod,
                                    reference = reference.ifBlank { null },
                                    notes = notes.ifBlank { null },
                                    paymentDate = paymentDate?.atStartOfDay() ?: LocalDateTime.now()
                                )
                            }
                            onBack()
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Gagal memproses pembayaran"
                        }
                    }
                }
            ) {
                Text("Simpan Pembayaran")
            }
        }
    }
}
