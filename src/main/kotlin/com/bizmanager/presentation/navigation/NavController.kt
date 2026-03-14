package com.bizmanager.presentation.navigation

enum class Screen {
    Dashboard,
    CustomerList,
    CustomerForm,
    CustomerDetail,
    ProductList,
    ProductForm,
    ProductDetail,
    InvoiceList,
    InvoiceForm,
    InvoiceDetail,
    PaymentList,
    PaymentForm,
    ReceivableList,
    CustomerReceivableList,
    CustomerLedger,
    ReportPage,
    SettingsPage,
    BackupRestore,
    AboutPage
}

data class NavigationState(
    val currentScreen: Screen = Screen.Dashboard,
    val selectedId: Int? = null // Generic ID passing between lists and details/forms
)

class NavController {
    var state: NavigationState = NavigationState()
        private set
    
    // In a real compose app, this would be wrapped in a MutableState or similar Flow, 
    // but we will manage state lifting in the root Composable.
    private val listeners = mutableListOf<(NavigationState) -> Unit>()

    fun navigate(screen: Screen, id: Int? = null) {
        state = state.copy(currentScreen = screen, selectedId = id)
        listeners.forEach { it(state) }
    }

    fun addListener(listener: (NavigationState) -> Unit) {
        listeners.add(listener)
    }
}
