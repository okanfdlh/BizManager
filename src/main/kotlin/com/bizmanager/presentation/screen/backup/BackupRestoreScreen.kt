package com.bizmanager.presentation.screen.backup

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bizmanager.data.repository.AppSettingRepository
import com.bizmanager.domain.service.BackupService
import java.io.File

@Composable
fun BackupRestoreScreen(
    appSettingRepository: AppSettingRepository
) {
    var backupFolder by remember { mutableStateOf("") }
    var restoreFilePath by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        backupFolder = appSettingRepository.getSettings().backupFolder ?: System.getProperty("user.home") + "/Desktop"
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Backup & Restore Database", style = MaterialTheme.typography.h4)
        Spacer(Modifier.height(8.dp))
        Text("Modul ini menangani pencadangan dan pemulihan database offline Anda.", style = MaterialTheme.typography.body1)
        Spacer(Modifier.height(16.dp))

        if (message != null) {
            Text(message!!, color = if(isError) MaterialTheme.colors.error else MaterialTheme.colors.primary)
            Spacer(Modifier.height(8.dp))
        }

        Card(modifier = Modifier.fillMaxWidth(), elevation = 4.dp) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Backup Database", style = MaterialTheme.typography.h6)
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                OutlinedTextField(
                    value = backupFolder,
                    onValueChange = { backupFolder = it },
                    label = { Text("Folder Tujuan Backup (Path)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        try {
                            val f = BackupService.backupDatabase(backupFolder)
                            isError = false
                            message = "Backup berhasil: ${f.absolutePath}"
                        } catch (e: Exception) {
                            isError = true
                            message = "Backup gagal: ${e.message}"
                        }
                    }
                ) {
                    Text("Proses Backup Sekarang")
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth(), elevation = 4.dp) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Restore Database", style = MaterialTheme.typography.h6, color = MaterialTheme.colors.error)
                Text("PERINGATAN: Me-restore database akan menimpa data saat ini. Sistem akan melakukan auto-backup secara otomatis sebelum me-replace file asli.", style = MaterialTheme.typography.caption)
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                OutlinedTextField(
                    value = restoreFilePath,
                    onValueChange = { restoreFilePath = it },
                    label = { Text("Path File Backup (.db) lengkap") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error),
                    onClick = {
                        try {
                            val file = File(restoreFilePath)
                            BackupService.restoreDatabase(file)
                            isError = false
                            message = "Restore berhasil dari ${file.name}. Silakan restart aplikasi bila diperlukan."
                        } catch (e: Exception) {
                            isError = true
                            message = "Restore gagal: ${e.message}"
                        }
                    }
                ) {
                    Text("Jalankan Restore Data")
                }
            }
        }
    }
}
