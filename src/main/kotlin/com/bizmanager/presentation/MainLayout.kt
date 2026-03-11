package com.bizmanager.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(18.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Sidebar(
            currentScreen = navigationState.currentScreen,
            onNavigate = onNavigate,
            modifier = Modifier.width(280.dp).fillMaxHeight()
        )
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            content()
        }
    }
}

@Composable
private fun Sidebar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shadowElevation = 12.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(18.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                    Text(
                        text = "BizManager",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Workspace untuk invoice, piutang, dan insight keuangan harian.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.88f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))
            SidebarSection("Operasional")
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
            SidebarItem("Piutang", Icons.Default.AccountBalanceWallet, currentScreen == Screen.ReceivableList) {
                onNavigate(Screen.ReceivableList)
            }
            SidebarItem("Buku Besar Customer", Icons.AutoMirrored.Filled.MenuBook, currentScreen == Screen.CustomerLedger) {
                onNavigate(Screen.CustomerLedger)
            }

            Spacer(modifier = Modifier.height(18.dp))
            SidebarSection("Insight & Sistem")
            SidebarItem("Reports", Icons.Default.Receipt, currentScreen == Screen.ReportPage) {
                onNavigate(Screen.ReportPage)
            }
            SidebarItem("Settings", Icons.Default.Settings, currentScreen == Screen.SettingsPage) {
                onNavigate(Screen.SettingsPage)
            }
            SidebarItem("Backup & Restore", Icons.Default.Backup, currentScreen == Screen.BackupRestore) {
                onNavigate(Screen.BackupRestore)
            }
            SidebarItem("Tentang Aplikasi", Icons.Default.Info, currentScreen == Screen.AboutPage) {
                onNavigate(Screen.AboutPage)
            }
        }
    }
}

@Composable
private fun SidebarSection(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
    )
}

@Composable
private fun SidebarItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clickable(onClick = onClick)
            .background(containerColor, RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = contentColor
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = contentColor
        )
    }
}
