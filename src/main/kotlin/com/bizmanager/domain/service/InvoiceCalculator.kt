package com.bizmanager.domain.service

import com.bizmanager.domain.model.InvoiceItem
import java.math.BigDecimal

object InvoiceCalculator {

    fun calculateItem(qty: Int, sellPrice: BigDecimal, costPrice: BigDecimal, discount: BigDecimal): InvoiceItemCalculations {
        val qBD = BigDecimal(qty)
        val subtotal = qBD.multiply(sellPrice)
        val totalCost = qBD.multiply(costPrice)
        val grossProfit = subtotal.subtract(totalCost).subtract(discount)
        
        return InvoiceItemCalculations(
            subtotal = subtotal,
            totalCost = totalCost,
            grossProfit = grossProfit
        )
    }

    fun calculateInvoiceTotals(
        items: List<InvoiceItem>,
        additionalCost: BigDecimal,
        manualTotal: BigDecimal? = null
    ): InvoiceTotals {
        var subtotal = BigDecimal.ZERO
        var totalCost = BigDecimal.ZERO
        var totalDiscount = BigDecimal.ZERO

        items.forEach { item ->
            subtotal = subtotal.add(item.subtotal)
            totalCost = totalCost.add(item.totalCost)
            totalDiscount = totalDiscount.add(item.discount)
        }

        if (items.isEmpty() && manualTotal != null) {
            subtotal = manualTotal
        }

        val grossProfit = subtotal.subtract(totalCost).subtract(totalDiscount)
        val grandTotal = subtotal.subtract(totalDiscount)
        val netProfit = grossProfit.subtract(additionalCost)

        return InvoiceTotals(
            subtotal = subtotal,
            totalCost = totalCost,
            totalDiscount = totalDiscount,
            grossProfit = grossProfit,
            netProfit = netProfit,
            grandTotal = grandTotal
        )
    }
}

data class InvoiceItemCalculations(
    val subtotal: BigDecimal,
    val totalCost: BigDecimal,
    val grossProfit: BigDecimal
)

data class InvoiceTotals(
    val subtotal: BigDecimal,
    val totalCost: BigDecimal,
    val totalDiscount: BigDecimal,
    val grossProfit: BigDecimal,
    val netProfit: BigDecimal,
    val grandTotal: BigDecimal
)
