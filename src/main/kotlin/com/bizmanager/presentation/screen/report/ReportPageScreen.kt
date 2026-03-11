package com.bizmanager.presentation.screen.report

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bizmanager.domain.service.ExcelExportService
import com.bizmanager.domain.service.ReportService
import com.bizmanager.domain.service.SalesReport
import com.bizmanager.domain.service.SalesSummary
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
fun ReportPageScreen(reportService: ReportService) {
    // Basic date state
    var startDateStr by remember { mutableStateOf(LocalDateTime.now().withDayOfMonth(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))) }
    var endDateStr by remember { mutableStateOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))) }
    
    var summary by remember { mutableStateOf<SalesSummary?>(null) }
    var latestReport by remember { mutableStateOf<SalesReport?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    val excelExportService = remember { ExcelExportService() }
    
    // In a full implementation there would be tabs for Customer Report, Product Report, etc.
    // Here we focus on the multi-filter summary as the core requirement.
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Laporan Manajemen", style = MaterialTheme.typography.h4)
        Spacer(Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = startDateStr,
                onValueChange = { startDateStr = it },
                label = { Text("Dari Tanggal (YYYY-MM-DD)") },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = endDateStr,
                onValueChange = { endDateStr = it },
                label = { Text("Sampai Tanggal (YYYY-MM-DD)") },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = {
                    try {
                        val start = LocalDateTime.parse("${startDateStr}T00:00:00")
                        val end = LocalDateTime.parse("${endDateStr}T23:59:59")
                        val report = reportService.getSalesReport(start, end)
                        latestReport = report
                        summary = report.summary
                        errorMessage = null
                        successMessage = null
                    } catch (e: Exception) {
                        errorMessage = "Format tanggal salah. Gunakan YYYY-MM-DD"
                        successMessage = null
                    }
                },
                modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically)
            ) {
                Text("Cari")
            }
            Spacer(Modifier.width(8.dp))
            OutlinedButton(
                onClick = {
                    try {
                        val start = LocalDateTime.parse("${startDateStr}T00:00:00")
                        val end = LocalDateTime.parse("${endDateStr}T23:59:59")
                        val report = reportService.getSalesReport(start, end)
                        val outputFile = chooseExcelOutputFile(startDateStr, endDateStr) ?: return@OutlinedButton
                        excelExportService.exportSalesReport(report, outputFile)
                        latestReport = report
                        summary = report.summary
                        successMessage = "File Excel berhasil disimpan: ${outputFile.absolutePath}"
                        errorMessage = null
                    } catch (e: Exception) {
                        errorMessage = e.message ?: "Export Excel gagal diproses"
                        successMessage = null
                    }
                },
                modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically)
            ) {
                Text("Export Excel")
            }
        }
        
        Spacer(Modifier.height(8.dp))
        if (errorMessage != null) {
            Text(errorMessage!!, color = MaterialTheme.colors.error)
        }
        if (successMessage != null) {
            Text(successMessage!!, color = MaterialTheme.colors.primary)
        }
        
        Spacer(Modifier.height(24.dp))
        
        if (summary != null) {
            Card(modifier = Modifier.fillMaxWidth().padding(8.dp), elevation = 4.dp) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Ringkasan Periode: $startDateStr - $endDateStr", style = MaterialTheme.typography.h6)
                    Divider(modifier = Modifier.padding(vertical = 12.dp))
                    
                    ReportRow("Total Omzet Penjualan", summary!!.totalOmzet)
                    ReportRow("Total Laba Kotor", summary!!.totalGrossProfit)
                    ReportRow("Total Laba Bersih", summary!!.totalNetProfit)
                    Divider(modifier = Modifier.padding(vertical = 12.dp))
                    ReportRow("Total Pembayaran Masuk", summary!!.totalPaymentsReceived)
                    ReportRow("Penambahan Piutang Baru", summary!!.totalReceivables)
                }
            }
        } else {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("Silakan klik Cari untuk menampilkan laporan.")
            }
        }
    }
}

private fun chooseExcelOutputFile(startDateStr: String, endDateStr: String): File? {
    val chooser = JFileChooser().apply {
        dialogTitle = "Simpan Laporan Excel"
        fileFilter = FileNameExtensionFilter("Excel Workbook (*.xlsx)", "xlsx")
        selectedFile = File(
            System.getProperty("user.home"),
            "Laporan-BizManager-$startDateStr-sd-$endDateStr.xlsx"
        )
    }

    val result = chooser.showSaveDialog(null)
    if (result != JFileChooser.APPROVE_OPTION) {
        return null
    }

    val selectedFile = chooser.selectedFile ?: return null
    return if (selectedFile.extension.equals("xlsx", ignoreCase = true)) {
        selectedFile
    } else {
        val parent = selectedFile.parentFile ?: File(System.getProperty("user.home"))
        File(parent, "${selectedFile.name}.xlsx")
    }
}

@Composable
fun ReportRow(label: String, amount: java.math.BigDecimal) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.subtitle1)
        Text("Rp ${amount.toPlainString()}", style = MaterialTheme.typography.h6)
    }
}
