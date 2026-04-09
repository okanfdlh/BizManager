package com.bizmanager.presentation.screen.ledger

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.bizmanager.data.repository.ProductRepository
import com.bizmanager.domain.model.Customer
import com.bizmanager.domain.model.Invoice
import com.bizmanager.domain.model.Product
import com.bizmanager.domain.service.CustomerLedgerInvoice
import com.bizmanager.domain.service.CustomerLedgerReport
import com.bizmanager.domain.service.CustomerLedgerService
import com.bizmanager.domain.service.InvoiceItemInput
import com.bizmanager.domain.service.InvoiceService
import com.bizmanager.domain.service.PaymentService
import com.bizmanager.presentation.ui.toCurrencyLabel
import com.bizmanager.presentation.ui.toDateLabel
import com.bizmanager.presentation.ui.toDateTimeLabel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CustomerLedgerScreen(
    initialCustomerId: Int?,
    customerRepository: CustomerRepository,
    customerLedgerService: CustomerLedgerService,
    invoiceService: InvoiceService,
    paymentService: PaymentService,
    productRepository: ProductRepository,
    onNavigateToReport: (() -> Unit)? = null
) {
    var customers by remember { mutableStateOf<List<Customer>>(emptyList()) }
    var customerIdInput by remember { mutableStateOf("") }
    var customerNameInput by remember { mutableStateOf("") }
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var showCustomerList by remember { mutableStateOf(false) }
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
            val initial = customers.find { it.id == initialCustomerId }
            if (initial != null) {
                selectedCustomer = initial
                customerIdInput = initial.code
                customerNameInput = initial.name
            }
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

    fun handleFind() {
        val idQuery = customerIdInput.trim()
        val nameQuery = customerNameInput.trim()
        if (idQuery.isBlank() && nameQuery.isBlank()) return
        val match = customers.find { c ->
            (idQuery.isNotBlank() && (c.code.equals(idQuery, ignoreCase = true) || c.name.contains(idQuery, ignoreCase = true))) ||
            (nameQuery.isNotBlank() && c.name.contains(nameQuery, ignoreCase = true))
        }
        if (match != null) {
            selectedCustomer = match
            customerIdInput = match.code
            customerNameInput = match.name
        }
    }

    Row(modifier = Modifier.fillMaxSize()) {
        // Left panel: main content
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Buku Besar Customer", style = MaterialTheme.typography.headlineMedium)
                    if (onNavigateToReport != null) {
                        androidx.compose.material3.OutlinedButton(onClick = onNavigateToReport) {
                            Text("Report Buku Besar")
                        }
                    }
                }
            }

            // Frame Customer — sesuai spesifikasi PKE System
            item {
                CustomerFrame(
                    customerIdInput = customerIdInput,
                    customerNameInput = customerNameInput,
                    showCustomerList = showCustomerList,
                    onCustomerIdChange = { customerIdInput = it },
                    onCustomerNameChange = { customerNameInput = it },
                    onFind = { handleFind() },
                    onToggleList = { showCustomerList = !showCustomerList }
                )
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
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("Belum ada customer yang dipilih.", style = MaterialTheme.typography.titleLarge)
                                Text(
                                    "Masukan Customer ID atau Nama lalu klik Find, atau klik List Customer untuk memilih dari daftar.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
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

        // Right panel: Customer / Supplier list table
        if (showCustomerList) {
            CustomerListTable(
                customers = customers,
                selectedCustomer = selectedCustomer,
                modifier = Modifier
                    .width(360.dp)
                    .fillMaxHeight(),
                onSelect = { customer ->
                    selectedCustomer = customer
                    customerIdInput = customer.code
                    customerNameInput = customer.name
                }
            )
        }
    }

    // Dialogs
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

// ─── Frame Customer ────────────────────────────────────────────────────────────

@Composable
private fun CustomerFrame(
    customerIdInput: String,
    customerNameInput: String,
    showCustomerList: Boolean,
    onCustomerIdChange: (String) -> Unit,
    onCustomerNameChange: (String) -> Unit,
    onFind: () -> Unit,
    onToggleList: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Customer",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            // Baris Customer ID
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Customer ID",
                    modifier = Modifier.width(120.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = customerIdInput,
                    onValueChange = onCustomerIdChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    placeholder = { Text("Masukan Customer ID") }
                )
                Button(onClick = onFind) {
                    Text("Find")
                }
                OutlinedButton(onClick = onToggleList) {
                    Text(if (showCustomerList) "Close List" else "List Customer")
                }
            }

            // Baris Customer Name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Customer Name",
                    modifier = Modifier.width(120.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = customerNameInput,
                    onValueChange = onCustomerNameChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    placeholder = { Text("Nama customer") }
                )
            }
        }
    }
}

