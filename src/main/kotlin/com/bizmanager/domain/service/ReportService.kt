package com.bizmanager.domain.service

import com.bizmanager.data.database.Customers
import com.bizmanager.data.database.Invoices
import com.bizmanager.data.database.Payments
import com.bizmanager.domain.model.InvoiceStatus
import com.bizmanager.domain.model.PaymentStatus
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greater
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

class ReportService {

    fun getSalesReport(startDate: LocalDateTime, endDate: LocalDateTime): SalesReport = transaction {
        val invoiceRows = Invoices.select {
            (Invoices.date greaterEq startDate) and
                (Invoices.date lessEq endDate) and
                (Invoices.invoiceStatus neq InvoiceStatus.Cancelled.name)
        }.toList()

        val paymentRows = Payments.select {
            (Payments.date greaterEq startDate) and
                (Payments.date lessEq endDate)
        }.toList()

        val customerNames = Customers.selectAll().associate { row ->
            row[Customers.id].value to row[Customers.name]
        }

        val invoices = invoiceRows.map { row ->
            SalesReportInvoiceRow(
                invoiceId = row[Invoices.id].value,
                invoiceNumber = row[Invoices.invoiceNumber],
                invoiceDate = row[Invoices.date],
                dueDate = row[Invoices.dueDate],
                customerName = customerNames[row[Invoices.customerId].value] ?: "Customer #${row[Invoices.customerId].value}",
                invoiceStatus = row[Invoices.invoiceStatus],
                paymentStatus = row[Invoices.paymentStatus],
                grandTotal = row[Invoices.grandTotal],
                totalPaid = row[Invoices.totalPaid],
                balanceDue = row[Invoices.balanceDue],
                grossProfit = row[Invoices.grossProfit],
                netProfit = row[Invoices.netProfit]
            )
        }

        val invoiceById = invoices.associateBy { it.invoiceId }

        val payments = paymentRows.map { row ->
            val invoice = invoiceById[row[Payments.invoiceId].value]
            SalesReportPaymentRow(
                paymentNumber = row[Payments.paymentNumber],
                paymentDate = row[Payments.date],
                invoiceNumber = invoice?.invoiceNumber ?: "Invoice #${row[Payments.invoiceId].value}",
                customerName = invoice?.customerName ?: "-",
                paymentMethod = row[Payments.paymentMethod],
                amount = row[Payments.amount],
                reference = row[Payments.reference]
            )
        }

        SalesReport(
            startDate = startDate,
            endDate = endDate,
            summary = buildSalesSummary(invoiceRows, paymentRows),
            invoices = invoices,
            payments = payments
        )
    }

    fun getSalesSummary(startDate: LocalDateTime, endDate: LocalDateTime): SalesSummary = transaction {
        val invoices = Invoices.select {
            (Invoices.date greaterEq startDate) and
                (Invoices.date lessEq endDate) and
                (Invoices.invoiceStatus neq InvoiceStatus.Cancelled.name)
        }.toList()

        val payments = Payments.select {
            (Payments.date greaterEq startDate) and
                (Payments.date lessEq endDate)
        }.toList()

        buildSalesSummary(invoices, payments)
    }

    fun getDashboardReport(monthCount: Int = 6): DashboardReport = transaction {
        val now = LocalDateTime.now()
        val currentMonthStart = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0)
        val currentMonthEnd = currentMonthStart.plusMonths(1).minusNanos(1)

        val periodInvoices = Invoices.select {
            (Invoices.date greaterEq currentMonthStart) and
                (Invoices.date lessEq currentMonthEnd) and
                (Invoices.invoiceStatus neq InvoiceStatus.Cancelled.name)
        }.toList()

        val periodPayments = Payments.select {
            (Payments.date greaterEq currentMonthStart) and
                (Payments.date lessEq currentMonthEnd)
        }.toList()

        val summary = buildSalesSummary(periodInvoices, periodPayments)

        var settledSales = BigDecimal.ZERO
        var creditSales = BigDecimal.ZERO
        var settledProfit = BigDecimal.ZERO
        var creditProfit = BigDecimal.ZERO
        var paidInvoiceCount = 0
        var outstandingInvoiceCount = 0
        var unpaidInvoiceCount = 0

