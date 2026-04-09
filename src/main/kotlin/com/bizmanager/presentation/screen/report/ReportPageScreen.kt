package com.bizmanager.presentation.screen.report

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bizmanager.data.repository.CustomerRepository
import com.bizmanager.domain.model.Customer
import com.bizmanager.domain.service.AmountOperator
import com.bizmanager.domain.service.BukuBesarFilter
import com.bizmanager.domain.service.BukuBesarResult
import com.bizmanager.domain.service.BukuBesarRow
import com.bizmanager.domain.service.BukuBesarStatus
import com.bizmanager.domain.service.ReportService
import com.bizmanager.presentation.ui.DatePickerField
import com.bizmanager.presentation.ui.toCurrencyLabel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy")
private val DATE_FMT_SHORT = DateTimeFormatter.ofPattern("dd/MM/yy")

val PAYMENT_TYPES = listOf("Semua", "Transfer Bank", "Cash")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportPageScreen(
    reportService: ReportService,
    customerRepository: CustomerRepository
) {
    val scope = rememberCoroutineScope()

    // ── Filter state ─────────────────────────────────────────────────────────
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }
    var amountStr by remember { mutableStateOf("0") }
    var amountGte by remember { mutableStateOf(true) }  // true = >=, false = <=
    var selectedStatus by remember { mutableStateOf<BukuBesarStatus?>(null) }
    var customerQuery by remember { mutableStateOf("") }
    var paymentTypeQuery by remember { mutableStateOf("") }
    var usingData by remember { mutableStateOf("Both") }  // "Customer" / "Supplier" / "Both"

    // ── Result state ──────────────────────────────────────────────────────────
    var result by remember { mutableStateOf<BukuBesarResult?>(null) }
    var currentPage by remember { mutableStateOf(1) }
    var loading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // ── Customer list popup ───────────────────────────────────────────────────
    var showCustomerList by remember { mutableStateOf(false) }
    var customerListData by remember { mutableStateOf<List<Customer>>(emptyList()) }
    var customerListType by remember { mutableStateOf("Customer") }  // "Customer" / "Supplier"

    fun buildFilter(): BukuBesarFilter {
        val amount = try { BigDecimal(amountStr) } catch (e: Exception) { null }
        return BukuBesarFilter(
            startDate = startDate?.atStartOfDay(),
            endDate = endDate?.atTime(23, 59, 59),
            amountValue = amount,
            amountOperator = if (amountGte) AmountOperator.GTE else AmountOperator.LTE,
            statusAs = selectedStatus,
            customerQuery = customerQuery.trim(),
            paymentType = paymentTypeQuery.trim()
        )
    }

    fun doSubmit(page: Int = 1) {
        scope.launch {
            loading = true
            errorMsg = null
            try {
                val filter = buildFilter()
                val res = withContext(Dispatchers.IO) {
                    reportService.getBukuBesarReport(filter, page)
                }
                result = res
                currentPage = res.currentPage
            } catch (e: Exception) {
                errorMsg = e.message ?: "Terjadi kesalahan"
            } finally {
                loading = false
            }
        }
    }

    fun doReset() {
        startDate = null
        endDate = null
        amountStr = "0"
        amountGte = true
        selectedStatus = null
        customerQuery = ""
        paymentTypeQuery = ""
        usingData = "Both"
        result = null
        currentPage = 1
        errorMsg = null
    }

    fun openCustomerList() {
        scope.launch {
            customerListData = withContext(Dispatchers.IO) {
                customerRepository.findAll(includeInactive = true)
            }
            showCustomerList = true
        }
    }

    // ── Layout ────────────────────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Report Buku Besar", style = MaterialTheme.typography.headlineMedium)

        // ── Selected Criteria frame ───────────────────────────────────────────
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Selected Criteria",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                // Main criteria row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Left: Period + Amount
                    Column(
                        modifier = Modifier.weight(1.2f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Period
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Period",
                                modifier = Modifier.width(44.dp).padding(bottom = 14.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            DatePickerField(
                                label = "from",
                                date = startDate,
                                onSelect = { startDate = it },
                                width = 150.dp
                            )
                            DatePickerField(
                                label = "to",
                                date = endDate,
                                onSelect = { endDate = it },
                                width = 150.dp
                            )
                        }

                        // Amount
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Amount Rp.", modifier = Modifier.width(80.dp), style = MaterialTheme.typography.bodyMedium)
                            OutlinedTextField(
                                value = amountStr,
                                onValueChange = { amountStr = it },
                                modifier = Modifier.width(120.dp),
                                singleLine = true
                            )
                            Text("for:", style = MaterialTheme.typography.bodySmall)
                            RadioButton(selected = amountGte, onClick = { amountGte = true })
                            Text(">=", style = MaterialTheme.typography.bodySmall)
                            RadioButton(selected = !amountGte, onClick = { amountGte = false })
                            Text("<=", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    // Center: Customer ID/Name + Payment Type + Using data
                    Column(
                        modifier = Modifier.weight(1.3f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Customer ID/Name + List button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "Customer ID / Name",
                                modifier = Modifier.width(130.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            OutlinedTextField(
                                value = customerQuery,
                                onValueChange = { customerQuery = it },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Button(
                                onClick = { openCustomerList() },
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text("List")
                            }
                        }

                        // Payment Type dropdown
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "Payment Type",
                                modifier = Modifier.width(130.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            var ptExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = ptExpanded,
                                onExpandedChange = { ptExpanded = !ptExpanded },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = if (paymentTypeQuery.isBlank()) "Semua" else paymentTypeQuery,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ptExpanded) },
                                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                                    singleLine = true
                                )
                                ExposedDropdownMenu(
                                    expanded = ptExpanded,
                                    onDismissRequest = { ptExpanded = false }
                                ) {
                                    PAYMENT_TYPES.forEach { opt ->
                                        DropdownMenuItem(
                                            text = { Text(opt) },
                                            onClick = {
                                                paymentTypeQuery = if (opt == "Semua") "" else opt
                                                ptExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Using data
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                "Using data",
                                modifier = Modifier.width(130.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            listOf("Customer", "Supplier", "Both").forEach { option ->
                                RadioButton(
                                    selected = usingData == option,
                                    onClick = { usingData = option }
                                )
                                Text(option, style = MaterialTheme.typography.bodySmall)
                                Spacer(Modifier.width(4.dp))
                            }
                        }
                    }

                    // Right: Action buttons
                    Column(
                        modifier = Modifier.width(100.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { doSubmit(1) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !loading
                        ) {
                            Text("Submit")
                        }
                        OutlinedButton(
                            onClick = { doReset() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Reset")
                        }
                        OutlinedButton(
                            onClick = { /* Print placeholder */ },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Print")
                        }
                    }
                }

                // Status as row (full width)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Status as :", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.width(4.dp))
                    listOf(
                        null to "All",
                        BukuBesarStatus.Closed to "Closed",
                        BukuBesarStatus.New to "New",
                        BukuBesarStatus.Outstanding to "Outstanding",
                        BukuBesarStatus.Unpaid to "Unpaid",
                        BukuBesarStatus.AllOutstanding to "All Outstanding"
                    ).forEach { (status, label) ->
                        RadioButton(
                            selected = selectedStatus == status,
                            onClick = { selectedStatus = status }
                        )
                        Text(label, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.width(6.dp))
                    }
                }
            }
        }

        // ── Error ─────────────────────────────────────────────────────────────
        if (errorMsg != null) {
            Text(
                errorMsg!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // ── Loading ───────────────────────────────────────────────────────────
        if (loading) {
            Text("Memuat data…", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // ── Results ───────────────────────────────────────────────────────────
        result?.let { res ->
            // Info bar: FOUNDED + TOTAL MARGIN + PAGE + Prev/Next
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        Text(
                            "FOUNDED : ${res.foundCount} Faktur",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "TOTAL MARGIN : ${res.totalMargin.toCurrencyLabel()}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (res.totalPages > 1 && res.currentPage > 1) {
                            OutlinedButton(
                                onClick = { doSubmit(currentPage - 1) },
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("<< Previous", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Text(
                            "PAGE : ${res.currentPage} OF ${res.totalPages}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (res.totalPages > 1 && res.currentPage < res.totalPages) {
                            Button(
                                onClick = { doSubmit(currentPage + 1) },
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("Next >>", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            // Totals bar: Total Paid + Total Outstanding + Total Amount
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TotalCell("Total Paid", res.totalPaid.toCurrencyLabel())
                    TotalCell("Total Outstanding", res.totalOutstanding.toCurrencyLabel())
                    TotalCell("Total Amount", res.totalAmount.toCurrencyLabel())
                }
            }

            // Table
            if (res.rows.isEmpty()) {
                Text(
                    "Tidak ada faktur yang sesuai kriteria.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    val hScrollState = rememberScrollState()
                    val vScrollState = rememberScrollState()

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 600.dp)
                            .horizontalScroll(hScrollState)
                    ) {
                        Column(
                            modifier = Modifier
                                .width(TABLE_TOTAL_WIDTH)
                                .verticalScroll(vScrollState)
                        ) {
                            // Header
                            BukuBesarTableHeader()
                            HorizontalDivider(thickness = 1.5.dp)

                            // Rows
                            res.rows.forEach { row ->
                                BukuBesarTableRow(row)
                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                )
                                // Sub Total row per invoice
                                SubTotalRow(row)
                                HorizontalDivider(
                                    thickness = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Empty state
        if (result == null && !loading) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Belum ada data ditampilkan.", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Atur kriteria di atas lalu klik Submit untuk menampilkan laporan.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // ── Customer / Supplier List Popup ────────────────────────────────────────
    if (showCustomerList) {
        AlertDialog(
            onDismissRequest = { showCustomerList = false },
            title = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("List of Customer / Supplier")
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = customerListType == "Customer",
                            onClick = { customerListType = "Customer" }
                        )
                        Text("CUSTOMER", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(16.dp))
                        RadioButton(
                            selected = customerListType == "Supplier",
                            onClick = { customerListType = "Supplier" }
                        )
                        Text("SUPPLIER", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Table header
                    Surface(color = MaterialTheme.colorScheme.primaryContainer) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Nr.", modifier = Modifier.width(32.dp), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                            Text("CustomerID", modifier = Modifier.width(100.dp), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                            Text("Name", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                            Text("Status", modifier = Modifier.width(50.dp), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    HorizontalDivider()

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        customerListData.forEachIndexed { index, customer ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (index % 2 == 0) MaterialTheme.colorScheme.surface
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    )
                                    .clickable {
                                        customerQuery = customer.code
                                        showCustomerList = false
                                    }
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${index + 1}", modifier = Modifier.width(32.dp), style = MaterialTheme.typography.bodySmall)
                                Text(customer.code, modifier = Modifier.width(100.dp), style = MaterialTheme.typography.bodySmall)
                                Text(customer.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                                Text(
                                    if (customer.isActive) "Good" else "Bad",
                                    modifier = Modifier.width(50.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (customer.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                            }
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCustomerList = false }) {
                    Text("Tutup")
                }
            }
        )
    }
}

// ─── Table helpers ─────────────────────────────────────────────────────────────

private val COL_CUSTOMERID = 95.dp
private val COL_NAME = 155.dp
private val COL_FAKTUR_NR = 145.dp
private val COL_DATE = 90.dp
private val COL_STATUS = 90.dp
private val COL_DESC = 130.dp
private val COL_PAID_DATE = 90.dp
private val COL_PAID = 110.dp
private val COL_OUTSTANDING = 110.dp
private val COL_TOTAL = 110.dp
private val COL_PAYMENT_TYPE = 110.dp
private val COL_MARGIN = 100.dp

private val TABLE_TOTAL_WIDTH =
    COL_CUSTOMERID + COL_NAME + COL_FAKTUR_NR + COL_DATE + COL_STATUS +
    COL_DESC + COL_PAID_DATE + COL_PAID + COL_OUTSTANDING +
    COL_TOTAL + COL_PAYMENT_TYPE + COL_MARGIN + 24.dp  // padding

@Composable
private fun BukuBesarTableHeader() {
    Surface(color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeaderCell("CUSTOMERID", COL_CUSTOMERID)
            HeaderCell("NAME / COMPANY", COL_NAME)
            HeaderCell("FAKTUR NR.", COL_FAKTUR_NR)
            HeaderCell("FAKTUR DATE", COL_DATE)
            HeaderCell("STATUS", COL_STATUS)
            HeaderCell("DESCRIPTION", COL_DESC)
            HeaderCell("PAID DATE", COL_PAID_DATE)
            HeaderCell("PAID", COL_PAID)
            HeaderCell("OUTSTANDING", COL_OUTSTANDING)
            HeaderCell("TOTAL", COL_TOTAL)
            HeaderCell("PAYMENT TYPE", COL_PAYMENT_TYPE)
            HeaderCell("MARGIN", COL_MARGIN)
        }
    }
}

@Composable
private fun HeaderCell(text: String, width: androidx.compose.ui.unit.Dp) {
    Text(
        text,
        modifier = Modifier.width(width),
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.labelSmall,
        fontSize = 11.sp
    )
}

@Composable
private fun BukuBesarTableRow(row: BukuBesarRow) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DataCell(row.customerCode, COL_CUSTOMERID, bold = true)
        DataCell(row.customerName, COL_NAME)
        DataCell(row.fakturNr, COL_FAKTUR_NR)
        DataCell(row.fakturDate.format(DATE_FMT), COL_DATE)
        DataCell(
            row.agingStatus,
            COL_STATUS,
            color = when (row.agingStatus) {
                "Closed" -> MaterialTheme.colorScheme.primary
                "New" -> MaterialTheme.colorScheme.secondary
                "Outstanding" -> MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                "Unpaid" -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
        DataCell(row.description ?: "", COL_DESC)
        DataCell(row.paidDate?.format(DATE_FMT) ?: "-", COL_PAID_DATE)
        DataCell(row.paid.toCurrencyLabel(), COL_PAID)
        DataCell(row.outstanding.toCurrencyLabel(), COL_OUTSTANDING)
        DataCell(row.total.toCurrencyLabel(), COL_TOTAL)
        DataCell(row.paymentType ?: "-", COL_PAYMENT_TYPE)
        DataCell(row.margin.toCurrencyLabel(), COL_MARGIN)
    }
}

@Composable
private fun SubTotalRow(row: BukuBesarRow) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Sub Total",
            modifier = Modifier.width(COL_CUSTOMERID + COL_NAME + COL_FAKTUR_NR + COL_DATE + COL_STATUS + COL_DESC + COL_PAID_DATE + 16.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        DataCell(row.paid.toCurrencyLabel(), COL_PAID, bold = true)
        DataCell(row.outstanding.toCurrencyLabel(), COL_OUTSTANDING, bold = true)
        DataCell(row.total.toCurrencyLabel(), COL_TOTAL, bold = true)
        DataCell("", COL_PAYMENT_TYPE)
        DataCell(row.margin.toCurrencyLabel(), COL_MARGIN, bold = true)
    }
}

@Composable
private fun DataCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    bold: Boolean = false,
    color: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Unspecified
) {
    Text(
        text,
        modifier = Modifier.width(width),
        style = MaterialTheme.typography.bodySmall,
        fontSize = 11.sp,
        fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal,
        color = color,
        maxLines = 1,
        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
    )
}

@Composable
private fun TotalCell(label: String, value: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}
