package com.bizmanager.presentation.screen.ledger

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bizmanager.data.repository.CustomerRepository
import com.bizmanager.domain.model.Customer
import com.bizmanager.domain.service.CustomerLedgerInvoice
import com.bizmanager.domain.service.CustomerLedgerReport
import com.bizmanager.domain.service.CustomerLedgerService
import com.bizmanager.presentation.ui.toCurrencyLabel
import com.bizmanager.presentation.ui.toDateLabel
import com.bizmanager.presentation.ui.toDateTimeLabel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import com.bizmanager.domain.model.Invoice
import com.bizmanager.domain.service.InvoiceService
import com.bizmanager.domain.service.PaymentService
import com.bizmanager.data.repository.ProductRepository
import com.bizmanager.domain.model.Product
import com.bizmanager.domain.service.InvoiceItemInput
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.LocalDate
import java.time.LocalTime
import java.math.BigDecimal
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CustomerLedgerScreen(
    initialCustomerId: Int?,
    customerRepository: CustomerRepository,
    customerLedgerService: CustomerLedgerService,
    invoiceService: InvoiceService,
    paymentService: PaymentService,
    productRepository: ProductRepository
) {
    var customers by remember { mutableStateOf<List<Customer>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var ledgerReport by remember { mutableStateOf<CustomerLedgerReport?>(null) }
    var loading by remember { mutableStateOf(false) }
    val expandedInvoices = remember { mutableStateMapOf<Int, Boolean>() }
    
    var showQuickFakturDialog by remember { mutableStateOf(false) }
    var showQuickPaymentDialog by remember { mutableStateOf(false) }
    var productsList by remember { mutableStateOf<List<Product>>(emptyList()) }
    var loadTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        customers = withContext(Dispatchers.IO) {
            customerRepository.findAll(includeInactive = true)
        }
        if (initialCustomerId != null) {
            selectedCustomer = customers.find { it.id == initialCustomerId }
            selectedCustomer?.let { query = it.name }
        }
        productsList = withContext(Dispatchers.IO) {
            productRepository.findAll(includeInactive = false)
        }
    }

    LaunchedEffect(selectedCustomer?.id, loadTrigger) {
        val customer = selectedCustomer ?: run {
            ledgerReport = null
            return@LaunchedEffect
        }

        loading = true
        ledgerReport = withContext(Dispatchers.IO) {
            customerLedgerService.getCustomerLedger(customer.id)
        }
        loading = false
    }

    val filteredCustomers = customers.filter { customer ->
        query.isBlank() ||
            customer.name.contains(query, ignoreCase = true) ||
            customer.code.contains(query, ignoreCase = true) ||
            (customer.company?.contains(query, ignoreCase = true) == true)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Buku Besar Customer", style = MaterialTheme.typography.headlineMedium)
        }

        item {
            Text(
                "Cari customer, pilih dari dropdown, lalu review semua faktur, item produk, histori pembayaran, dan posisi hutang per faktur.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = {
                        query = it
                        expanded = true
                    },
                    label = { Text("Cari customer / company / kode") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    filteredCustomers.forEach { customer ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(customer.name, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        listOfNotNull(customer.code, customer.company).joinToString(" • "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                selectedCustomer = customer
                                query = customer.name
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        when {
            loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            ledgerReport == null -> {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 4.dp,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("Belum ada customer yang dipilih.", style = MaterialTheme.typography.titleLarge)
                            Text("Gunakan search di atas untuk memilih customer dan memunculkan ledger lengkap.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            else -> {
                val report = ledgerReport!!
                item {
                    LedgerSummary(
                        report = report,
                        onQuickInput = { showQuickFakturDialog = true },
                        onQuickPayment = { showQuickPaymentDialog = true }
                    )
                }
                items(report.invoices, key = { it.invoice.id }) { entry ->
                    val isExpanded = expandedInvoices[entry.invoice.id] ?: false
                    InvoiceLedgerCard(
                        entry = entry,
                        expanded = isExpanded,
                        onToggle = { expandedInvoices[entry.invoice.id] = !isExpanded }
                    )
                }
            }
        }
    }
    
    if (showQuickFakturDialog && selectedCustomer != null) {
        QuickFakturDialog(
            customer = selectedCustomer!!,
            products = productsList,
            onDismiss = { showQuickFakturDialog = false },
            onSubmit = { tanggal, noFaktur, notes, productId, qty, diskon, manualTotal, paid, isPosted, paymentMethod ->
                try {
                    val inv = invoiceService.createInvoice(
                        customerId = selectedCustomer!!.id,
                        dueDate = tanggal.plusDays(30),
                        additionalCost = BigDecimal.ZERO,
                        notes = notes,
                        isDraft = !isPosted,
                        itemsInput = if (productId != null) listOf(InvoiceItemInput(productId, qty, diskon)) else emptyList(),
                        customDate = tanggal,
                        customInvoiceNumber = noFaktur.ifBlank { null },
                        manualTotal = manualTotal
                    )
                    
                    if (isPosted && paid > BigDecimal.ZERO) {
                        paymentService.addPayment(
                            invoiceId = inv.id,
                            amount = paid,
                            paymentMethod = paymentMethod,
                            reference = null,
                            notes = "Quick Payment"
                        )
                    }
                    showQuickFakturDialog = false
                    loadTrigger++
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        )
    }

    if (showQuickPaymentDialog && selectedCustomer != null && ledgerReport != null) {
        val openInvoices = ledgerReport!!.invoices.filter { !it.isSettled }.map { it.invoice }
        QuickPaymentDialog(
            customer = selectedCustomer!!,
            openInvoices = openInvoices,
            onDismiss = { showQuickPaymentDialog = false },
            onSubmit = { invoiceId, nominal, paymentMethod ->
                try {
                    paymentService.addPayment(
                        invoiceId = invoiceId,
                        amount = nominal,
                        paymentMethod = paymentMethod,
                        reference = null,
                        notes = "Quick Payment"
                    )
                    showQuickPaymentDialog = false
                    loadTrigger++
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LedgerSummary(report: CustomerLedgerReport, onQuickInput: () -> Unit, onQuickPayment: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(report.customer.name, style = MaterialTheme.typography.headlineSmall)
                    Text(
                        listOfNotNull(report.customer.company, report.customer.phone, report.customer.email).joinToString(" • "),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onQuickInput) {
                        Text("Quick Input Faktur")
                    }
                    Button(
                        onClick = onQuickPayment,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Terima Pembayaran")
                    }
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryMiniCard("Total Faktur", report.summary.totalInvoices.toString())
                SummaryMiniCard("Faktur Ongoing", report.summary.openInvoices.toString())
                SummaryMiniCard("Faktur Lunas", report.summary.settledInvoices.toString())
                SummaryMiniCard("Total Tagihan", report.summary.totalInvoiced.toCurrencyLabel())
                SummaryMiniCard("Total Terbayar", report.summary.totalPaid.toCurrencyLabel())
                SummaryMiniCard("Sisa Hutang", report.summary.totalOutstanding.toCurrencyLabel())
                SummaryMiniCard("Laba Kotor", report.summary.totalGrossProfit.toCurrencyLabel())
                SummaryMiniCard("Laba Bersih", report.summary.totalNetProfit.toCurrencyLabel())
                SummaryMiniCard("Baris Produk", report.summary.totalProductLines.toString())
            }
        }
    }
}

@Composable
private fun SummaryMiniCard(title: String, value: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .width(180.dp)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InvoiceLedgerCard(
    entry: CustomerLedgerInvoice,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(entry.invoice.invoiceNumber, style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Tanggal ${entry.invoice.date.toDateLabel()} • Jatuh tempo ${entry.invoice.dueDate.toDateLabel()}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(onClick = onToggle) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (expanded) "Sembunyikan" else "Lihat detail")
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    selected = entry.isSettled,
                    onClick = {},
                    label = { Text(if (entry.isSettled) "Lunas" else "Ongoing") }
                )
                AssistChip(onClick = {}, label = { Text("Status faktur: ${entry.invoice.invoiceStatus.name}") })
                AssistChip(onClick = {}, label = { Text("Status bayar: ${entry.invoice.paymentStatus.name}") })
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryMiniCard("Nilai Faktur", entry.invoice.grandTotal.toCurrencyLabel())
                SummaryMiniCard("Total Terbayar", entry.totalPaid.toCurrencyLabel())
                SummaryMiniCard("Sisa Hutang", entry.outstanding.toCurrencyLabel())
                SummaryMiniCard("Laba Bersih", entry.invoice.netProfit.toCurrencyLabel())
            }

            if (expanded) {
                HorizontalDivider()

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Produk pada faktur", style = MaterialTheme.typography.titleMedium)
                    if (entry.items.isEmpty()) {
                        Text("Tidak ada item produk tercatat.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        entry.items.forEach { item ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(item.productNameSnapshot, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            "${item.qty} ${item.unitSnapshot ?: ""} • ${item.productCodeSnapshot}",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(item.subtotal.toCurrencyLabel(), fontWeight = FontWeight.SemiBold)
                                        Text("Profit ${item.grossProfit.toCurrencyLabel()}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Riwayat transaksi", style = MaterialTheme.typography.titleMedium)
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                            Text("Faktur dibuat", fontWeight = FontWeight.SemiBold)
                            Text(entry.invoice.date.toDateTimeLabel(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (!entry.invoice.notes.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(entry.invoice.notes, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    if (entry.payments.isEmpty()) {
                        Text("Belum ada pembayaran masuk untuk faktur ini.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        entry.payments.forEach { payment ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(payment.paymentNumber, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            "${payment.date.toDateTimeLabel()} • ${payment.paymentMethod}",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (!payment.reference.isNullOrBlank() || !payment.notes.isNullOrBlank()) {
                                            Text(
                                                listOfNotNull(payment.reference, payment.notes).joinToString(" • "),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Text(payment.amount.toCurrencyLabel(), style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickFakturDialog(
    customer: Customer,
    products: List<Product>,
    onDismiss: () -> Unit,
    onSubmit: (
        tanggal: LocalDateTime,
        noFaktur: String,
        notes: String,
        productId: Int?,
        qty: Int,
        diskon: BigDecimal,
        manualTotal: BigDecimal,
        paid: BigDecimal,
        isPosted: Boolean,
        paymentMethod: String
    ) -> Unit
) {
    var dateStr by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))) }
    var noFaktur by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var selectedProductId by remember { mutableStateOf<Int?>(null) }
    var prodDropdownExpanded by remember { mutableStateOf(false) }

    var qtyStr by remember { mutableStateOf("1") }
    var diskonStr by remember { mutableStateOf("0") }
    var manualTotalStr by remember { mutableStateOf("0") }

    var isPosted by remember { mutableStateOf(false) }
    var paidStr by remember { mutableStateOf("0") }
    var paymentMethod by remember { mutableStateOf("Transfer Bank") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Quick Input Faktur - ${customer.name}") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = dateStr,
                    onValueChange = { dateStr = it },
                    label = { Text("Tanggal (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = noFaktur,
                    onValueChange = { noFaktur = it },
                    label = { Text("No. Faktur (Kosong = Otomatis)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Deskripsi / Catatan") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                ExposedDropdownMenuBox(
                    expanded = prodDropdownExpanded,
                    onExpandedChange = { prodDropdownExpanded = !prodDropdownExpanded }
                ) {
                    val pName = products.find { it.id == selectedProductId }?.name ?: "Tanpa Produk (Input Total Manual)"
                    OutlinedTextField(
                        value = pName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pilih Produk") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = prodDropdownExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = prodDropdownExpanded,
                        onDismissRequest = { prodDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Tanpa Produk (Input Total Manual)") },
                            onClick = { selectedProductId = null; prodDropdownExpanded = false }
                        )
                        products.forEach { p ->
                            DropdownMenuItem(
                                text = { Text("${p.code} - ${p.name}") },
                                onClick = { selectedProductId = p.id; prodDropdownExpanded = false }
                            )
                        }
                    }
                }

                if (selectedProductId != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = qtyStr,
                            onValueChange = { qtyStr = it },
                            label = { Text("Qty") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = diskonStr,
                            onValueChange = { diskonStr = it },
                            label = { Text("Diskon (Rp)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = manualTotalStr,
                        onValueChange = { manualTotalStr = it },
                        label = { Text("Total Faktur Manual (Rp)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.Checkbox(
                        checked = isPosted,
                        onCheckedChange = { isPosted = it }
                    )
                    Text("Post Faktur (Kunci dokumen & aktifkan bayar)")
                }
                
                if (isPosted) {
                    OutlinedTextField(
                        value = paidStr,
                        onValueChange = { paidStr = it },
                        label = { Text("Nominal Dibayar (Rp)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = paymentMethod,
                        onValueChange = { paymentMethod = it },
                        label = { Text("Metode Pembayaran") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val dt = try { LocalDate.parse(dateStr).atTime(LocalTime.now()) } catch(e: Exception) { LocalDateTime.now() }
                val qty = qtyStr.toIntOrNull() ?: 1
                val diskon = try { BigDecimal(diskonStr) } catch(e: Exception) { BigDecimal.ZERO }
                val manualTotal = try { BigDecimal(manualTotalStr) } catch(e: Exception) { BigDecimal.ZERO }
                val paid = try { BigDecimal(paidStr) } catch(e: Exception) { BigDecimal.ZERO }
                
                onSubmit(dt, noFaktur, notes, selectedProductId, qty, diskon, manualTotal, paid, isPosted, paymentMethod)
            }) {
                Text("Simpan")
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickPaymentDialog(
    customer: Customer,
    openInvoices: List<Invoice>,
    onDismiss: () -> Unit,
    onSubmit: (invoiceId: Int, nominal: BigDecimal, paymentMethod: String) -> Unit
) {
    var selectedInvoiceId by remember { mutableStateOf<Int?>(openInvoices.firstOrNull()?.id) }
    var invDropdownExpanded by remember { mutableStateOf(false) }
    
    var nominalStr by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Transfer Bank") }

    LaunchedEffect(selectedInvoiceId) {
        val inv = openInvoices.find { it.id == selectedInvoiceId }
        if (inv != null && nominalStr.isBlank()) {
            nominalStr = inv.balanceDue.toPlainString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Quick Terima Pembayaran - ${customer.name}") },
        text = {
            if (openInvoices.isEmpty()) {
                Text("Tidak ada faktur yang belum lunas (outstanding) untuk customer ini.")
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = invDropdownExpanded,
                        onExpandedChange = { invDropdownExpanded = !invDropdownExpanded }
                    ) {
                        val invName = openInvoices.find { it.id == selectedInvoiceId }?.let { "${it.invoiceNumber} (Sisa: ${it.balanceDue.toCurrencyLabel()})" } ?: "Pilih Faktur"
                        OutlinedTextField(
                            value = invName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Pilih Faktur") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = invDropdownExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = invDropdownExpanded,
                            onDismissRequest = { invDropdownExpanded = false },
                            modifier = Modifier.heightIn(max = 250.dp)
                        ) {
                            openInvoices.forEach { inv ->
                                DropdownMenuItem(
                                    text = { Text("${inv.invoiceNumber} - Sisa: ${inv.balanceDue.toCurrencyLabel()}") },
                                    onClick = { 
                                        selectedInvoiceId = inv.id
                                        nominalStr = inv.balanceDue.toPlainString()
                                        invDropdownExpanded = false 
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = nominalStr,
                        onValueChange = { nominalStr = it },
                        label = { Text("Nominal Dibayar (Rp)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = paymentMethod,
                        onValueChange = { paymentMethod = it },
                        label = { Text("Metode Pembayaran") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            if (openInvoices.isNotEmpty()) {
                Button(onClick = {
                    val nominal = try { BigDecimal(nominalStr) } catch(e: Exception) { BigDecimal.ZERO }
                    if (selectedInvoiceId != null && nominal > BigDecimal.ZERO) {
                        onSubmit(selectedInvoiceId!!, nominal, paymentMethod)
                    }
                }) {
                    Text("Simpan Pembayaran")
                }
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text(if (openInvoices.isEmpty()) "Tutup" else "Batal")
            }
        }
    )
}

