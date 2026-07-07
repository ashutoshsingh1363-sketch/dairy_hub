package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.DairyRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

sealed class Screen {
    object Auth : Screen()
    object Dashboard : Screen()
    object Customers : Screen()
    object Collection : Screen()
    object Products : Screen()
    object Sales : Screen()
    object Payments : Screen()
    object Expenses : Screen()
    object Reports : Screen()
    object Settings : Screen()
    object RateChart : Screen()
    object Invoices : Screen()
    object AboutUs : Screen()
}

class DairyViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = DairyRepository(database)

    // Current Screen
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Auth)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Active User
    private val _activeUser = MutableStateFlow<UserEntity?>(null)
    val activeUser: StateFlow<UserEntity?> = _activeUser.asStateFlow()

    private val prefs = application.getSharedPreferences("dairyhub_prefs", android.content.Context.MODE_PRIVATE)

    // App Preferences & Configurations (Theme, App Lock, Printer, SMS, WhatsApp, Language, Center Info)
    private val _isDarkTheme = MutableStateFlow<Boolean>(prefs.getBoolean("isDarkTheme", false))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _appLockEnabled = MutableStateFlow<Boolean>(prefs.getBoolean("appLockEnabled", false))
    val appLockEnabled: StateFlow<Boolean> = _appLockEnabled.asStateFlow()

    private val _printerType = MutableStateFlow<String>(prefs.getString("printerType", "Laser/Inject") ?: "Laser/Inject")
    val printerType: StateFlow<String> = _printerType.asStateFlow()

    private val _smsApp = MutableStateFlow<String>(prefs.getString("smsApp", "Phone Message App") ?: "Phone Message App")
    val smsApp: StateFlow<String> = _smsApp.asStateFlow()

    private val _whatsappSetting = MutableStateFlow<String>(prefs.getString("whatsappSetting", "WhatsApp") ?: "WhatsApp")
    val whatsappSetting: StateFlow<String> = _whatsappSetting.asStateFlow()

    private val _language = MutableStateFlow<String>(prefs.getString("language", "English") ?: "English")
    val language: StateFlow<String> = _language.asStateFlow()

    private val _centerName = MutableStateFlow<String>(prefs.getString("centerName", "Rampur Milk Collection Center") ?: "Rampur Milk Collection Center")
    val centerName: StateFlow<String> = _centerName.asStateFlow()

    private val _centerCode = MutableStateFlow<String>(prefs.getString("centerCode", "MCC-089") ?: "MCC-089")
    val centerCode: StateFlow<String> = _centerCode.asStateFlow()

    private val _printerSize = MutableStateFlow<String>(prefs.getString("printerSize", "58mm") ?: "58mm")
    val printerSize: StateFlow<String> = _printerSize.asStateFlow()

    private val _connectedPrinterName = MutableStateFlow<String>(prefs.getString("connectedPrinterName", "") ?: "")
    val connectedPrinterName: StateFlow<String> = _connectedPrinterName.asStateFlow()

    private val _connectedPrinterAddress = MutableStateFlow<String>(prefs.getString("connectedPrinterAddress", "") ?: "")
    val connectedPrinterAddress: StateFlow<String> = _connectedPrinterAddress.asStateFlow()

    private val _facebookLink = MutableStateFlow<String>(prefs.getString("facebookLink", "https://facebook.com/dairyhub") ?: "https://facebook.com/dairyhub")
    val facebookLink: StateFlow<String> = _facebookLink.asStateFlow()

    private val _instagramLink = MutableStateFlow<String>(prefs.getString("instagramLink", "https://instagram.com/dairyhub") ?: "https://instagram.com/dairyhub")
    val instagramLink: StateFlow<String> = _instagramLink.asStateFlow()

    private val _youtubeLink = MutableStateFlow<String>(prefs.getString("youtubeLink", "https://youtube.com/dairyhub") ?: "https://youtube.com/dairyhub")
    val youtubeLink: StateFlow<String> = _youtubeLink.asStateFlow()

    private val _linkedinLink = MutableStateFlow<String>(prefs.getString("linkedinLink", "https://linkedin.com/company/dairyhub") ?: "https://linkedin.com/company/dairyhub")
    val linkedinLink: StateFlow<String> = _linkedinLink.asStateFlow()

    private val _telegramLink = MutableStateFlow<String>(prefs.getString("telegramLink", "https://t.me/dairyhub") ?: "https://t.me/dairyhub")
    val telegramLink: StateFlow<String> = _telegramLink.asStateFlow()

    private val _supportTime = MutableStateFlow<String>(prefs.getString("supportTime", "09:00 AM - 06:00 PM (Mon - Sat)") ?: "09:00 AM - 06:00 PM (Mon - Sat)")
    val supportTime: StateFlow<String> = _supportTime.asStateFlow()

    fun updateSocialLinksAndSupportTime(
        facebook: String,
        instagram: String,
        youtube: String,
        linkedin: String,
        telegram: String,
        support: String
    ) {
        _facebookLink.value = facebook
        _instagramLink.value = instagram
        _youtubeLink.value = youtube
        _linkedinLink.value = linkedin
        _telegramLink.value = telegram
        _supportTime.value = support
        prefs.edit()
            .putString("facebookLink", facebook)
            .putString("instagramLink", instagram)
            .putString("youtubeLink", youtube)
            .putString("linkedinLink", linkedin)
            .putString("telegramLink", telegram)
            .putString("supportTime", support)
            .apply()
    }

    fun toggleTheme() {
        val newVal = !_isDarkTheme.value
        _isDarkTheme.value = newVal
        prefs.edit().putBoolean("isDarkTheme", newVal).apply()
    }

    fun setDarkTheme(enabled: Boolean) {
        _isDarkTheme.value = enabled
        prefs.edit().putBoolean("isDarkTheme", enabled).apply()
    }

    fun setAppLockEnabled(enabled: Boolean) {
        _appLockEnabled.value = enabled
        prefs.edit().putBoolean("appLockEnabled", enabled).apply()
    }

    fun setPrinterType(type: String) {
        _printerType.value = type
        prefs.edit().putString("printerType", type).apply()
    }

    fun setSmsApp(app: String) {
        _smsApp.value = app
        prefs.edit().putString("smsApp", app).apply()
    }

    fun setWhatsappSetting(setting: String) {
        _whatsappSetting.value = setting
        prefs.edit().putString("whatsappSetting", setting).apply()
    }

    fun setLanguage(lang: String) {
        _language.value = lang
        // Update the live translation state immediately so every screen using L.t()
        // recomposes right away, instead of only picking up the new language after
        // the app is restarted (previously this only synced once, in init{}).
        com.example.ui.screens.L.currentLang = lang
        prefs.edit().putString("language", lang).apply()
        com.example.ui.screens.L.currentLang = lang
    }

    fun setPrinterSize(size: String) {
        _printerSize.value = size
        prefs.edit().putString("printerSize", size).apply()
    }

    fun connectPrinter(name: String, address: String) {
        _connectedPrinterName.value = name
        _connectedPrinterAddress.value = address
        prefs.edit().putString("connectedPrinterName", name).putString("connectedPrinterAddress", address).apply()
    }

    fun disconnectPrinter() {
        _connectedPrinterName.value = ""
        _connectedPrinterAddress.value = ""
        prefs.edit().remove("connectedPrinterName").remove("connectedPrinterAddress").apply()
    }

    fun updateCenterInfo(name: String, code: String) {
        _centerName.value = name
        _centerCode.value = code
        prefs.edit().putString("centerName", name).putString("centerCode", code).apply()
    }

    // Screen State / Back Stack
    private val screenHistory = mutableListOf<Screen>()

    fun navigateTo(screen: Screen) {
        if (_currentScreen.value != screen) {
            screenHistory.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack() {
        if (screenHistory.isNotEmpty()) {
            _currentScreen.value = screenHistory.removeAt(screenHistory.size - 1)
        }
    }

    // Flows from Repository
    val allUsers = repository.allUsers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allCustomers = repository.allCustomers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allCollections = repository.allCollections.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allProducts = repository.allProducts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allInvoices = repository.allInvoices.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allPayments = repository.allPayments.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allExpenses = repository.allExpenses.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allNotifications = repository.allNotifications.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val unreadNotifications = repository.unreadNotifications.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val recentActivityLogs = repository.recentActivityLogs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allRateCharts = repository.allRateCharts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val dairyProfile = repository.dairyProfile.stateIn(viewModelScope, SharingStarted.Eagerly, DairyProfileEntity())

    // UI state states
    var authError = MutableStateFlow<String?>(null)
    var successMessage = MutableStateFlow<String?>(null)

    init {
        // Sync the static Language Helper current language with SharedPreferences saved value
        com.example.ui.screens.L.currentLang = _language.value

        // Sync the dynamic Dairy Profile with local States & SharedPreferences
        viewModelScope.launch {
            repository.dairyProfile.collect { profile ->
                if (profile != null) {
                    _centerName.value = profile.dairyName
                    _centerCode.value = profile.upiId
                    prefs.edit().putString("centerName", profile.dairyName).putString("centerCode", profile.upiId).apply()
                }
            }
        }

        // Check for remembered user on startup
        viewModelScope.launch {
            val user = repository.getRememberedUser()
            if (user != null) {
                _activeUser.value = user
                _currentScreen.value = Screen.Dashboard
                repository.logActivity(user.role, user.username, "Automatic login session restored via Remember Me")
            }
        }
    }

    fun updateDairyProfile(
        dairyName: String,
        ownerName: String,
        contactNumber: String,
        emailAddress: String,
        address: String,
        gstNumber: String,
        panNumber: String,
        logo: String,
        signature: String,
        website: String,
        upiId: String
    ) {
        viewModelScope.launch {
            val updated = DairyProfileEntity(
                id = 1,
                dairyName = dairyName,
                ownerName = ownerName,
                contactNumber = contactNumber,
                emailAddress = emailAddress,
                address = address,
                gstNumber = gstNumber,
                panNumber = panNumber,
                logo = logo,
                signature = signature,
                website = website,
                upiId = upiId
            )
            repository.updateDairyProfile(updated)
            repository.logActivity(activeUser.value?.role ?: "Admin", activeUser.value?.username ?: "admin", "Updated Dairy Profile settings: $dairyName")
        }
    }

    // AUTHENTICATION
    fun login(username: String, password: String, rememberMe: Boolean) {
        viewModelScope.launch {
            authError.value = null
            val user = repository.getUserByUsername(username)
            if (user == null) {
                authError.value = "Invalid username or password"
                return@launch
            }
            val passwordMatches = if (com.example.data.security.PasswordHasher.isHashed(user.passwordHash)) {
                com.example.data.security.PasswordHasher.verify(password, user.passwordHash)
            } else {
                // Legacy plaintext account (from before this fix). Verify against the
                // old raw value, then transparently upgrade it to a salted hash so it
                // is never stored in plaintext again.
                val matches = user.passwordHash == password
                if (matches) {
                    repository.updateUserPasswordHash(user.id, com.example.data.security.PasswordHasher.hash(password))
                }
                matches
            }
            if (passwordMatches) {
                _activeUser.value = user
                repository.setRememberUser(user.id, rememberMe)
                _currentScreen.value = Screen.Dashboard
                repository.logActivity(user.role, user.username, "User logged in successfully")
            } else {
                authError.value = "Invalid username or password"
            }
        }
    }

    fun register(username: String, password: String, fullName: String, role: String, phone: String, email: String) {
        viewModelScope.launch {
            authError.value = null
            val existing = repository.getUserByUsername(username)
            if (existing != null) {
                authError.value = "Username already exists"
                return@launch
            }
            val newUser = UserEntity(
                username = username,
                passwordHash = com.example.data.security.PasswordHasher.hash(password),
                fullName = fullName,
                role = role,
                phone = phone,
                email = email
            )
            repository.registerUser(newUser)
            successMessage.value = "Account created! Please log in."
        }
    }

    fun logout() {
        viewModelScope.launch {
            val user = _activeUser.value
            if (user != null) {
                repository.setRememberUser(user.id, false)
                repository.logActivity(user.role, user.username, "User logged out")
            }
            _activeUser.value = null
            _currentScreen.value = Screen.Auth
            screenHistory.clear()
        }
    }

    // CUSTOMERS
    fun addCustomer(
        name: String,
        phone: String,
        address: String,
        code: String = "",
        email: String = "",
        milkType: String = "Both",
        bankName: String = "",
        branch: String = "",
        accountNumber: String = "",
        ifsc: String = "",
        openingBalance: Double = 0.0,
        customerType: String = "Farmer",
        paymentMode: String = "Cash",
        isActive: Boolean = true
    ) {
        viewModelScope.launch {
            val customer = CustomerEntity(
                name = name,
                phone = phone,
                address = address,
                code = code,
                email = email,
                milkType = milkType,
                bankName = bankName,
                branch = branch,
                accountNumber = accountNumber,
                ifsc = ifsc,
                balance = openingBalance,
                customerType = customerType,
                paymentMode = paymentMode,
                isActive = isActive
            )
            repository.addCustomer(customer, _activeUser.value)
        }
    }

    fun updateCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.updateCustomer(customer, _activeUser.value)
        }
    }

    fun deleteCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.deleteCustomer(customer, _activeUser.value)
        }
    }

    // MILK COLLECTION RATE CALCULATION
    fun calculateRate(fat: Double, snf: Double): Double {
        return getClosestRateFromChart("Cow", fat, snf).first
    }

    fun getClosestRateFromChart(milkType: String, fat: Double, snf: Double): Pair<Double, Boolean> {
        val charts = allRateCharts.value.filter {
            it.milkType.equals(milkType, ignoreCase = true) && it.isActive
        }
        if (charts.isEmpty()) {
            val formulaRate = calculateRateFormula(milkType, fat, snf)
            return Pair(formulaRate, false) // false means not found in chart, using formula fallback
        }
        
        // Try exact match
        val exact = charts.find { it.fat == fat && it.snf == snf }
        if (exact != null) {
            return Pair(exact.rate, true) // true means exact match
        }

        // Try closest match
        var closestRate = 0.0
        var minDiff = Double.MAX_VALUE
        var found = false
        for (row in charts) {
            val diff = Math.abs(row.fat - fat) + Math.abs(row.snf - snf)
            if (diff < minDiff) {
                minDiff = diff
                closestRate = row.rate
                found = true
            }
        }

        if (found && minDiff <= 0.61) {
            return Pair(closestRate, true) // Closest match within tolerance
        }

        // Return formula fallback if no match is reasonably close
        return Pair(calculateRateFormula(milkType, fat, snf), false)
    }

    private fun calculateRateFormula(milkType: String, fat: Double, snf: Double): Double {
        val factorFat = if (milkType.equals("Cow", ignoreCase = true)) 6.5 else 7.2
        val factorSnf = if (milkType.equals("Cow", ignoreCase = true)) 3.8 else 4.2
        val computed = (fat * factorFat) + (snf * factorSnf)
        return String.format("%.2f", if (computed < 10.0) 10.0 else computed).toDouble()
    }

    fun addMilkCollection(customerId: Long, shift: String, fat: Double, snf: Double, clr: Double, quantity: Double, milkType: String = "Cow") {
        viewModelScope.launch {
            val rateResult = getClosestRateFromChart(milkType, fat, snf)
            val rate = rateResult.first
            val amount = String.format("%.2f", quantity * rate).toDouble()
            val collection = MilkCollectionEntity(
                customerId = customerId,
                shift = shift,
                fat = fat,
                snf = snf,
                clr = clr,
                quantity = quantity,
                rate = rate,
                amount = amount,
                milkType = milkType
            )
            repository.addCollection(collection, _activeUser.value)
        }
    }

    // RATE CHART CRUD ACTIONS
    fun addRate(milkType: String, fat: Double, snf: Double, rate: Double, isActive: Boolean = true) {
        viewModelScope.launch {
            repository.addRate(RateChartEntity(milkType = milkType, fat = fat, snf = snf, rate = rate, isActive = isActive))
            repository.logActivity(
                activeUser.value?.role ?: "Manager",
                activeUser.value?.username ?: "system",
                "Added rate: $milkType FAT: $fat SNF: $snf Rate: $rate"
            )
        }
    }

    fun updateRate(rateChart: RateChartEntity) {
        viewModelScope.launch {
            repository.updateRate(rateChart)
            repository.logActivity(
                activeUser.value?.role ?: "Manager",
                activeUser.value?.username ?: "system",
                "Updated rate #${rateChart.id}: ${rateChart.milkType} FAT: ${rateChart.fat} SNF: ${rateChart.snf} Rate: ${rateChart.rate}"
            )
        }
    }

    fun deleteRate(rateChart: RateChartEntity) {
        viewModelScope.launch {
            repository.deleteRate(rateChart)
            repository.logActivity(
                activeUser.value?.role ?: "Manager",
                activeUser.value?.username ?: "system",
                "Deleted rate #${rateChart.id}"
            )
        }
    }

    fun clearRateChart(milkType: String) {
        viewModelScope.launch {
            repository.clearRateChart(milkType)
            repository.logActivity(
                activeUser.value?.role ?: "Manager",
                activeUser.value?.username ?: "system",
                "Cleared all rates for $milkType"
            )
        }
    }

    fun importRatesFromCsv(milkType: String, csvText: String): Boolean {
        try {
            val lines = csvText.lines()
            val ratesList = mutableListOf<RateChartEntity>()
            for (line in lines) {
                if (line.isBlank()) continue
                val parts = line.split(",")
                if (parts.size < 3) continue
                if (parts[0].trim().equals("fat", ignoreCase = true) || parts[0].trim().equals("snf", ignoreCase = true)) {
                    continue
                }
                val fatVal = parts[0].trim().toDoubleOrNull()
                val snfVal = parts[1].trim().toDoubleOrNull()
                val rateVal = parts[2].trim().toDoubleOrNull()
                if (fatVal != null && snfVal != null && rateVal != null) {
                    ratesList.add(RateChartEntity(
                        milkType = milkType,
                        fat = fatVal,
                        snf = snfVal,
                        rate = rateVal,
                        isActive = true
                    ))
                }
            }
            if (ratesList.isNotEmpty()) {
                viewModelScope.launch {
                    repository.addRates(ratesList)
                    repository.logActivity(
                        activeUser.value?.role ?: "Manager",
                        activeUser.value?.username ?: "system",
                        "Imported ${ratesList.size} rates for $milkType from CSV template"
                    )
                }
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun updateCollection(collection: MilkCollectionEntity) {
        viewModelScope.launch {
            repository.updateCollection(collection, _activeUser.value)
        }
    }

    fun deleteCollection(collection: MilkCollectionEntity) {
        viewModelScope.launch {
            repository.removeCollection(collection, _activeUser.value)
        }
    }

    // PRODUCT INVENTORY
    fun addProduct(name: String, category: String, price: Double, stock: Double, unit: String) {
        viewModelScope.launch {
            val product = ProductEntity(name = name, category = category, price = price, stockQty = stock, unit = unit)
            repository.addProduct(product, _activeUser.value)
        }
    }

    fun updateProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.updateProduct(product, _activeUser.value)
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product, _activeUser.value)
        }
    }

    fun restockProduct(productId: Long, quantity: Double) {
        viewModelScope.launch {
            repository.adjustProductStock(productId, quantity)
        }
    }

    // SALES & BILLING DRAFT STATE
    private val _draftItems = MutableStateFlow<List<InvoiceItemEntity>>(emptyList())
    val draftItems: StateFlow<List<InvoiceItemEntity>> = _draftItems.asStateFlow()

    fun addProductToDraft(product: ProductEntity, quantity: Double) {
        val existingIndex = _draftItems.value.indexOfFirst { it.productId == product.id }
        if (existingIndex >= 0) {
            val list = _draftItems.value.toMutableList()
            val oldItem = list[existingIndex]
            val newQty = oldItem.quantity + quantity
            list[existingIndex] = oldItem.copy(
                quantity = newQty,
                amount = String.format("%.2f", newQty * oldItem.price).toDouble()
            )
            _draftItems.value = list
        } else {
            _draftItems.value = _draftItems.value + InvoiceItemEntity(
                invoiceId = 0L,
                productId = product.id,
                productName = product.name,
                quantity = quantity,
                price = product.price,
                amount = String.format("%.2f", quantity * product.price).toDouble()
            )
        }
    }

    fun removeProductFromDraft(draftItem: InvoiceItemEntity) {
        _draftItems.value = _draftItems.value.filterNot { it.productId == draftItem.productId }
    }

    fun clearDraft() {
        _draftItems.value = emptyList()
    }

    fun checkoutInvoice(customerId: Long, customerName: String, discountPercent: Double, gstPercent: Double, isPaid: Boolean) {
        viewModelScope.launch {
            val items = _draftItems.value
            if (items.isEmpty()) return@launch

            val subtotal = items.sumOf { it.amount }
            val discountAmount = (subtotal * discountPercent) / 100.0
            val gstAmount = ((subtotal - discountAmount) * gstPercent) / 100.0
            val total = String.format("%.2f", subtotal - discountAmount + gstAmount).toDouble()

            val invNum = "INV-${System.currentTimeMillis().toString().takeLast(6)}"
            val qrContent = "https://dairyhub.net/pay?invoice=$invNum&amount=$total"

            val invoice = InvoiceEntity(
                customerId = customerId,
                customerName = customerName,
                subtotal = subtotal,
                discount = discountAmount,
                gst = gstAmount,
                totalAmount = total,
                paymentStatus = if (isPaid) "Paid" else "Pending",
                invoiceNumber = invNum,
                barcodeData = invNum,
                qrData = qrContent
            )

            repository.createInvoice(invoice, items, _activeUser.value)
            clearDraft()
        }
    }

    // PAYMENTS
    fun receivePayment(customerId: Long, amount: Double, method: String, notes: String) {
        viewModelScope.launch {
            val payment = PaymentEntity(
                customerId = customerId,
                amountReceived = amount,
                paymentMethod = method,
                notes = notes,
                transactionId = "TXN-${UUID.randomUUID().toString().take(8).uppercase()}"
            )
            repository.addPayment(payment, _activeUser.value)
        }
    }

    // EXPENSES
    fun addExpense(category: String, amount: Double, description: String) {
        viewModelScope.launch {
            val expense = ExpenseEntity(
                category = category,
                amount = amount,
                description = description
            )
            repository.addExpense(expense, _activeUser.value)
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.removeExpense(expense, _activeUser.value)
        }
    }

    // NOTIFICATIONS
    fun markNotificationRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
        }
    }

    // RAW MYSQL 8 DDL SCHEMA GENERATOR
    val mysqlSchemaSql: String = """
-- ======================================================
-- DAIRYHUB - NORMALIZED MYSQL 8 DATABASE SCHEMA
-- Generated automatically by DairyHub Enterprise Core
-- ======================================================

CREATE DATABASE IF NOT EXISTS dairyhub_db;
USE dairyhub_db;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(30) NOT NULL, -- Owner, Manager, Staff, Collector, Accountant, Customer
    phone VARCHAR(20),
    email VARCHAR(100),
    joined_date BIGINT NOT NULL,
    is_remembered BOOLEAN DEFAULT FALSE,
    INDEX idx_users_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Customers Table
CREATE TABLE IF NOT EXISTS customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    address TEXT,
    joined_date BIGINT NOT NULL,
    balance DOUBLE DEFAULT 0.0,
    INDEX idx_customers_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Products Table
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    price DOUBLE NOT NULL,
    stock_qty DOUBLE DEFAULT 0.0,
    unit VARCHAR(10) NOT NULL,
    INDEX idx_products_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Milk Collections Table (Farmer Supplies)
CREATE TABLE IF NOT EXISTS milk_collections (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    shift VARCHAR(10) NOT NULL, -- Morning, Evening
    fat DOUBLE NOT NULL,
    snf DOUBLE NOT NULL,
    clr DOUBLE NOT NULL,
    quantity DOUBLE NOT NULL,
    rate DOUBLE NOT NULL,
    amount DOUBLE NOT NULL,
    timestamp BIGINT NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    INDEX idx_collection_customer (customer_id),
    INDEX idx_collection_date (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Invoices Table (Product Sales)
CREATE TABLE IF NOT EXISTS invoices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    customer_name VARCHAR(100) NOT NULL,
    subtotal DOUBLE NOT NULL,
    discount DOUBLE NOT NULL,
    gst DOUBLE NOT NULL,
    total_amount DOUBLE NOT NULL,
    payment_status VARCHAR(20) NOT NULL, -- Paid, Pending
    timestamp BIGINT NOT NULL,
    invoice_number VARCHAR(20) NOT NULL UNIQUE,
    barcode_data VARCHAR(100),
    qr_data TEXT,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE RESTRICT,
    INDEX idx_invoices_customer (customer_id),
    INDEX idx_invoices_number (invoice_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Invoice Items Table
CREATE TABLE IF NOT EXISTS invoice_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    quantity DOUBLE NOT NULL,
    price DOUBLE NOT NULL,
    amount DOUBLE NOT NULL,
    FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT,
    INDEX idx_invoice_items_invoice (invoice_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. Payments Table (Payments received from buyers)
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    amount_received DOUBLE NOT NULL,
    date_millis BIGINT NOT NULL,
    payment_method VARCHAR(30) NOT NULL, -- Cash, UPI, Card, Bank
    notes VARCHAR(255),
    transaction_id VARCHAR(50) NOT NULL UNIQUE,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    INDEX idx_payments_customer (customer_id),
    INDEX idx_payments_date (date_millis)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. Expenses Table
CREATE TABLE IF NOT EXISTS expenses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category VARCHAR(50) NOT NULL, -- Salary, Fuel, Power, Maintenance, Feed, Others
    amount DOUBLE NOT NULL,
    description VARCHAR(255),
    date_millis BIGINT NOT NULL,
    INDEX idx_expenses_date (date_millis)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9. Notifications Table
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    date_millis BIGINT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 10. Activity Logs Table (Security Auditing)
CREATE TABLE IF NOT EXISTS activity_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_role VARCHAR(50) NOT NULL,
    username VARCHAR(50) NOT NULL,
    action TEXT NOT NULL,
    timestamp BIGINT NOT NULL,
    INDEX idx_activity_time (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ======================================================
-- SEED DATA & INITIAL SETUP
-- ======================================================

INSERT INTO users (username, password_hash, full_name, role, phone, email, joined_date) VALUES
('owner', 'owner123', 'Amit Kumar (Owner)', 'Owner', '9876543210', 'owner@dairyhub.com', 1700000000000),
('manager', 'manager123', 'Rajesh Sharma (Manager)', 'Manager', '9876543211', 'manager@dairyhub.com', 1700000000000),
('staff', 'staff123', 'Suresh Patel (Staff)', 'Staff', '9876543212', 'staff@dairyhub.com', 1700000000000),
('collector', 'collector123', 'Vijay Yadav (Collector)', 'Milk Collector', '9876543213', 'collector@dairyhub.com', 1700000000000),
('accountant', 'accountant123', 'Anil Gupta (Accountant)', 'Accountant', '9876543214', 'accountant@dairyhub.com', 1700000000000);

INSERT INTO customers (name, phone, address, joined_date, balance) VALUES
('Ramesh Singh', '9898012345', 'Village Rampur, Sector 1', 1700000000000, 1250.0),
('Sita Devi', '9898054321', 'Village Rampur, Sector 2', 1700000000000, 0.0),
('Mahendra Yadav', '9898123456', 'Yadav Farm, Ward 5', 1700000000000, 3400.0),
('Sunita Sharma', '9898654321', 'Main Bazaar, Gali No. 3', 1700000000000, -150.0),
('Gopal Das', '9898321456', 'Village Rampur, Sector 4', 1700000000000, 850.0);

INSERT INTO products (name, category, price, stock_qty, unit) VALUES
('Standard Milk', 'Dairy', 60.0, 450.0, 'Litre'),
('Fresh Paneer', 'Dairy', 360.0, 42.0, 'Kg'),
('Creamy Curd', 'Dairy', 80.0, 95.0, 'Kg'),
('Cow Ghee', 'Dairy', 680.0, 35.0, 'Kg'),
('Salted Butter', 'Dairy', 440.0, 20.0, 'Kg'),
('Sweet Lassi', 'Dairy', 40.0, 150.0, 'Packet'),
('Mozzarella Cheese', 'Dairy', 480.0, 18.0, 'Kg');
    """.trimIndent()

    // Mock REST API payload loggers for showing real integration
    private val _apiLogs = MutableStateFlow<List<String>>(
        listOf(
            "[GET] /api/v1/auth/me - HTTP 200 OK - Active user fetched",
            "[GET] /api/v1/products - HTTP 200 OK - 7 Products synced",
            "[GET] /api/v1/customers - HTTP 200 OK - 5 Customers synced"
        )
    )
    val apiLogs: StateFlow<List<String>> = _apiLogs.asStateFlow()

    suspend fun getInvoiceItems(invoiceId: Long): List<InvoiceItemEntity> {
        return repository.getInvoiceItems(invoiceId)
    }

    fun logApiCall(method: String, endpoint: String, requestBody: String? = null, responseBody: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        val logLine = "[$timestamp] $method $endpoint\nReq: ${requestBody ?: "None"}\nRes: $responseBody"
        _apiLogs.value = (listOf(logLine) + _apiLogs.value).take(50)
    }
}
