package com.bizmanager.domain.service

import com.bizmanager.data.repository.AppSettingRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicInteger

/**
 * Generates document numbers like INV/2026/03/0001
 * In a real multi-user system we'd lock the DB row, but for a local desktop single-user app, 
 * this simple sequential generation reading from DB or an atomic counter is sufficient.
 */
class DocumentNumberGenerator(
    private val appSettingRepository: AppSettingRepository
) {
    // We would normally ping the database to find the last ID for the month and increment it.
    // We will simulate that logic here by using a timestamp component.
    
    fun generateInvoiceNumber(lastDailySequence: Int): String {
        val settings = appSettingRepository.getSettings()
        val prefix = settings.invoicePrefix
        val today = LocalDate.now()
        val year = today.format(DateTimeFormatter.ofPattern("yyyy"))
        val month = today.format(DateTimeFormatter.ofPattern("MM"))
        val seq = String.format("%04d", lastDailySequence + 1)
        
        return "$prefix/$year/$month/$seq"
    }

    fun generatePaymentNumber(lastDailySequence: Int): String {
        val settings = appSettingRepository.getSettings()
        val prefix = settings.paymentPrefix
        val today = LocalDate.now()
        val year = today.format(DateTimeFormatter.ofPattern("yyyy"))
        val month = today.format(DateTimeFormatter.ofPattern("MM"))
        val seq = String.format("%04d", lastDailySequence + 1)
        
        return "$prefix/$year/$month/$seq"
    }
}
