package com.bizmanager.domain.service

import com.bizmanager.data.repository.InvoiceRepository
import com.bizmanager.data.repository.PaymentRepository
import com.bizmanager.domain.model.InvoiceStatus
import com.bizmanager.domain.model.Payment
import com.bizmanager.domain.model.PaymentStatus
import java.math.BigDecimal
import java.time.LocalDateTime

class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val invoiceRepository: InvoiceRepository,
    private val documentNumberGenerator: DocumentNumberGenerator
) {

    fun addPayment(
        invoiceId: Int,
        amount: BigDecimal,
        paymentMethod: String,
        reference: String?,
        notes: String?,
        paymentDate: LocalDateTime? = null
    ): Payment {
        val invoice = invoiceRepository.findById(invoiceId)
            ?: throw IllegalArgumentException("Invoice not found")

        // Validation Rules
        if (invoice.invoiceStatus == InvoiceStatus.Cancelled) {
            throw IllegalStateException("Cannot receive payments for a cancelled invoice.")
        }
        if (invoice.invoiceStatus == InvoiceStatus.Draft) {
            throw IllegalStateException("Cannot receive payments for a draft invoice. Please post it first.")
        }
        if (amount <= BigDecimal.ZERO) {
            throw IllegalArgumentException("Payment amount must be greater than zero.")
        }
        if (amount > invoice.balanceDue) {
            throw IllegalArgumentException("Payment amount cannot exceed the balance due of ${invoice.balanceDue}.")
        }

        val today = paymentDate ?: LocalDateTime.now()
        val todaysCount = paymentRepository.findAll().count { it.createdAt.toLocalDate() == today.toLocalDate() }
        val paymentNum = documentNumberGenerator.generatePaymentNumber(todaysCount)

        val payment = Payment(
            paymentNumber = paymentNum,
            date = today,
            invoiceId = invoice.id,
            amount = amount,
            paymentMethod = paymentMethod,
            reference = reference,
            notes = notes
        )

        val savedPayment = paymentRepository.insert(payment)

        // Update Invoice status and balance
        val newTotalPaid = invoice.totalPaid.add(amount)
        val newBalanceDue = invoice.grandTotal.subtract(newTotalPaid)

        val newPaymentStatus = when {
            newBalanceDue <= BigDecimal.ZERO -> PaymentStatus.Paid
            newTotalPaid > BigDecimal.ZERO -> PaymentStatus.Outstanding
            else -> PaymentStatus.Unpaid
        }

        val updatedInvoice = invoice.copy(
            totalPaid = newTotalPaid,
            balanceDue = newBalanceDue,
            paymentStatus = newPaymentStatus,
            updatedAt = LocalDateTime.now()
        )
        invoiceRepository.updateInvoice(updatedInvoice)

        return savedPayment
    }
}
