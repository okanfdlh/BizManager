package com.bizmanager.presentation.screen.invoice

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bizmanager.data.repository.CustomerRepository
import com.bizmanager.data.repository.InvoiceRepository
import com.bizmanager.data.repository.PaymentRepository
import com.bizmanager.domain.model.*
import com.bizmanager.domain.service.InvoiceService
import com.bizmanager.presentation.ui.toCurrencyLabel
import com.bizmanager.presentation.ui.toRupiahWordsLabel
import java.math.BigDecimal

@Composable
fun InvoiceDetailScreen(
    invoiceId: Int,
    invoiceRepository: InvoiceRepository,
    customerRepository: CustomerRepository,
    paymentRepository: PaymentRepository,
    invoiceService: InvoiceService,
    onNavigateToPaymentForm: (Int) -> Unit,
    onBack: () -> Unit
) {
    var invoice by remember { mutableStateOf<Invoice?>(null) }
    var customer by remember { mutableStateOf<Customer?>(null) }
    var items by remember { mutableStateOf(emptyList<InvoiceItem>()) }
    var payments by remember { mutableStateOf(emptyList<Payment>()) }

    var refreshTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(invoiceId, refreshTrigger) {
        val inv = invoiceRepository.findById(invoiceId)
        invoice = inv
        if (inv != null) {
            customer = customerRepository.findById(inv.customerId)
            items = invoiceRepository.getItemsForInvoice(inv.id)
            payments = paymentRepository.findByInvoiceId(inv.id)
        }
    }

    val inv = invoice ?: return

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Detail Invoice: ${inv.invoiceNumber}", style = MaterialTheme.typography.h4)
            Row {
                Button(onClick = onBack) { Text("Kembali") }
                Spacer(Modifier.width(8.dp))
                if (inv.invoiceStatus != InvoiceStatus.Cancelled && inv.paymentStatus != PaymentStatus.Paid) {
                    Button(onClick = { onNavigateToPaymentForm(inv.id) }) {
                        Text("Terima Pembayaran")
                    }
                }
                Spacer(Modifier.width(8.dp))
                if (inv.invoiceStatus != InvoiceStatus.Cancelled) {
                    OutlinedButton(
                        onClick = { 
                            invoiceService.cancelInvoice(inv.id) 
                            refreshTrigger++
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colors.error)
                    ) {
                        Text("Cancel Invoice")
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Kepada:", style = MaterialTheme.typography.subtitle2)
                        Text(customer?.name ?: "Unknown")
                        Text(customer?.company ?: "")
                        Text("Jatuh Tempo: ${inv.dueDate.toLocalDate()}")
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Status Dokumen: ${inv.invoiceStatus.name}", style = MaterialTheme.typography.subtitle2, color = if(inv.invoiceStatus == InvoiceStatus.Posted) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface)
                        Text("Status Bayar: ${inv.paymentStatus.name}", style = MaterialTheme.typography.subtitle2)
                        Spacer(Modifier.height(8.dp))
                        Text("CATATAN:", style = MaterialTheme.typography.subtitle2)
                        Text(inv.notes ?: "-")
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Daftar Item", style = MaterialTheme.typography.h6)
        Divider()
        
        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
            items(items) { itm ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.weight(2f)) {
                        Text(itm.productNameSnapshot, style = MaterialTheme.typography.subtitle1)
                        Text("${itm.qty} x ${itm.sellPrice.toCurrencyLabel()} (Diskon: ${itm.discount.toCurrencyLabel()})", style = MaterialTheme.typography.caption)
                    }
                    Text(itm.subtotal.subtract(itm.discount).toCurrencyLabel(), modifier = Modifier.weight(1f), style = MaterialTheme.typography.subtitle1, textAlign = androidx.compose.ui.text.style.TextAlign.End)
                }
                Divider()
            }
            
            item {
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Column(modifier = Modifier.width(300.dp)) {
                        SummaryRow("Subtotal", inv.subtotal)
                        SummaryRow("Total Diskon", inv.totalDiscount, true)
                        SummaryRow("Grand Total", inv.grandTotal, bold = true)
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                        SummaryRow("Total Dibayar", inv.totalPaid)
                        SummaryRow("Sisa Tagihan", inv.balanceDue, bold = true, color = if (inv.balanceDue > BigDecimal.ZERO) MaterialTheme.colors.error else MaterialTheme.colors.primary)
                        Spacer(Modifier.height(8.dp))
                        Text("Terbilang: ${inv.grandTotal.toRupiahWordsLabel()}", style = MaterialTheme.typography.caption)
                    }
                }
            }

            if (payments.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(24.dp))
                    Text("Histori Pembayaran", style = MaterialTheme.typography.h6)
                    Divider()
                }
                items(payments) { pay ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(pay.date.toLocalDate().toString())
                        Text(pay.paymentMethod)
                        Text(pay.reference ?: "-")
                        Text(pay.amount.toCurrencyLabel(), style = MaterialTheme.typography.subtitle2)
                    }
                    Divider()
                }
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, amount: BigDecimal, isNegative: Boolean = false, bold: Boolean = false, color: androidx.compose.ui.graphics.Color = MaterialTheme.colors.onSurface) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = if(bold) MaterialTheme.typography.subtitle1 else MaterialTheme.typography.body2)
        val displayAmount = if (isNegative) amount.negate() else amount
        Text(displayAmount.toCurrencyLabel(), style = if(bold) MaterialTheme.typography.subtitle1 else MaterialTheme.typography.body2, color = color)
    }
}
