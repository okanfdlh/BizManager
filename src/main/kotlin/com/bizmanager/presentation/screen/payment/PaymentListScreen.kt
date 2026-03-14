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
import com.bizmanager.presentation.ui.PaginationControl
import com.bizmanager.presentation.ui.paginateList
import com.bizmanager.presentation.ui.toCurrencyLabel
import kotlin.math.ceil

@Composable
fun PaymentListScreen(
    paymentRepository: PaymentRepository,
    invoiceRepository: InvoiceRepository
) {
    var payments by remember { mutableStateOf(emptyList<Payment>()) }
    var invoiceMap by remember { mutableStateOf(emptyMap<Int, String>()) }

    // Filter & Pagination States
    var searchQuery by remember { mutableStateOf("") }
    var paymentMethodFilter by remember { mutableStateOf("Semua") } // "Semua", "Transfer", "Cash" dsb.
    
    var currentPage by remember { mutableStateOf(1) }
    var pageSize by remember { mutableStateOf(20) }

    val paymentMethods = remember(payments) {
        listOf("Semua") + payments.map { it.paymentMethod }.distinct().sorted()
    }

    LaunchedEffect(Unit) {
        val payData = paymentRepository.findAll()
        val invData = invoiceRepository.findAll().associateBy({ it.id }, { it.invoiceNumber })
        payments = payData
        invoiceMap = invData
    }

    // Advanced in-memory filtering
    val filteredPayments by remember(payments, invoiceMap, searchQuery, paymentMethodFilter) {
        derivedStateOf {
            payments.filter { p ->
                val invoiceNumber = invoiceMap[p.invoiceId] ?: ""
                val matchesSearch = p.paymentNumber.contains(searchQuery, ignoreCase = true) ||
                        invoiceNumber.contains(searchQuery, ignoreCase = true) ||
                        (p.reference?.contains(searchQuery, ignoreCase = true) == true)
                
                val matchesMethod = when (paymentMethodFilter) {
                    "Semua" -> true
                    else -> p.paymentMethod == paymentMethodFilter
                }
                
                matchesSearch && matchesMethod
            }
        }
    }

    // Pagination Logic
    val totalElements = filteredPayments.size
    val totalPages = if (totalElements == 0) 1 else ceil(totalElements.toDouble() / pageSize).toInt()
    
    // Auto-adjust page if out of bounds after filtering
    LaunchedEffect(totalPages) {
        if (currentPage > totalPages) currentPage = totalPages
        if (currentPage < 1) currentPage = 1
    }

    val paginatedPayments by remember(filteredPayments, currentPage, pageSize) {
        derivedStateOf {
            paginateList(filteredPayments, currentPage, pageSize)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Daftar Pembayaran Masuk", style = MaterialTheme.typography.h4)
        }
        Spacer(Modifier.height(16.dp))

        // Filter Controls
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it; currentPage = 1 },
                label = { Text("Cari (No. Pembayaran, No. Faktur, Referensi)") },
                modifier = Modifier.weight(1f)
            )
            
            var expanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.width(200.dp)) {
                OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                    Text("Metode: $paymentMethodFilter")
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    paymentMethods.forEach { opt ->
                        DropdownMenuItem(onClick = {
                            paymentMethodFilter = opt
                            currentPage = 1
                            expanded = false
                        }) {
                            Text(opt)
                        }
                    }
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Text("No. Pembayaran", modifier = Modifier.weight(1f), style = MaterialTheme.typography.subtitle2)
            Text("No. Faktur", modifier = Modifier.weight(1f), style = MaterialTheme.typography.subtitle2)
            Text("Tanggal", modifier = Modifier.weight(1f), style = MaterialTheme.typography.subtitle2)
            Text("Metode", modifier = Modifier.weight(1f), style = MaterialTheme.typography.subtitle2)
            Text("Nominal", modifier = Modifier.weight(1f), style = MaterialTheme.typography.subtitle2)
        }
        Divider()

        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
            items(paginatedPayments) { p ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(p.paymentNumber, modifier = Modifier.weight(1f))
                    Text(invoiceMap[p.invoiceId] ?: "Unknown", modifier = Modifier.weight(1f))
                    Text(p.date.toLocalDate().toString(), modifier = Modifier.weight(1f))
                    Text(p.paymentMethod, modifier = Modifier.weight(1f))
                    Text(p.amount.toCurrencyLabel(), modifier = Modifier.weight(1f))
                }
                Divider()
            }
        }
        
        Divider()
        PaginationControl(
            currentPage = currentPage,
            totalPages = totalPages,
            pageSize = pageSize,
            totalElements = totalElements,
            onPageChanged = { currentPage = it },
            onPageSizeChanged = { pageSize = it; currentPage = 1 }
        )
    }
}
