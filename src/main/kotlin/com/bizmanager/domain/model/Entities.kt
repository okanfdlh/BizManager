package com.bizmanager.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class Customer(
    val id: Int = 0,
    val code: String,
    val name: String,
    val company: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val notes: String? = null,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

data class Product(
    val id: Int = 0,
    val code: String,
    val name: String,
    val category: String? = null,
    val unit: String? = null,
    val costPrice: BigDecimal,
    val sellPrice: BigDecimal,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

data class Invoice(
    val id: Int = 0,
    val invoiceNumber: String,
    val date: LocalDateTime,
    val dueDate: LocalDateTime,
    val customerId: Int,
    val notes: String? = null,
    val subtotal: BigDecimal,
    val totalDiscount: BigDecimal,
    val additionalCost: BigDecimal,
    val totalCost: BigDecimal,
    val grossProfit: BigDecimal,
    val netProfit: BigDecimal,
    val grandTotal: BigDecimal,
    val totalPaid: BigDecimal = BigDecimal.ZERO,
    val balanceDue: BigDecimal,
    val invoiceStatus: InvoiceStatus,
    val paymentStatus: PaymentStatus,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

data class InvoiceItem(
    val id: Int = 0,
    val invoiceId: Int,
    val productId: Int,
    val productCodeSnapshot: String,
    val productNameSnapshot: String,
    val unitSnapshot: String? = null,
    val qty: Int,
    val sellPrice: BigDecimal,
    val costPrice: BigDecimal,
    val discount: BigDecimal = BigDecimal.ZERO,
    val subtotal: BigDecimal,
    val totalCost: BigDecimal,
    val grossProfit: BigDecimal,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

data class Payment(
    val id: Int = 0,
    val paymentNumber: String,
    val date: LocalDateTime,
    val invoiceId: Int,
    val amount: BigDecimal,
    val paymentMethod: String,
    val reference: String? = null,
    val notes: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

data class AppSetting(
    val id: Int = 0,
    val companyName: String? = null,
    val companyAddress: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val logoPath: String? = null,
    val invoicePrefix: String = "INV",
    val paymentPrefix: String = "PAY",
    val defaultDueDays: Int = 30,
    val currency: String = "Rp",
    val backupFolder: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

data class ActivityLog(
    val id: Int = 0,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val action: String,
    val entity: String,
    val referenceId: String? = null,
    val description: String? = null
)

enum class InvoiceStatus {
    Draft,
    Posted,
    Cancelled
}

enum class PaymentStatus {
    Unpaid,
    Outstanding,
    Paid
}

enum class AgingStatus {
    Current,
    Overdue0To30,
    Overdue31To90,
    OverdueMoreThan90
}
