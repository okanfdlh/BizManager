package com.bizmanager.presentation.screen.customer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bizmanager.data.repository.CustomerRepository
import com.bizmanager.domain.model.Customer

@Composable
fun CustomerListScreen(
    customerRepository: CustomerRepository,
    onNavigateToForm: (Int?) -> Unit,
    onNavigateToDetail: (Int) -> Unit
) {
    var customers by remember { mutableStateOf(emptyList<Customer>()) }

    LaunchedEffect(Unit) {
        customers = customerRepository.findAll(includeInactive = false)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Daftar Customer", style = MaterialTheme.typography.h4)
            Button(onClick = { onNavigateToForm(null) }) {
                Icon(Icons.Default.Add, contentDescription = "Tambah")
                Spacer(Modifier.width(8.dp))
                Text("Tambah Customer")
            }
        }
        
        Spacer(Modifier.height(16.dp))

        // Simple table header
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Text("Kode", modifier = Modifier.weight(1f), style = MaterialTheme.typography.subtitle2)
            Text("Nama", modifier = Modifier.weight(2f), style = MaterialTheme.typography.subtitle2)
            Text("Telepon", modifier = Modifier.weight(1f), style = MaterialTheme.typography.subtitle2)
            Text("Status", modifier = Modifier.width(100.dp), style = MaterialTheme.typography.subtitle2)
            Text("Aksi", modifier = Modifier.width(150.dp), style = MaterialTheme.typography.subtitle2)
        }
        Divider()

        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
            items(customers) { c ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigateToDetail(c.id) }.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(c.code, modifier = Modifier.weight(1f))
                    Text(c.name, modifier = Modifier.weight(2f))
                    Text(c.phone ?: "-", modifier = Modifier.weight(1f))
                    Text(if (c.isActive) "Aktif" else "Nonaktif", color = if (c.isActive) MaterialTheme.colors.primary else MaterialTheme.colors.error, modifier = Modifier.width(100.dp))
                    
                    Row(modifier = Modifier.width(150.dp)) {
                        TextButton(onClick = { onNavigateToForm(c.id) }) { Text("Edit") }
                    }
                }
                Divider()
            }
        }
    }
}
