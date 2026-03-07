package com.bizmanager.domain.service

import com.bizmanager.data.database.Invoices
import com.bizmanager.data.database.InvoiceItems
import com.bizmanager.data.database.Payments
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Service to aggregate data for the Reports module, using history/snapshot transactional data
 * as mandated in the blueprint.
 */
class ReportService {

    fun getSalesSummary(startDate: LocalDateTime, endDate: LocalDateTime): SalesSummary = transaction {
        val invoices = Invoices.select { 
            (Invoices.createdAt greaterEq startDate) and (Invoices.createdAt lessEq endDate) and (Invoices.invoiceStatus neq "Cancelled") 
        }
        
        var totalOmzet = BigDecimal.ZERO
        var totalGrossProfit = BigDecimal.ZERO
        var totalNetProfit = BigDecimal.ZERO
        var totalReceivables = BigDecimal.ZERO
        
        invoices.forEach { 
            totalOmzet = totalOmzet.add(it[Invoices.grandTotal])
            totalGrossProfit = totalGrossProfit.add(it[Invoices.grossProfit])
            totalNetProfit = totalNetProfit.add(it[Invoices.netProfit])
            totalReceivables = totalReceivables.add(it[Invoices.balanceDue])
        }

        val payments = Payments.select {
            (Payments.createdAt greaterEq startDate) and (Payments.createdAt lessEq endDate)
        }
        
        var totalPaidInPeriod = BigDecimal.ZERO
        payments.forEach {
            totalPaidInPeriod = totalPaidInPeriod.add(it[Payments.amount])
        }

        SalesSummary(
            totalOmzet = totalOmzet,
            totalGrossProfit = totalGrossProfit,
            totalNetProfit = totalNetProfit,
            totalReceivables = totalReceivables,
            totalPaymentsReceived = totalPaidInPeriod
        )
    }

    // Additional aggregated reports for customers and products can be added here
}

data class SalesSummary(
    val totalOmzet: BigDecimal,
    val totalGrossProfit: BigDecimal,
    val totalNetProfit: BigDecimal,
    val totalReceivables: BigDecimal,
    val totalPaymentsReceived: BigDecimal
)
