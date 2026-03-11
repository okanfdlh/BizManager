package com.bizmanager.presentation.screen.payment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bizmanager.data.repository.InvoiceRepository
import com.bizmanager.data.repository.PaymentRepository
import com.bizmanager.domain.model.Payment

@Composable
fun PaymentListScreen(
    paymentRepository: PaymentRepository,
    invoiceRepository: InvoiceRepository
) {
    var payments by remember { mutableStateOf(emptyList<Payment>()) }
    var invoiceMap by remember { mutableStateOf(emptyMap<Int, String>()) }

    LaunchedEffect(Unit) {
        val payData = paymentRepository.findAll()
        val invData = invoiceRepository.findAll().associateBy({ it.id }, { it.invoiceNumber })
        payments = payData
        invoiceMap = invData
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Daftar Pembayaran Masuk", style = MaterialTheme.typography.h4)
        }
        Spacer(Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Text("No. Pembayaran", modifier = Modifier.weight(1f), style = MaterialTheme.typography.subtitle2)
            Text("No. Invoice", modifier = Modifier.weight(1f), style = MaterialTheme.typography.subtitle2)
            Text("Tanggal", modifier = Modifier.weight(1f), style = MaterialTheme.typography.subtitle2)
            Text("Metode", modifier = Modifier.weight(1f), style = MaterialTheme.typography.subtitle2)
            Text("Nominal", modifier = Modifier.weight(1f), style = MaterialTheme.typography.subtitle2)
        }
        Divider()

        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
            items(payments) { p ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(p.paymentNumber, modifier = Modifier.weight(1f))
                    Text(invoiceMap[p.invoiceId] ?: "Unknown", modifier = Modifier.weight(1f))
                    Text(p.date.toLocalDate().toString(), modifier = Modifier.weight(1f))
                    Text(p.paymentMethod, modifier = Modifier.weight(1f))
                    Text("Rp ${p.amount.toPlainString()}", modifier = Modifier.weight(1f))
                }
                Divider()
            }
        }
    }
}
