package com.bizmanager.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.dp
import com.bizmanager.presentation.navigation.NavigationState
import com.bizmanager.presentation.navigation.Screen

@Composable
fun MainLayout(
    navigationState: NavigationState,
    onNavigate: (Screen) -> Unit,
    content: @Composable () -> Unit
) {
    var sidebarCollapsed by rememberSaveable { mutableStateOf(false) }
    val sidebarWidth by animateDpAsState(if (sidebarCollapsed) 96.dp else 280.dp)

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(18.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Sidebar(
            currentScreen = navigationState.currentScreen,
            collapsed = sidebarCollapsed,
            onToggleCollapse = { sidebarCollapsed = !sidebarCollapsed },
            onNavigate = onNavigate,
            modifier = Modifier.width(sidebarWidth).fillMaxHeight()
        )
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            content()
        }
    }
}

@Composable
private fun Sidebar(
    currentScreen: Screen,
    collapsed: Boolean,
    onToggleCollapse: () -> Unit,
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
                if (collapsed) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "BM",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        IconButton(onClick = onToggleCollapse) {
                            Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Buka sidebar",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                } else {
                    Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "BizManager",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            IconButton(onClick = onToggleCollapse) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Minimize sidebar",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Workspace untuk faktur, piutang, dan insight keuangan harian.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.88f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))
            if (!collapsed) SidebarSection("Operasional")
            SidebarItem("Dashboard", Icons.Default.Dashboard, currentScreen == Screen.Dashboard, collapsed) {
                onNavigate(Screen.Dashboard)
            }
            SidebarItem("Customers", Icons.Default.People, currentScreen in listOf(Screen.CustomerList, Screen.CustomerForm, Screen.CustomerDetail), collapsed) {
                onNavigate(Screen.CustomerList)
            }
            SidebarItem("Products", Icons.Default.Inventory, currentScreen in listOf(Screen.ProductList, Screen.ProductForm, Screen.ProductDetail), collapsed) {
                onNavigate(Screen.ProductList)
            }
            SidebarItem("Faktur", Icons.Default.Receipt, currentScreen in listOf(Screen.InvoiceList, Screen.InvoiceForm, Screen.InvoiceDetail), collapsed) {
                onNavigate(Screen.InvoiceList)
            }
            SidebarItem("Payments", Icons.Default.Payments, currentScreen in listOf(Screen.PaymentList, Screen.PaymentForm), collapsed) {
                onNavigate(Screen.PaymentList)
            }
            SidebarItem("Piutang (Aging)", Icons.Default.AccountBalanceWallet, currentScreen == Screen.ReceivableList, collapsed) {
                onNavigate(Screen.ReceivableList)
            }
            SidebarItem("Rekap Piutang", Icons.Default.AccountBalanceWallet, currentScreen == Screen.CustomerReceivableList, collapsed) {
                onNavigate(Screen.CustomerReceivableList)
            }
            SidebarItem("Buku Besar Customer", Icons.AutoMirrored.Filled.MenuBook, currentScreen == Screen.CustomerLedger, collapsed) {
                onNavigate(Screen.CustomerLedger)
            }

            Spacer(modifier = Modifier.height(18.dp))
            if (!collapsed) SidebarSection("Insight & Sistem")
            SidebarItem("Reports", Icons.Default.Receipt, currentScreen == Screen.ReportPage, collapsed) {
                onNavigate(Screen.ReportPage)
            }
            SidebarItem("Settings", Icons.Default.Settings, currentScreen == Screen.SettingsPage, collapsed) {
                onNavigate(Screen.SettingsPage)
            }
            SidebarItem("Backup & Restore", Icons.Default.Backup, currentScreen == Screen.BackupRestore, collapsed) {
                onNavigate(Screen.BackupRestore)
            }
            SidebarItem("Tentang Aplikasi", Icons.Default.Info, currentScreen == Screen.AboutPage, collapsed) {
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun SidebarItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    collapsed: Boolean,
    onClick: () -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }

    val targetContainerColor = when {
        isSelected && isHovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.13f)
        isHovered -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
        else -> MaterialTheme.colorScheme.surface
    }
    val targetContentColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isHovered -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurface
    }
    val targetIndicatorColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isHovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
        else -> MaterialTheme.colorScheme.surface
    }

    val containerColor by animateColorAsState(targetContainerColor)
    val contentColor by animateColorAsState(targetContentColor)
    val indicatorColor by animateColorAsState(targetIndicatorColor)

    val hoverModifier = Modifier
        .onPointerEvent(PointerEventType.Enter) { isHovered = true }
        .onPointerEvent(PointerEventType.Exit) { isHovered = false }

    if (collapsed) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp)
                .clip(RoundedCornerShape(18.dp))
                .then(hoverModifier)
                .clickable(onClick = onClick)
                .background(containerColor, RoundedCornerShape(18.dp))
                .padding(horizontal = 12.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(4.dp)
                    .height(24.dp)
                    .background(indicatorColor, RoundedCornerShape(999.dp))
            )
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = contentColor
            )
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp)
                .clip(RoundedCornerShape(18.dp))
                .then(hoverModifier)
                .clickable(onClick = onClick)
                .background(containerColor, RoundedCornerShape(18.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(24.dp)
                    .background(indicatorColor, RoundedCornerShape(999.dp))
            )
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
}
