package com.bizmanager.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.bizmanager.presentation.navigation.NavigationState
import com.bizmanager.presentation.navigation.Screen

@Composable
fun MainLayout(
    navigationState: NavigationState,
    onNavigate: (Screen) -> Unit,
    content: @Composable () -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        Sidebar(navigationState.currentScreen, onNavigate)
        Divider(modifier = Modifier.fillMaxHeight().width(1.dp))
        Box(modifier = Modifier.fillMaxSize().weight(1f)) {
            content()
        }
    }
}

@Composable
fun Sidebar(currentScreen: Screen, onNavigate: (Screen) -> Unit) {
    Column(modifier = Modifier.width(250.dp).fillMaxHeight().padding(vertical = 16.dp)) {
        Text(
            text = "BizManager",
            style = MaterialTheme.typography.h5,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        SidebarItem("Dashboard", Icons.Default.Dashboard, currentScreen == Screen.Dashboard) {
            onNavigate(Screen.Dashboard)
        }
        SidebarItem("Customers", Icons.Default.People, currentScreen in listOf(Screen.CustomerList, Screen.CustomerForm, Screen.CustomerDetail)) {
            onNavigate(Screen.CustomerList)
        }
        SidebarItem("Products", Icons.Default.Inventory, currentScreen in listOf(Screen.ProductList, Screen.ProductForm, Screen.ProductDetail)) {
            onNavigate(Screen.ProductList)
        }
        SidebarItem("Invoices", Icons.Default.Receipt, currentScreen in listOf(Screen.InvoiceList, Screen.InvoiceForm, Screen.InvoiceDetail)) {
            onNavigate(Screen.InvoiceList)
        }
        SidebarItem("Payments", Icons.Default.Payments, currentScreen in listOf(Screen.PaymentList, Screen.PaymentForm)) {
            onNavigate(Screen.PaymentList)
        }
        SidebarItem("Receivables (Piutang)", Icons.Default.AccountBalanceWallet, currentScreen == Screen.ReceivableList) {
            onNavigate(Screen.ReceivableList)
        }
        SidebarItem("Reports", Icons.Default.Assessment, currentScreen == Screen.ReportPage) {
            onNavigate(Screen.ReportPage)
        }
        SidebarItem("Settings", Icons.Default.Settings, currentScreen == Screen.SettingsPage) {
            onNavigate(Screen.SettingsPage)
        }
        SidebarItem("Backup & Restore", Icons.Default.Backup, currentScreen == Screen.BackupRestore) {
            onNavigate(Screen.BackupRestore)
        }
    }
}

@Composable
fun SidebarItem(title: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) MaterialTheme.colors.primary.copy(alpha = 0.12f) else MaterialTheme.colors.surface
    val contentColor = if (isSelected) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = contentColor)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, style = MaterialTheme.typography.body1, color = contentColor)
    }
}
