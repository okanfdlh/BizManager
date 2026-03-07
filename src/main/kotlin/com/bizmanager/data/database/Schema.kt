package com.bizmanager.data.database

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.math.BigDecimal

// We use string representations or big decimal for currency fields to prevent floating-point errors
// Exposed has a decimal extension

object Customers : IntIdTable("customers") {
    val code = varchar("code", 50).uniqueIndex()
    val name = varchar("name", 255)
    val company = varchar("company", 255).nullable()
    val phone = varchar("phone", 50).nullable()
    val email = varchar("email", 100).nullable()
    val address = text("address").nullable()
    val notes = text("notes").nullable()
    val isActive = bool("is_active").default(true)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

object Products : IntIdTable("products") {
    val code = varchar("code", 50).uniqueIndex()
    val name = varchar("name", 255)
    val category = varchar("category", 100).nullable()
    val unit = varchar("unit", 50).nullable()
    val costPrice = decimal("cost_price", 19, 4) // 19 digits total, 4 precision
    val sellPrice = decimal("sell_price", 19, 4)
    val isActive = bool("is_active").default(true)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

object Invoices : IntIdTable("invoices") {
    val invoiceNumber = varchar("invoice_number", 50).uniqueIndex()
    val date = datetime("date")
    val dueDate = datetime("due_date")
    val customerId = reference("customer_id", Customers)
    val notes = text("notes").nullable()
    
    val subtotal = decimal("subtotal", 19, 4)
    val totalDiscount = decimal("total_discount", 19, 4)
    val additionalCost = decimal("additional_cost", 19, 4)
    val totalCost = decimal("total_cost", 19, 4)
    val grossProfit = decimal("gross_profit", 19, 4)
    val netProfit = decimal("net_profit", 19, 4)
    val grandTotal = decimal("grand_total", 19, 4) // subtotal - totalDiscount
    
    val totalPaid = decimal("total_paid", 19, 4).default(BigDecimal.ZERO)
    val balanceDue = decimal("balance_due", 19, 4)
    
    val invoiceStatus = varchar("invoice_status", 50) // Draft, Posted, Cancelled
    val paymentStatus = varchar("payment_status", 50) // Unpaid, Outstanding, Paid
    
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

object InvoiceItems : IntIdTable("invoice_items") {
    val invoiceId = reference("invoice_id", Invoices)
    val productId = reference("product_id", Products)
    
    // Snapshot fields
    val productCodeSnapshot = varchar("product_code_snapshot", 50)
    val productNameSnapshot = varchar("product_name_snapshot", 255)
    val unitSnapshot = varchar("unit_snapshot", 50).nullable()
    
    val qty = integer("qty")
    val sellPrice = decimal("sell_price", 19, 4)
    val costPrice = decimal("cost_price", 19, 4)
    val discount = decimal("discount", 19, 4).default(BigDecimal.ZERO)
    
    val subtotal = decimal("subtotal", 19, 4)
    val totalCost = decimal("total_cost", 19, 4)
    val grossProfit = decimal("gross_profit", 19, 4)
    
    val createdAt = datetime("created_at")
}

object Payments : IntIdTable("payments") {
    val paymentNumber = varchar("payment_number", 50).uniqueIndex()
    val date = datetime("date")
    val invoiceId = reference("invoice_id", Invoices)
    
    val amount = decimal("amount", 19, 4)
    val paymentMethod = varchar("payment_method", 100)
    val reference = varchar("reference", 255).nullable()
    val notes = text("notes").nullable()
    
    val createdAt = datetime("created_at")
}

object AppSettings : IntIdTable("app_settings") { // Explicit columns
    val companyName = varchar("company_name", 255).nullable()
    val companyAddress = text("company_address").nullable()
    val phone = varchar("phone", 100).nullable()
    val email = varchar("email", 100).nullable()
    val logoPath = text("logo_path").nullable()
    
    val invoicePrefix = varchar("invoice_prefix", 20).default("INV")
    val paymentPrefix = varchar("payment_prefix", 20).default("PAY")
    val defaultDueDays = integer("default_due_days").default(30)
    val currency = varchar("currency", 20).default("Rp")
    
    val backupFolder = text("backup_folder").nullable()
    
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

object ActivityLogs : IntIdTable("activity_logs") {
    val createdAt = datetime("created_at")
    val action = varchar("action", 50)  // CREATE, UPDATE, CANCEL, DELETE, RESTORE, PAYMENT_ADD
    val entity = varchar("entity", 50)  // CUSTOMER, PRODUCT, INVOICE, PAYMENT, SETTINGS
    val referenceId = varchar("reference_id", 100).nullable() // Storing as string to handle composite IDs or UUIDs if ever needed
    val description = text("description").nullable()
}
