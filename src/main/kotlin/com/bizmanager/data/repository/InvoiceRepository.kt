package com.bizmanager.data.repository

import com.bizmanager.data.database.InvoiceItems
import com.bizmanager.data.database.Invoices
import com.bizmanager.domain.model.Invoice
import com.bizmanager.domain.model.InvoiceItem
import com.bizmanager.domain.model.InvoiceStatus
import com.bizmanager.domain.model.PaymentStatus
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class InvoiceRepository {

    fun insertWithItems(invoice: Invoice, items: List<InvoiceItem>): Invoice = transaction {
        val insertInvoice = Invoices.insert {
            it[invoiceNumber] = invoice.invoiceNumber
            it[date] = invoice.date
            it[dueDate] = invoice.dueDate
            it[customerId] = invoice.customerId
            it[notes] = invoice.notes
            it[subtotal] = invoice.subtotal
            it[totalDiscount] = invoice.totalDiscount
            it[additionalCost] = invoice.additionalCost
            it[totalCost] = invoice.totalCost
            it[grossProfit] = invoice.grossProfit
            it[netProfit] = invoice.netProfit
            it[grandTotal] = invoice.grandTotal
            it[totalPaid] = invoice.totalPaid
            it[balanceDue] = invoice.balanceDue
            it[invoiceStatus] = invoice.invoiceStatus.name
            it[paymentStatus] = invoice.paymentStatus.name
            it[createdAt] = LocalDateTime.now()
            it[updatedAt] = LocalDateTime.now()
        }
        
        val newInvoiceId = insertInvoice[Invoices.id].value

        InvoiceItems.batchInsert(items) { item ->
            this[InvoiceItems.invoiceId] = newInvoiceId
            this[InvoiceItems.productId] = item.productId
            this[InvoiceItems.productCodeSnapshot] = item.productCodeSnapshot
            this[InvoiceItems.productNameSnapshot] = item.productNameSnapshot
            this[InvoiceItems.unitSnapshot] = item.unitSnapshot
            this[InvoiceItems.qty] = item.qty
            this[InvoiceItems.sellPrice] = item.sellPrice
            this[InvoiceItems.costPrice] = item.costPrice
            this[InvoiceItems.discount] = item.discount
            this[InvoiceItems.subtotal] = item.subtotal
            this[InvoiceItems.totalCost] = item.totalCost
            this[InvoiceItems.grossProfit] = item.grossProfit
            this[InvoiceItems.createdAt] = LocalDateTime.now()
        }

        invoice.copy(id = newInvoiceId)
    }

    fun updateInvoice(invoice: Invoice) = transaction {
        Invoices.update({ Invoices.id eq invoice.id }) {
            it[date] = invoice.date
            it[dueDate] = invoice.dueDate
            it[customerId] = invoice.customerId
            it[notes] = invoice.notes
            it[subtotal] = invoice.subtotal
            it[totalDiscount] = invoice.totalDiscount
            it[additionalCost] = invoice.additionalCost
            it[totalCost] = invoice.totalCost
            it[grossProfit] = invoice.grossProfit
            it[netProfit] = invoice.netProfit
            it[grandTotal] = invoice.grandTotal
            it[totalPaid] = invoice.totalPaid
            it[balanceDue] = invoice.balanceDue
            it[invoiceStatus] = invoice.invoiceStatus.name
            it[paymentStatus] = invoice.paymentStatus.name
            it[updatedAt] = LocalDateTime.now()
        }
    }

    fun findById(id: Int): Invoice? = transaction {
        Invoices.select { Invoices.id eq id }.map { mapToInvoice(it) }.singleOrNull()
    }

    fun getItemsForInvoice(invoiceId: Int): List<InvoiceItem> = transaction {
        InvoiceItems.select { InvoiceItems.invoiceId eq invoiceId }
            .map { mapToInvoiceItem(it) }
    }

    fun findAll(): List<Invoice> = transaction {
        Invoices.selectAll()
            .orderBy(Invoices.createdAt to SortOrder.DESC)
            .map { mapToInvoice(it) }
    }

    fun findByCustomerId(customerId: Int): List<Invoice> = transaction {
        Invoices.select { Invoices.customerId eq customerId }
            .orderBy(Invoices.createdAt to SortOrder.DESC)
            .map { mapToInvoice(it) }
    }
    
    // For aging parsing logic efficiently
    fun findAllWithPositiveBalance(): List<Invoice> = transaction {
        Invoices.select { Invoices.balanceDue greater java.math.BigDecimal.ZERO }
            .map { mapToInvoice(it) }
    }

    fun deleteItemsForInvoice(invoiceId: Int) = transaction {
        InvoiceItems.deleteWhere { InvoiceItems.invoiceId eq EntityID(invoiceId, Invoices) }
    }

    private fun mapToInvoice(row: ResultRow): Invoice = Invoice(
        id = row[Invoices.id].value,
        invoiceNumber = row[Invoices.invoiceNumber],
        date = row[Invoices.date],
        dueDate = row[Invoices.dueDate],
        customerId = row[Invoices.customerId].value,
        notes = row[Invoices.notes],
        subtotal = row[Invoices.subtotal],
        totalDiscount = row[Invoices.totalDiscount],
        additionalCost = row[Invoices.additionalCost],
        totalCost = row[Invoices.totalCost],
        grossProfit = row[Invoices.grossProfit],
        netProfit = row[Invoices.netProfit],
        grandTotal = row[Invoices.grandTotal],
        totalPaid = row[Invoices.totalPaid],
        balanceDue = row[Invoices.balanceDue],
        invoiceStatus = InvoiceStatus.valueOf(row[Invoices.invoiceStatus]),
        paymentStatus = PaymentStatus.valueOf(row[Invoices.paymentStatus]),
        createdAt = row[Invoices.createdAt],
        updatedAt = row[Invoices.updatedAt]
    )

    private fun mapToInvoiceItem(row: ResultRow): InvoiceItem = InvoiceItem(
        id = row[InvoiceItems.id].value,
        invoiceId = row[InvoiceItems.invoiceId].value,
        productId = row[InvoiceItems.productId].value,
        productCodeSnapshot = row[InvoiceItems.productCodeSnapshot],
        productNameSnapshot = row[InvoiceItems.productNameSnapshot],
        unitSnapshot = row[InvoiceItems.unitSnapshot],
        qty = row[InvoiceItems.qty],
        sellPrice = row[InvoiceItems.sellPrice],
        costPrice = row[InvoiceItems.costPrice],
        discount = row[InvoiceItems.discount],
        subtotal = row[InvoiceItems.subtotal],
        totalCost = row[InvoiceItems.totalCost],
        grossProfit = row[InvoiceItems.grossProfit],
        createdAt = row[InvoiceItems.createdAt]
    )
}
