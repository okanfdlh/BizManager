package com.bizmanager.data.repository

import com.bizmanager.data.database.Payments
import com.bizmanager.domain.model.Payment
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class PaymentRepository {

    fun insert(payment: Payment): Payment = transaction {
        val insertStatement = Payments.insert {
            it[paymentNumber] = payment.paymentNumber
            it[date] = payment.date
            it[invoiceId] = payment.invoiceId
            it[amount] = payment.amount
            it[paymentMethod] = payment.paymentMethod
            it[reference] = payment.reference
            it[notes] = payment.notes
            it[createdAt] = LocalDateTime.now()
        }
        payment.copy(id = insertStatement[Payments.id].value)
    }

    fun findByInvoiceId(invoiceId: Int): List<Payment> = transaction {
        Payments.select { Payments.invoiceId eq invoiceId }
            .orderBy(Payments.createdAt to SortOrder.DESC)
            .map { mapToPayment(it) }
    }

    fun findAll(): List<Payment> = transaction {
        Payments.selectAll()
            .orderBy(Payments.createdAt to SortOrder.DESC)
            .map { mapToPayment(it) }
    }

    private fun mapToPayment(row: ResultRow): Payment = Payment(
        id = row[Payments.id].value,
        paymentNumber = row[Payments.paymentNumber],
        date = row[Payments.date],
        invoiceId = row[Payments.invoiceId].value,
        amount = row[Payments.amount],
        paymentMethod = row[Payments.paymentMethod],
        reference = row[Payments.reference],
        notes = row[Payments.notes],
        createdAt = row[Payments.createdAt]
    )
}
