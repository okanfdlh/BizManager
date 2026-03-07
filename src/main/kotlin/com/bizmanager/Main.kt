package com.bizmanager

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.bizmanager.data.database.DatabaseConfig
import com.bizmanager.data.repository.*
import com.bizmanager.domain.service.*
import com.bizmanager.presentation.MainLayout
import com.bizmanager.presentation.navigation.NavController
import com.bizmanager.presentation.navigation.Screen
import com.bizmanager.presentation.screen.backup.BackupRestoreScreen
import com.bizmanager.presentation.screen.customer.CustomerDetailScreen
import com.bizmanager.presentation.screen.customer.CustomerFormScreen
import com.bizmanager.presentation.screen.customer.CustomerListScreen
import com.bizmanager.presentation.screen.dashboard.DashboardScreen
import com.bizmanager.presentation.screen.invoice.InvoiceDetailScreen
import com.bizmanager.presentation.screen.invoice.InvoiceFormScreen
import com.bizmanager.presentation.screen.invoice.InvoiceListScreen
import com.bizmanager.presentation.screen.payment.PaymentFormScreen
import com.bizmanager.presentation.screen.payment.PaymentListScreen
import com.bizmanager.presentation.screen.product.ProductDetailScreen
import com.bizmanager.presentation.screen.product.ProductFormScreen
import com.bizmanager.presentation.screen.product.ProductListScreen
import com.bizmanager.presentation.screen.receivable.ReceivableListScreen
import com.bizmanager.presentation.screen.report.ReportPageScreen
import com.bizmanager.presentation.screen.settings.SettingsScreen

@Composable
@Preview
fun App(
    reportService: ReportService,
    customerRepository: CustomerRepository,
    productRepository: ProductRepository,
    invoiceRepository: InvoiceRepository,
    paymentRepository: PaymentRepository,
    appSettingRepository: AppSettingRepository,
    invoiceService: InvoiceService,
    paymentService: PaymentService
) {
    val navController = remember { NavController() }
    var currentScreen by remember { mutableStateOf(navController.state.currentScreen) }
    var selectedId by remember { mutableStateOf(navController.state.selectedId) }
    
    LaunchedEffect(navController) {
        navController.addListener { state ->
            currentScreen = state.currentScreen
            selectedId = state.selectedId
        }
    }

    MaterialTheme {
        MainLayout(
            navigationState = navController.state,
            onNavigate = { screen -> navController.navigate(screen) }
        ) {
            when (currentScreen) {
                Screen.Dashboard -> DashboardScreen(
                    reportService = reportService,
                    onNavigateToInvoices = { navController.navigate(Screen.InvoiceList) },
                    onNavigateToReceivables = { navController.navigate(Screen.ReceivableList) }
                )
                Screen.CustomerList -> CustomerListScreen(customerRepository, { id -> navController.navigate(Screen.CustomerForm, id) }, { id -> navController.navigate(Screen.CustomerDetail, id) })
                Screen.CustomerForm -> CustomerFormScreen(selectedId, customerRepository, { navController.navigate(Screen.CustomerList) })
                Screen.CustomerDetail -> CustomerDetailScreen(selectedId ?: 0, customerRepository, invoiceRepository, { navController.navigate(Screen.CustomerList) })
                
                Screen.ProductList -> ProductListScreen(productRepository, { id -> navController.navigate(Screen.ProductForm, id) }, { id -> navController.navigate(Screen.ProductDetail, id) })
                Screen.ProductForm -> ProductFormScreen(selectedId, productRepository, { navController.navigate(Screen.ProductList) })
                Screen.ProductDetail -> ProductDetailScreen(selectedId ?: 0, productRepository, { navController.navigate(Screen.ProductList) })
                
                Screen.InvoiceList -> InvoiceListScreen(invoiceRepository, customerRepository, { id -> navController.navigate(Screen.InvoiceForm, id) }, { id -> navController.navigate(Screen.InvoiceDetail, id) })
                Screen.InvoiceForm -> InvoiceFormScreen(selectedId, invoiceService, customerRepository, productRepository, { navController.navigate(Screen.InvoiceList) })
                Screen.InvoiceDetail -> InvoiceDetailScreen(selectedId ?: 0, invoiceRepository, customerRepository, paymentRepository, invoiceService, { id -> navController.navigate(Screen.PaymentForm, id) }, { navController.navigate(Screen.InvoiceList) })
                
                Screen.PaymentList -> PaymentListScreen(paymentRepository, invoiceRepository)
                Screen.PaymentForm -> PaymentFormScreen(selectedId, invoiceRepository, paymentService, { navController.navigate(Screen.PaymentList) })
                
                Screen.ReceivableList -> ReceivableListScreen(invoiceRepository, customerRepository)
                Screen.ReportPage -> ReportPageScreen(reportService)
                Screen.SettingsPage -> SettingsScreen(appSettingRepository)
                Screen.BackupRestore -> BackupRestoreScreen(appSettingRepository)
            }
        }
    }
}

fun main() {
    try {
        DatabaseConfig.init()
    } catch (e: Exception) {
        System.err.println("Failed to initialize database: ${e.message}")
        e.printStackTrace()
    }
    
    // Wire dependencies
    val customerRepository = CustomerRepository()
    val productRepository = ProductRepository()
    val invoiceRepository = InvoiceRepository()
    val paymentRepository = PaymentRepository()
    val appSettingRepository = AppSettingRepository()
    val logRepository = ActivityLogRepository()
    
    val documentNumberGenerator = DocumentNumberGenerator(appSettingRepository)
    val invoiceService = InvoiceService(invoiceRepository, productRepository, documentNumberGenerator)
    val paymentService = PaymentService(paymentRepository, invoiceRepository, documentNumberGenerator)
    val reportService = ReportService()

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "BizManager",
        ) {
            App(
                reportService,
                customerRepository,
                productRepository,
                invoiceRepository,
                paymentRepository,
                appSettingRepository,
                invoiceService,
                paymentService
            )
        }
    }
}