// ─── Tabel Customer / Supplier (panel kanan) ───────────────────────────────────

@Composable
private fun CustomerListTable(
    customers: List<Customer>,
    selectedCustomer: Customer?,
    modifier: Modifier = Modifier,
    onSelect: (Customer) -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(0.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header tabel
            Surface(color = MaterialTheme.colorScheme.primaryContainer) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Nr.",
                        modifier = Modifier.width(32.dp),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        "Cust/Supplier ID",
                        modifier = Modifier.width(108.dp),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        "Cust/Supplier Name",
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            HorizontalDivider()

            // Baris data
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(customers) { index, customer ->
                    val isSelected = selectedCustomer?.id == customer.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                when {
                                    isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f)
                                    index % 2 == 0 -> MaterialTheme.colorScheme.surface
                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                }
                            )
                            .clickable { onSelect(customer) }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${index + 1}",
                            modifier = Modifier.width(32.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            customer.code,
                            modifier = Modifier.width(108.dp),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        Text(
                            customer.name,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

// ─── Ringkasan Ledger ──────────────────────────────────────────────────────────

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
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
        shape = RoundedCornerShape(18.dp)
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

// ─── Kartu Invoice Ledger ──────────────────────────────────────────────────────

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
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
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
                                    Column(
                                        horizontalAlignment = Alignment.End,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
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
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
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
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
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

// ─── Dialog Quick Input Faktur ─────────────────────────────────────────────────

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
                    Checkbox(
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
                    var pmExp1 by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = pmExp1,
                        onExpandedChange = { pmExp1 = !pmExp1 }
                    ) {
                        OutlinedTextField(
                            value = paymentMethod,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Metode Pembayaran") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = pmExp1) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = pmExp1, onDismissRequest = { pmExp1 = false }) {
                            listOf("Transfer Bank", "Cash").forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = { paymentMethod = opt; pmExp1 = false }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val dt = try { LocalDate.parse(dateStr).atTime(LocalTime.now()) } catch (e: Exception) { LocalDateTime.now() }
                val qty = qtyStr.toIntOrNull() ?: 1
                val diskon = try { BigDecimal(diskonStr) } catch (e: Exception) { BigDecimal.ZERO }
                val manualTotal = try { BigDecimal(manualTotalStr) } catch (e: Exception) { BigDecimal.ZERO }
                val paid = try { BigDecimal(paidStr) } catch (e: Exception) { BigDecimal.ZERO }
                onSubmit(dt, noFaktur, notes, selectedProductId, qty, diskon, manualTotal, paid, isPosted, paymentMethod)
            }) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

// ─── Dialog Quick Terima Pembayaran ───────────────────────────────────────────

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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = invDropdownExpanded,
                        onExpandedChange = { invDropdownExpanded = !invDropdownExpanded }
                    ) {
                        val invName = openInvoices.find { it.id == selectedInvoiceId }
                            ?.let { "${it.invoiceNumber} (Sisa: ${it.balanceDue.toCurrencyLabel()})" }
                            ?: "Pilih Faktur"
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
                    var pmExp2 by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = pmExp2,
                        onExpandedChange = { pmExp2 = !pmExp2 }
                    ) {
                        OutlinedTextField(
                            value = paymentMethod,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Metode Pembayaran") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = pmExp2) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = pmExp2, onDismissRequest = { pmExp2 = false }) {
                            listOf("Transfer Bank", "Cash").forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = { paymentMethod = opt; pmExp2 = false }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (openInvoices.isNotEmpty()) {
                Button(onClick = {
                    val nominal = try { BigDecimal(nominalStr) } catch (e: Exception) { BigDecimal.ZERO }
                    if (selectedInvoiceId != null && nominal > BigDecimal.ZERO) {
                        onSubmit(selectedInvoiceId!!, nominal, paymentMethod)
                    }
                }) {
                    Text("Simpan Pembayaran")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (openInvoices.isEmpty()) "Tutup" else "Batal")
            }
        }
    )
}
