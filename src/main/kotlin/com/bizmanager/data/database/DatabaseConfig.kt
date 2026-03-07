package com.bizmanager.data.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.statements.StatementType
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

object DatabaseConfig {
    private const val DB_NAME = "app.db"

    private fun resolveWindowsDataDir(userHome: String): File {
        val localAppData = System.getenv("LOCALAPPDATA")
            ?: System.getenv("LocalAppData")
        return if (!localAppData.isNullOrBlank()) {
            File(localAppData, "BizManager")
        } else {
            File(userHome, "AppData/Local/BizManager")
        }
    }
    
    // Determine the location based on OS for "%LocalAppData%/<AppName>/app.db" equivalent
    val dbFolder: File by lazy {
        val os = System.getProperty("os.name").lowercase()
        val userHome = System.getProperty("user.home")
        
        val folder = when {
            os.contains("win") -> resolveWindowsDataDir(userHome)
            os.contains("mac") -> File(userHome, "Library/Application Support/BizManager")
            else -> File(userHome, ".config/BizManager")
        }
        
        if (!folder.exists() && !folder.mkdirs()) {
            error("Failed to create database folder: ${folder.absolutePath}")
        }
        folder
    }

    val dbFile: File by lazy {
        File(dbFolder, DB_NAME)
    }

    fun init() {
        // jdbc:sqlite:/path/to/db/app.db
        val url = "jdbc:sqlite:${dbFile.absolutePath}"
        
        Database.connect(
            url = url,
            driver = "org.sqlite.JDBC"
        )

        // Foreign keys must be enabled manually in SQLite per connection if needed.
        // Exposed tries to handle this when dialect is sqlite, but we might want to ensure it via PRAGMA foreign_keys = ON;
        transaction {
            exec("PRAGMA foreign_keys = ON;", explicitStatementType = StatementType.EXEC)
            
            // We will create tables here later
            SchemaUtils.createMissingTablesAndColumns(
                Customers,
                Products,
                Invoices,
                InvoiceItems,
                Payments,
                AppSettings,
                ActivityLogs
            )
        }
        
        println("Database initialized at: ${dbFile.absolutePath}")
    }
}
