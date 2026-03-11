package com.bizmanager.presentation.screen.settings

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bizmanager.data.repository.AppSettingRepository
import com.bizmanager.domain.model.AppSetting

@Composable
fun SettingsScreen(
    appSettingRepository: AppSettingRepository
) {
    var setting by remember { mutableStateOf<AppSetting?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    
    // Form fields
    var companyName by remember { mutableStateOf("") }
    var companyAddress by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var invoicePrefix by remember { mutableStateOf("") }
    var paymentPrefix by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("") }
    var backupFolder by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val s = appSettingRepository.getSettings()
        setting = s
        companyName = s.companyName ?: ""
        companyAddress = s.companyAddress ?: ""
        phone = s.phone ?: ""
        email = s.email ?: ""
        invoicePrefix = s.invoicePrefix
        paymentPrefix = s.paymentPrefix
        currency = s.currency
        backupFolder = s.backupFolder ?: ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Pengaturan Aplikasi", style = MaterialTheme.typography.h4)
        Spacer(Modifier.height(16.dp))

        if (successMessage != null) {
            Text(successMessage!!, color = MaterialTheme.colors.primary)
            Spacer(Modifier.height(8.dp))
        }

        Row {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text("Profil Perusahaan", style = MaterialTheme.typography.h6)
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                OutlinedTextField(value = companyName, onValueChange = { companyName = it }, label = { Text("Nama Perusahaan") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = companyAddress, onValueChange = { companyAddress = it }, label = { Text("Alamat") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Telepon") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
            }
            Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                Text("Preferensi Sistem", style = MaterialTheme.typography.h6)
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                OutlinedTextField(value = invoicePrefix, onValueChange = { invoicePrefix = it }, label = { Text("Prefix Invoice (Mis: INV)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = paymentPrefix, onValueChange = { paymentPrefix = it }, label = { Text("Prefix Pembayaran (Mis: PAY)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = currency, onValueChange = { currency = it }, label = { Text("Mata Uang (Mis: Rp.)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = backupFolder, onValueChange = { backupFolder = it }, label = { Text("Folder Backup Default") }, modifier = Modifier.fillMaxWidth())
            }
        }
        
        Spacer(Modifier.height(32.dp))
        Button(
            modifier = Modifier.fillMaxWidth().height(50.dp),
            onClick = {
                setting?.let { s ->
                    val updated = s.copy(
                        companyName = companyName,
                        companyAddress = companyAddress,
                        phone = phone,
                        email = email,
                        invoicePrefix = invoicePrefix.ifBlank { "INV" },
                        paymentPrefix = paymentPrefix.ifBlank { "PAY" },
                        currency = currency.ifBlank { "Rp." },
                        backupFolder = backupFolder
                    )
                    appSettingRepository.updateSettings(updated)
                    successMessage = "Pengaturan berhasil disimpan."
                }
            }
        ) {
            Text("Simpan Pengaturan")
        }
    }
}
