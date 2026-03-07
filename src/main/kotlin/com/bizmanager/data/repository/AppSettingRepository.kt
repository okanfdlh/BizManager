package com.bizmanager.data.repository

import com.bizmanager.data.database.AppSettings
import com.bizmanager.domain.model.AppSetting
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class AppSettingRepository {

    fun getSettings(): AppSetting = transaction {
        val row = AppSettings.selectAll().firstOrNull()
        if (row != null) {
            mapToAppSetting(row)
        } else {
            // Create default settings if not exists
            val defaultSetting = AppSetting()
            val insertStatement = AppSettings.insert {
                it[invoicePrefix] = defaultSetting.invoicePrefix
                it[paymentPrefix] = defaultSetting.paymentPrefix
                it[defaultDueDays] = defaultSetting.defaultDueDays
                it[currency] = defaultSetting.currency
                it[createdAt] = LocalDateTime.now()
                it[updatedAt] = LocalDateTime.now()
            }
            defaultSetting.copy(id = insertStatement[AppSettings.id].value)
        }
    }

    fun updateSettings(setting: AppSetting) = transaction {
        val exist = AppSettings.selectAll().count() > 0
        if (exist) {
            AppSettings.update({ AppSettings.id eq setting.id }) {
                it[companyName] = setting.companyName
                it[companyAddress] = setting.companyAddress
                it[phone] = setting.phone
                it[email] = setting.email
                it[logoPath] = setting.logoPath
                it[invoicePrefix] = setting.invoicePrefix
                it[paymentPrefix] = setting.paymentPrefix
                it[defaultDueDays] = setting.defaultDueDays
                it[currency] = setting.currency
                it[backupFolder] = setting.backupFolder
                it[updatedAt] = LocalDateTime.now()
            }
        } else {
            AppSettings.insert {
                it[companyName] = setting.companyName
                it[companyAddress] = setting.companyAddress
                it[phone] = setting.phone
                it[email] = setting.email
                it[logoPath] = setting.logoPath
                it[invoicePrefix] = setting.invoicePrefix
                it[paymentPrefix] = setting.paymentPrefix
                it[defaultDueDays] = setting.defaultDueDays
                it[currency] = setting.currency
                it[backupFolder] = setting.backupFolder
                it[createdAt] = LocalDateTime.now()
                it[updatedAt] = LocalDateTime.now()
            }
        }
    }

    private fun mapToAppSetting(row: ResultRow): AppSetting = AppSetting(
        id = row[AppSettings.id].value,
        companyName = row[AppSettings.companyName],
        companyAddress = row[AppSettings.companyAddress],
        phone = row[AppSettings.phone],
        email = row[AppSettings.email],
        logoPath = row[AppSettings.logoPath],
        invoicePrefix = row[AppSettings.invoicePrefix],
        paymentPrefix = row[AppSettings.paymentPrefix],
        defaultDueDays = row[AppSettings.defaultDueDays],
        currency = row[AppSettings.currency],
        backupFolder = row[AppSettings.backupFolder],
        createdAt = row[AppSettings.createdAt],
        updatedAt = row[AppSettings.updatedAt]
    )
}
