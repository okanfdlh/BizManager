package com.bizmanager.domain.service

import com.bizmanager.data.repository.CustomerRepository
import com.bizmanager.data.repository.InvoiceRepository
import com.bizmanager.data.repository.PaymentRepository
import com.bizmanager.domain.model.Customer
import com.bizmanager.domain.model.Invoice
import com.bizmanager.domain.model.InvoiceItem
import com.bizmanager.domain.model.Payment
import java.math.BigDecimal

class CustomerLedgerService(
    private val customerRepository: CustomerRepository,
    private val invoiceRepository: InvoiceRepository,
    private val paymentRepository: PaymentRepository
) {

    fun getCustomerLedger(customerId: Int): CustomerLedgerReport? {
        val customer = customerRepository.findById(customerId) ?: return null
        val invoices = invoiceRepository.findByCustomerId(customerId)

        val ledgerInvoices = invoices.map { invoice ->
            val items = invoiceRepository.getItemsForInvoice(invoice.id)
            val payments = paymentRepository.findByInvoiceId(invoice.id).sortedByDescending { it.date }
            val totalPaid = payments.fold(BigDecimal.ZERO) { acc, payment -> acc.add(payment.amount) }

            CustomerLedgerInvoice(
                invoice = invoice,
                items = items,
                payments = payments,
                totalPaid = totalPaid,
                outstanding = invoice.balanceDue,
                isSettled = invoice.balanceDue == BigDecimal.ZERO
            )
        }

        return CustomerLedgerReport(
            customer = customer,
            summary = buildSummary(ledgerInvoices),
            invoices = ledgerInvoices.sortedByDescending { it.invoice.date }
        )
    }

    private fun buildSummary(invoices: List<CustomerLedgerInvoice>): CustomerLedgerSummary {
        var totalInvoiced = BigDecimal.ZERO
        var totalPaid = BigDecimal.ZERO
        var totalOutstanding = BigDecimal.ZERO
        var totalGrossProfit = BigDecimal.ZERO
        var totalNetProfit = BigDecimal.ZERO
        var totalProductLines = 0
        var settledInvoices = 0
        var openInvoices = 0

        invoices.forEach { entry ->
            totalInvoiced = totalInvoiced.add(entry.invoice.grandTotal)
            totalPaid = totalPaid.add(entry.totalPaid)
            totalOutstanding = totalOutstanding.add(entry.outstanding)
            totalGrossProfit = totalGrossProfit.add(entry.invoice.grossProfit)
            totalNetProfit = totalNetProfit.add(entry.invoice.netProfit)
            totalProductLines += entry.items.size

            if (entry.isSettled) {
                settledInvoices += 1
            } else {
                openInvoices += 1
            }
        }

        return CustomerLedgerSummary(
            totalInvoices = invoices.size,
            openInvoices = openInvoices,
            settledInvoices = settledInvoices,
            totalInvoiced = totalInvoiced,
            totalPaid = totalPaid,
            totalOutstanding = totalOutstanding,
            totalGrossProfit = totalGrossProfit,
            totalNetProfit = totalNetProfit,
            totalProductLines = totalProductLines
        )
    }
}

data class CustomerLedgerReport(
    val customer: Customer,
    val summary: CustomerLedgerSummary,
    val invoices: List<CustomerLedgerInvoice>
)

data class CustomerLedgerSummary(
    val totalInvoices: Int,
    val openInvoices: Int,
    val settledInvoices: Int,
    val totalInvoiced: BigDecimal,
    val totalPaid: BigDecimal,
    val totalOutstanding: BigDecimal,
    val totalGrossProfit: BigDecimal,
    val totalNetProfit: BigDecimal,
    val totalProductLines: Int
)

data class CustomerLedgerInvoice(
    val invoice: Invoice,
    val items: List<InvoiceItem>,
    val payments: List<Payment>,
    val totalPaid: BigDecimal,
    val outstanding: BigDecimal,
    val isSettled: Boolean
)
