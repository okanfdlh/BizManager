package com.bizmanager.presentation.screen.customer

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bizmanager.data.repository.CustomerRepository
import com.bizmanager.domain.model.Customer
import java.time.LocalDateTime

@Composable
fun CustomerFormScreen(
    customerId: Int?,
    customerRepository: CustomerRepository,
    onBack: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(true) }
    
    var existingCustomer by remember { mutableStateOf<Customer?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(customerId) {
        if (customerId != null) {
            existingCustomer = customerRepository.findById(customerId)
            existingCustomer?.let {
                code = it.code
                name = it.name
                company = it.company ?: ""
                phone = it.phone ?: ""
                email = it.email ?: ""
                address = it.address ?: ""
                notes = it.notes ?: ""
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
            Text(if (customerId == null) "Tambah Customer Baru" else "Edit Customer", style = MaterialTheme.typography.h4)
            Button(onClick = onBack) { Text("Kembali") }
        }
        Spacer(Modifier.height(16.dp))

        if (errorMessage != null) {
            Text(errorMessage!!, color = MaterialTheme.colors.error)
            Spacer(Modifier.height(8.dp))
        }

        OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Kode *") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama *") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = company, onValueChange = { company = it }, label = { Text("Perusahaan") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Telepon") }, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        
        OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Alamat") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Catatan") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
        Spacer(Modifier.height(8.dp))
        
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Checkbox(checked = isActive, onCheckedChange = { isActive = it })
            Text("Customer Aktif")
        }
        
        Spacer(Modifier.height(24.dp))

        Button(
            modifier = Modifier.fillMaxWidth().height(50.dp),
            onClick = {
                if (code.isBlank() || name.isBlank()) {
                    errorMessage = "Kode dan Nama wajib diisi."
                    return@Button
                }
                try {
                    val customer = Customer(
                        id = customerId ?: 0,
                        code = code,
                        name = name,
                        company = company.ifBlank { null },
                        phone = phone.ifBlank { null },
                        email = email.ifBlank { null },
                        address = address.ifBlank { null },
                        notes = notes.ifBlank { null },
                        isActive = isActive,
                        createdAt = existingCustomer?.createdAt ?: LocalDateTime.now(),
                        updatedAt = LocalDateTime.now()
                    )

                    if (customerId == null) {
                        customerRepository.insert(customer)
                    } else {
                        customerRepository.update(customer)
                    }
                    onBack()
                } catch (e: Exception) {
                    errorMessage = "Gagal menyimpan: ${e.message}"
                }
            }
        ) {
            Text("Simpan")
        }
    }
}
