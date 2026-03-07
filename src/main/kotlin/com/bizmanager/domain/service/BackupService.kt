package com.bizmanager.domain.service

import com.bizmanager.data.database.DatabaseConfig
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object BackupService {

    fun backupDatabase(destinationFolder: String): File {
        val destDir = File(destinationFolder)
        if (!destDir.exists()) {
            destDir.mkdirs()
        }

        val activeDb = DatabaseConfig.dbFile
        if (!activeDb.exists()) throw IllegalStateException("Active database not found to backup.")

        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val backupFile = File(destDir, "BizManager_Backup_$timestamp.db")

        Files.copy(activeDb.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        return backupFile
    }

    fun restoreDatabase(backupFileToRestore: File) {
        if (!backupFileToRestore.exists()) throw IllegalArgumentException("Wile backup tidak ditemukan.")

        val activeDb = DatabaseConfig.dbFile
        
        // Safety Auto-Backup before overwrite
        if (activeDb.exists()) {
            val autoBackupFolder = File(DatabaseConfig.dbFolder, "AutoBackups")
            if (!autoBackupFolder.exists()) autoBackupFolder.mkdirs()
            
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            val safetyBackup = File(autoBackupFolder, "PreRestore_Safeguard_$timestamp.db")
            Files.copy(activeDb.toPath(), safetyBackup.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }

        // Overwrite active DB
        Files.copy(backupFileToRestore.toPath(), activeDb.toPath(), StandardCopyOption.REPLACE_EXISTING)
    }
}