        periodInvoices.forEach { row ->
            val grandTotal = row[Invoices.grandTotal]
            val netProfit = row[Invoices.netProfit]
            val paymentStatus = PaymentStatus.valueOf(row[Invoices.paymentStatus])
            val balanceDue = row[Invoices.balanceDue]

            if (paymentStatus == PaymentStatus.Paid || balanceDue == BigDecimal.ZERO) {
                settledSales = settledSales.add(grandTotal)
                settledProfit = settledProfit.add(netProfit)
                paidInvoiceCount += 1
            } else {
                creditSales = creditSales.add(grandTotal)
                creditProfit = creditProfit.add(netProfit)
            }

            when (paymentStatus) {
                PaymentStatus.Paid -> Unit
                PaymentStatus.Outstanding -> outstandingInvoiceCount += 1
                PaymentStatus.Unpaid -> unpaidInvoiceCount += 1
            }
        }

        val customerNames = Customers.selectAll().associate { row ->
            row[Customers.id].value to row[Customers.name]
        }

        val activeOutstandingInvoices = Invoices.select {
            (Invoices.invoiceStatus neq InvoiceStatus.Cancelled.name) and
                (Invoices.balanceDue greater BigDecimal.ZERO)
        }.toList()

        val receivableTotalsByCustomer = linkedMapOf<Int, BigDecimal>()
        val invoiceCountByCustomer = linkedMapOf<Int, Int>()
        var activeReceivables = BigDecimal.ZERO

        activeOutstandingInvoices.forEach { row ->
            val customerId = row[Invoices.customerId].value
            val balanceDue = row[Invoices.balanceDue]
            activeReceivables = activeReceivables.add(balanceDue)
            receivableTotalsByCustomer[customerId] = (receivableTotalsByCustomer[customerId] ?: BigDecimal.ZERO).add(balanceDue)
            invoiceCountByCustomer[customerId] = (invoiceCountByCustomer[customerId] ?: 0) + 1
        }

        val topReceivableCustomers = receivableTotalsByCustomer.entries
            .sortedByDescending { it.value }
            .take(5)
            .map { entry ->
                CustomerReceivableSnapshot(
                    customerId = entry.key,
                    customerName = customerNames[entry.key] ?: "Customer #${entry.key}",
                    totalReceivable = entry.value,
                    invoiceCount = invoiceCountByCustomer[entry.key] ?: 0
                )
            }

        val trendStart = currentMonthStart.minusMonths((monthCount - 1).coerceAtLeast(0).toLong())
        val monthKeys = (0 until monthCount).map { YearMonth.from(trendStart.plusMonths(it.toLong())) }
        val revenueByMonth = monthKeys.associateWith { BigDecimal.ZERO }.toMutableMap()
        val collectionsByMonth = monthKeys.associateWith { BigDecimal.ZERO }.toMutableMap()
        val profitByMonth = monthKeys.associateWith { BigDecimal.ZERO }.toMutableMap()

        Invoices.select {
            (Invoices.date greaterEq trendStart) and
                (Invoices.invoiceStatus neq InvoiceStatus.Cancelled.name)
        }.forEach { row ->
            val monthKey = YearMonth.from(row[Invoices.date])
            if (monthKey in revenueByMonth) {
                revenueByMonth[monthKey] = revenueByMonth.getValue(monthKey).add(row[Invoices.grandTotal])
                profitByMonth[monthKey] = profitByMonth.getValue(monthKey).add(row[Invoices.netProfit])
            }
        }

        Payments.select {
            Payments.date greaterEq trendStart
        }.forEach { row ->
            val monthKey = YearMonth.from(row[Payments.date])
            if (monthKey in collectionsByMonth) {
                collectionsByMonth[monthKey] = collectionsByMonth.getValue(monthKey).add(row[Payments.amount])
            }
        }

        val labelFormatter = DateTimeFormatter.ofPattern("MMM yy", Locale("id", "ID"))
        val monthlyTrend = monthKeys.map { monthKey ->
            MonthlyTrendPoint(
                label = monthKey.atDay(1).format(labelFormatter),
                revenue = revenueByMonth.getValue(monthKey),
                collections = collectionsByMonth.getValue(monthKey),
                netProfit = profitByMonth.getValue(monthKey)
            )
        }

