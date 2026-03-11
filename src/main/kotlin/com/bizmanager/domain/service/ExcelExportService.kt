package com.bizmanager.domain.service

import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.time.format.DateTimeFormatter

class ExcelExportService {

    fun exportSalesReport(report: SalesReport, outputFile: File) {
        outputFile.parentFile?.mkdirs()

        XSSFWorkbook().use { workbook ->
            val styles = WorkbookStyles(workbook)
            createSummarySheet(workbook, styles, report)
            createInvoiceSheet(workbook, styles, report)
            createPaymentSheet(workbook, styles, report)

            for (sheetIndex in 0 until workbook.numberOfSheets) {
                val sheet = workbook.getSheetAt(sheetIndex)
                val headerRow = sheet.getRow(0) ?: continue
                for (columnIndex in 0 until headerRow.lastCellNum) {
                    sheet.autoSizeColumn(columnIndex)
                }
            }

            FileOutputStream(outputFile).use { stream ->
                workbook.write(stream)
            }
        }
    }

    private fun createSummarySheet(workbook: XSSFWorkbook, styles: WorkbookStyles, report: SalesReport) {
        val sheet = workbook.createSheet("Ringkasan")
        var rowIndex = 0

        sheet.createRow(rowIndex++).apply {
            createStyledCell(0, "Laporan Penjualan BizManager", styles.header)
        }
        sheet.createRow(rowIndex++).apply {
            createStyledCell(0, "Periode", styles.label)
            createStyledCell(
                1,
                "${report.startDate.toLocalDate()} s/d ${report.endDate.toLocalDate()}",
                styles.text
            )
        }

        rowIndex++

        val rows = listOf(
            "Total Omzet Penjualan" to report.summary.totalOmzet,
            "Total Laba Kotor" to report.summary.totalGrossProfit,
            "Total Laba Bersih" to report.summary.totalNetProfit,
            "Total Pembayaran Masuk" to report.summary.totalPaymentsReceived,
            "Total Sisa Piutang" to report.summary.totalReceivables
        )

        rows.forEach { (label, value) ->
            sheet.createRow(rowIndex++).apply {
                createStyledCell(0, label, styles.label)
                createStyledAmountCell(1, value.toDouble(), styles.amount)
            }
        }
    }

    private fun createInvoiceSheet(workbook: XSSFWorkbook, styles: WorkbookStyles, report: SalesReport) {
        val sheet = workbook.createSheet("Invoice")
        val headers = listOf(
            "No. Invoice",
            "Tanggal Invoice",
            "Jatuh Tempo",
            "Customer",
            "Status Invoice",
            "Status Bayar",
            "Grand Total",
            "Total Dibayar",
            "Sisa Tagihan",
            "Laba Kotor",
            "Laba Bersih"
        )

        sheet.createRow(0).apply {
            headers.forEachIndexed { index, label ->
                createStyledCell(index, label, styles.header)
            }
        }

        report.invoices.forEachIndexed { index, invoice ->
            sheet.createRow(index + 1).apply {
                createStyledCell(0, invoice.invoiceNumber, styles.text)
                createStyledCell(1, invoice.invoiceDate.format(DATE_TIME_FORMATTER), styles.text)
                createStyledCell(2, invoice.dueDate.format(DATE_TIME_FORMATTER), styles.text)
                createStyledCell(3, invoice.customerName, styles.text)
                createStyledCell(4, invoice.invoiceStatus, styles.text)
                createStyledCell(5, invoice.paymentStatus, styles.text)
                createStyledAmountCell(6, invoice.grandTotal.toDouble(), styles.amount)
                createStyledAmountCell(7, invoice.totalPaid.toDouble(), styles.amount)
                createStyledAmountCell(8, invoice.balanceDue.toDouble(), styles.amount)
                createStyledAmountCell(9, invoice.grossProfit.toDouble(), styles.amount)
                createStyledAmountCell(10, invoice.netProfit.toDouble(), styles.amount)
            }
        }
    }

    private fun createPaymentSheet(workbook: XSSFWorkbook, styles: WorkbookStyles, report: SalesReport) {
        val sheet = workbook.createSheet("Pembayaran")
        val headers = listOf(
            "No. Pembayaran",
            "Tanggal Pembayaran",
            "No. Invoice",
            "Customer",
            "Metode Pembayaran",
            "Nominal",
            "Referensi"
        )

        sheet.createRow(0).apply {
            headers.forEachIndexed { index, label ->
                createStyledCell(index, label, styles.header)
            }
        }

        report.payments.forEachIndexed { index, payment ->
            sheet.createRow(index + 1).apply {
                createStyledCell(0, payment.paymentNumber, styles.text)
                createStyledCell(1, payment.paymentDate.format(DATE_TIME_FORMATTER), styles.text)
                createStyledCell(2, payment.invoiceNumber, styles.text)
                createStyledCell(3, payment.customerName, styles.text)
                createStyledCell(4, payment.paymentMethod, styles.text)
                createStyledAmountCell(5, payment.amount.toDouble(), styles.amount)
                createStyledCell(6, payment.reference ?: "-", styles.text)
            }
        }
    }

    private fun org.apache.poi.ss.usermodel.Row.createStyledCell(columnIndex: Int, value: String, style: CellStyle) {
        createCell(columnIndex).apply {
            setCellValue(value)
            cellStyle = style
        }
    }

    private fun org.apache.poi.ss.usermodel.Row.createStyledAmountCell(columnIndex: Int, value: Double, style: CellStyle) {
        createCell(columnIndex).apply {
            setCellValue(value)
            cellStyle = style
        }
    }

    private class WorkbookStyles(workbook: Workbook) {
        val header: CellStyle = workbook.createCellStyle().apply {
            alignment = HorizontalAlignment.LEFT
            setFont(workbook.createFont().apply { bold = true })
        }
        val label: CellStyle = workbook.createCellStyle().apply {
            alignment = HorizontalAlignment.LEFT
            setFont(workbook.createFont().apply { bold = true })
        }
        val text: CellStyle = workbook.createCellStyle().apply {
            alignment = HorizontalAlignment.LEFT
        }
        val amount: CellStyle = workbook.createCellStyle().apply {
            alignment = HorizontalAlignment.RIGHT
            dataFormat = workbook.creationHelper.createDataFormat().getFormat("#,##0.00")
        }
    }

    companion object {
        private val DATE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    }
}
