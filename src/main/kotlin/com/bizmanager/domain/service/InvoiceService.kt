package com.bizmanager.domain.service

import com.bizmanager.data.repository.InvoiceRepository
import com.bizmanager.data.repository.ProductRepository
import com.bizmanager.domain.model.*
import java.math.BigDecimal
import java.time.LocalDateTime

class InvoiceService(
    private val invoiceRepository: InvoiceRepository,
    private val productRepository: ProductRepository,
    private val documentNumberGenerator: DocumentNumberGenerator
) {

    /**
     * Creates a new Draft or Posted Invoice, calculating snapshot instances for each item.
     */
    fun createInvoice(
        customerId: Int,
        dueDate: LocalDateTime,
        additionalCost: BigDecimal,
        notes: String?,
        isDraft: Boolean,
        itemsInput: List<InvoiceItemInput>
    ): Invoice {
        val today = LocalDateTime.now()
        
        // Count today's invoices roughly for sequencing
        val todaysCount = invoiceRepository.findAll().count { it.createdAt.toLocalDate() == today.toLocalDate() }
        val invoiceNum = documentNumberGenerator.generateInvoiceNumber(todaysCount)

        val processedItems = mutableListOf<InvoiceItem>()
        
        // Create snapshots
        for (input in itemsInput) {
            val product = productRepository.findById(input.productId) 
                ?: throw IllegalArgumentException("Product ID ${input.productId} not found")
            
            val calculations = InvoiceCalculator.calculateItem(
                qty = input.qty,
                sellPrice = product.sellPrice,
                costPrice = product.costPrice,
                discount = input.discount
            )
            
            val snapshotItem = InvoiceItem(
                invoiceId = 0, // Assigned inside repo transaction
                productId = product.id,
                productCodeSnapshot = product.code,
                productNameSnapshot = product.name,
                unitSnapshot = product.unit,
                qty = input.qty,
                sellPrice = product.sellPrice,
                costPrice = product.costPrice,
                discount = input.discount,
                subtotal = calculations.subtotal,
                totalCost = calculations.totalCost,
                grossProfit = calculations.grossProfit
            )
            processedItems.add(snapshotItem)
        }

        val totals = InvoiceCalculator.calculateInvoiceTotals(processedItems, additionalCost)
        val status = if (isDraft) InvoiceStatus.Draft else InvoiceStatus.Posted
        
        val newInvoice = Invoice(
            invoiceNumber = invoiceNum,
            date = today,
            dueDate = dueDate,
            customerId = customerId,
            notes = notes,
            subtotal = totals.subtotal,
            totalDiscount = totals.totalDiscount,
            additionalCost = additionalCost,
            totalCost = totals.totalCost,
            grossProfit = totals.grossProfit,
            netProfit = totals.netProfit,
            grandTotal = totals.grandTotal,
            totalPaid = BigDecimal.ZERO,
            balanceDue = totals.grandTotal, // Initially balanceDue == grandTotal
            invoiceStatus = status,
            paymentStatus = PaymentStatus.Unpaid // Initially unpaid
        )

        return invoiceRepository.insertWithItems(newInvoice, processedItems)
    }

    /**
     * Edits a draft invoice. Posted or Cancelled invoices cannot be edited.
     */
    fun updateDraftInvoice(
        invoiceId: Int,
        dueDate: LocalDateTime,
        additionalCost: BigDecimal,
        notes: String?,
        postInvoice: Boolean,
        itemsInput: List<InvoiceItemInput>
    ): Invoice {
        val existing = invoiceRepository.findById(invoiceId)
            ?: throw IllegalArgumentException("Invoice not found")

        if (existing.invoiceStatus != InvoiceStatus.Draft) {
            throw IllegalStateException("Only Draft invoices can be updated.")
        }

        val processedItems = mutableListOf<InvoiceItem>()
        
        // Re-snapshot products in case they changed, or stick to input
        for (input in itemsInput) {
            val product = productRepository.findById(input.productId) 
                ?: throw IllegalArgumentException("Product ID ${input.productId} not found")
            
            val calculations = InvoiceCalculator.calculateItem(
                qty = input.qty,
                sellPrice = product.sellPrice,
                costPrice = product.costPrice,
                discount = input.discount
            )
            
            val snapshotItem = InvoiceItem(
                invoiceId = existing.id,
                productId = product.id,
                productCodeSnapshot = product.code,
                productNameSnapshot = product.name,
                unitSnapshot = product.unit,
                qty = input.qty,
                sellPrice = product.sellPrice,
                costPrice = product.costPrice,
                discount = input.discount,
                subtotal = calculations.subtotal,
                totalCost = calculations.totalCost,
                grossProfit = calculations.grossProfit
            )
            processedItems.add(snapshotItem)
        }

        val totals = InvoiceCalculator.calculateInvoiceTotals(processedItems, additionalCost)
        val status = if (postInvoice) InvoiceStatus.Posted else InvoiceStatus.Draft

        val updatedInvoice = existing.copy(
            dueDate = dueDate,
            notes = notes,
            subtotal = totals.subtotal,
            totalDiscount = totals.totalDiscount,
            additionalCost = additionalCost,
            totalCost = totals.totalCost,
            grossProfit = totals.grossProfit,
            netProfit = totals.netProfit,
            grandTotal = totals.grandTotal,
            // Assuming we haven't received payments since it was draft
            balanceDue = totals.grandTotal, 
            invoiceStatus = status
        )

        // Delete old items and insert new ones
        invoiceRepository.deleteItemsForInvoice(existing.id)
        return invoiceRepository.insertWithItems(updatedInvoice, processedItems)
    }

    fun cancelInvoice(invoiceId: Int) {
        val existing = invoiceRepository.findById(invoiceId)
            ?: throw IllegalArgumentException("Invoice not found")
        
        if (existing.invoiceStatus == InvoiceStatus.Cancelled) return
        
        val updated = existing.copy(
            invoiceStatus = InvoiceStatus.Cancelled
        )
        invoiceRepository.updateInvoice(updated)
    }
}

data class InvoiceItemInput(
    val productId: Int,
    val qty: Int,
    val discount: BigDecimal = BigDecimal.ZERO
)