        DashboardReport(
            periodSummary = summary,
            activeReceivables = activeReceivables,
            settledSales = settledSales,
            creditSales = creditSales,
            settledProfit = settledProfit,
            creditProfit = creditProfit,
            paidInvoiceCount = paidInvoiceCount,
            outstandingInvoiceCount = outstandingInvoiceCount,
            unpaidInvoiceCount = unpaidInvoiceCount,
            monthlyTrend = monthlyTrend,
            topReceivableCustomers = topReceivableCustomers
        )
    }

    // ─── Report Buku Besar ─────────────────────────────────────────────────────

    fun getBukuBesarReport(filter: BukuBesarFilter, page: Int = 1): BukuBesarResult = transaction {
        val PAGE_SIZE = 50
        val today = LocalDate.now()

        // Load all data (in-memory filter for flexibility)
        val invoiceRows = Invoices.selectAll().toList()
        val customerMap = Customers.selectAll().associate { row ->
            row[Customers.id].value to Triple(
                row[Customers.code],
                row[Customers.name],
                row[Customers.company]
            )
        }
        val allPayments = Payments.selectAll().toList()
        val latestPaymentByInvoice = allPayments
            .groupBy { it[Payments.invoiceId].value }
            .mapValues { (_, payments) -> payments.maxByOrNull { it[Payments.date] } }

        val allRows = invoiceRows.mapNotNull { row ->
            val invoiceId = row[Invoices.id].value
            val customerId = row[Invoices.customerId].value
            val customerTriple = customerMap[customerId] ?: return@mapNotNull null
            val (customerCode, customerName, _) = customerTriple

            val invoiceDate = row[Invoices.date]
            val grandTotal = row[Invoices.grandTotal]
            val balanceDue = row[Invoices.balanceDue]
            val daysOld = ChronoUnit.DAYS.between(invoiceDate.toLocalDate(), today)
            val latestPayment = latestPaymentByInvoice[invoiceId]
            val paymentType = latestPayment?.get(Payments.paymentMethod)

            // Period filter
            filter.startDate?.let { if (invoiceDate < it) return@mapNotNull null }
            filter.endDate?.let { if (invoiceDate > it) return@mapNotNull null }

            // Amount filter (only when amount > 0)
            filter.amountValue?.let { amt ->
                if (amt.signum() > 0) {
                    when (filter.amountOperator) {
                        AmountOperator.GTE -> if (grandTotal < amt) return@mapNotNull null
                        AmountOperator.LTE -> if (grandTotal > amt) return@mapNotNull null
                    }
                }
            }

            // Customer ID/Name filter
            if (filter.customerQuery.isNotBlank()) {
                val q = filter.customerQuery.lowercase()
                if (!customerCode.lowercase().contains(q) && !customerName.lowercase().contains(q)) {
                    return@mapNotNull null
                }
            }

            // Aging/status classification
            val agingStatus = when {
                balanceDue.signum() == 0 -> "Closed"
                daysOld <= 30 -> "New"
                daysOld <= 90 -> "Outstanding"
                else -> "Unpaid"
            }

            // Status filter
            val passesStatus = when (filter.statusAs) {
                BukuBesarStatus.Closed -> balanceDue.signum() == 0
                BukuBesarStatus.New -> daysOld <= 30
                BukuBesarStatus.Outstanding -> daysOld > 30 && daysOld <= 90
                BukuBesarStatus.Unpaid -> daysOld > 90
                BukuBesarStatus.AllOutstanding -> balanceDue.signum() > 0
                null -> true
            }
            if (!passesStatus) return@mapNotNull null

            // Payment type filter
            if (filter.paymentType.isNotBlank()) {
                if (paymentType == null || !paymentType.contains(filter.paymentType, ignoreCase = true)) {
                    return@mapNotNull null
                }
            }

            BukuBesarRow(
                customerCode = customerCode,
                customerName = customerName,
                fakturNr = row[Invoices.invoiceNumber],
                fakturDate = invoiceDate,
                agingStatus = agingStatus,
                description = row[Invoices.notes],
                paidDate = latestPayment?.get(Payments.date),
                paid = row[Invoices.totalPaid],
                outstanding = balanceDue,
                total = grandTotal,
                paymentType = paymentType,
                margin = row[Invoices.netProfit]
            )
        }.sortedWith(compareBy({ it.customerCode }, { it.fakturDate }))

        val foundCount = allRows.size
        val totalMargin = allRows.fold(BigDecimal.ZERO) { acc, r -> acc.add(r.margin) }
        val totalPaid = allRows.fold(BigDecimal.ZERO) { acc, r -> acc.add(r.paid) }
        val totalOutstanding = allRows.fold(BigDecimal.ZERO) { acc, r -> acc.add(r.outstanding) }
        val totalAmount = allRows.fold(BigDecimal.ZERO) { acc, r -> acc.add(r.total) }

        val totalPages = maxOf(1, (foundCount + PAGE_SIZE - 1) / PAGE_SIZE)
        val currentPage = page.coerceIn(1, totalPages)
        val pageRows = allRows.drop((currentPage - 1) * PAGE_SIZE).take(PAGE_SIZE)

        BukuBesarResult(
            rows = pageRows,
            foundCount = foundCount,
            totalMargin = totalMargin,
            totalPaid = totalPaid,
            totalOutstanding = totalOutstanding,
            totalAmount = totalAmount,
            currentPage = currentPage,
            totalPages = totalPages
        )
    }

    private fun buildSalesSummary(invoices: List<ResultRow>, payments: List<ResultRow>): SalesSummary {
        var totalOmzet = BigDecimal.ZERO
        var totalGrossProfit = BigDecimal.ZERO
        var totalNetProfit = BigDecimal.ZERO
        var totalReceivables = BigDecimal.ZERO
        var totalPaidInPeriod = BigDecimal.ZERO

        invoices.forEach { row ->
            totalOmzet = totalOmzet.add(row[Invoices.grandTotal])
            totalGrossProfit = totalGrossProfit.add(row[Invoices.grossProfit])
            totalNetProfit = totalNetProfit.add(row[Invoices.netProfit])
            totalReceivables = totalReceivables.add(row[Invoices.balanceDue])
        }

        payments.forEach { row ->
            totalPaidInPeriod = totalPaidInPeriod.add(row[Payments.amount])
        }

        return SalesSummary(
            totalOmzet = totalOmzet,
            totalGrossProfit = totalGrossProfit,
            totalNetProfit = totalNetProfit,
            totalReceivables = totalReceivables,
            totalPaymentsReceived = totalPaidInPeriod
        )
    }
}

// ─── Sales Report Models ───────────────────────────────────────────────────────

data class SalesSummary(
    val totalOmzet: BigDecimal,
    val totalGrossProfit: BigDecimal,
    val totalNetProfit: BigDecimal,
    val totalReceivables: BigDecimal,
    val totalPaymentsReceived: BigDecimal
)

data class SalesReport(
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val summary: SalesSummary,
    val invoices: List<SalesReportInvoiceRow>,
    val payments: List<SalesReportPaymentRow>
)

data class SalesReportInvoiceRow(
    val invoiceId: Int,
    val invoiceNumber: String,
    val invoiceDate: LocalDateTime,
    val dueDate: LocalDateTime,
    val customerName: String,
    val invoiceStatus: String,
    val paymentStatus: String,
    val grandTotal: BigDecimal,
    val totalPaid: BigDecimal,
    val balanceDue: BigDecimal,
    val grossProfit: BigDecimal,
    val netProfit: BigDecimal
)

data class SalesReportPaymentRow(
    val paymentNumber: String,
    val paymentDate: LocalDateTime,
    val invoiceNumber: String,
    val customerName: String,
    val paymentMethod: String,
    val amount: BigDecimal,
    val reference: String?
)

data class DashboardReport(
    val periodSummary: SalesSummary,
    val activeReceivables: BigDecimal,
    val settledSales: BigDecimal,
    val creditSales: BigDecimal,
    val settledProfit: BigDecimal,
    val creditProfit: BigDecimal,
    val paidInvoiceCount: Int,
    val outstandingInvoiceCount: Int,
    val unpaidInvoiceCount: Int,
    val monthlyTrend: List<MonthlyTrendPoint>,
    val topReceivableCustomers: List<CustomerReceivableSnapshot>
)

data class MonthlyTrendPoint(
    val label: String,
    val revenue: BigDecimal,
    val collections: BigDecimal,
    val netProfit: BigDecimal
)

data class CustomerReceivableSnapshot(
    val customerId: Int,
    val customerName: String,
    val totalReceivable: BigDecimal,
    val invoiceCount: Int
)

// ─── Buku Besar Report Models ──────────────────────────────────────────────────

enum class BukuBesarStatus { Closed, New, Outstanding, Unpaid, AllOutstanding }

enum class AmountOperator { GTE, LTE }

data class BukuBesarFilter(
    val startDate: LocalDateTime? = null,
    val endDate: LocalDateTime? = null,
    val amountValue: BigDecimal? = null,
    val amountOperator: AmountOperator = AmountOperator.GTE,
    val statusAs: BukuBesarStatus? = null,
    val customerQuery: String = "",
    val paymentType: String = ""
)

data class BukuBesarRow(
    val customerCode: String,
    val customerName: String,
    val fakturNr: String,
    val fakturDate: LocalDateTime,
    val agingStatus: String,
    val description: String?,
    val paidDate: LocalDateTime?,
    val paid: BigDecimal,
    val outstanding: BigDecimal,
    val total: BigDecimal,
    val paymentType: String?,
    val margin: BigDecimal
)

data class BukuBesarResult(
    val rows: List<BukuBesarRow>,
    val foundCount: Int,
    val totalMargin: BigDecimal,
    val totalPaid: BigDecimal,
    val totalOutstanding: BigDecimal,
    val totalAmount: BigDecimal,
    val currentPage: Int,
    val totalPages: Int
)
