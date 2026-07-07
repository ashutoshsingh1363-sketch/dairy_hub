package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

class DairyRepository(private val db: AppDatabase) {
    private val userDao = db.userDao()
    private val customerDao = db.customerDao()
    private val collectionDao = db.collectionDao()
    private val productDao = db.productDao()
    private val invoiceDao = db.invoiceDao()
    private val paymentDao = db.paymentDao()
    private val expenseDao = db.expenseDao()
    private val notificationDao = db.notificationDao()
    private val activityLogDao = db.activityLogDao()
    private val rateChartDao = db.rateChartDao()
    private val dairyProfileDao = db.dairyProfileDao()

    // Flow listings
    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsersFlow()
    val allCustomers: Flow<List<CustomerEntity>> = customerDao.getAllCustomersFlow()
    val allCollections: Flow<List<MilkCollectionEntity>> = collectionDao.getAllCollectionsFlow()
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProductsFlow()
    val allInvoices: Flow<List<InvoiceEntity>> = invoiceDao.getAllInvoicesFlow()
    val allPayments: Flow<List<PaymentEntity>> = paymentDao.getAllPaymentsFlow()
    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpensesFlow()
    val allNotifications: Flow<List<NotificationEntity>> = notificationDao.getAllNotificationsFlow()
    val unreadNotifications: Flow<List<NotificationEntity>> = notificationDao.getUnreadNotificationsFlow()
    val recentActivityLogs: Flow<List<ActivityLogEntity>> = activityLogDao.getRecentLogsFlow()
    val allRateCharts: Flow<List<RateChartEntity>> = rateChartDao.getAllRateChartsFlow()
    val dairyProfile: Flow<DairyProfileEntity?> = dairyProfileDao.getProfileFlow()

    // USER OPERATIONS
    suspend fun getUserByUsername(username: String): UserEntity? {
        return userDao.getUserByUsername(username)
    }

    suspend fun getRememberedUser(): UserEntity? {
        return userDao.getRememberedUser()
    }

    suspend fun registerUser(user: UserEntity): Long {
        val id = userDao.insertUser(user)
        logActivity(user.role, user.username, "Registered new account: ${user.fullName}")
        return id
    }

    suspend fun updateUser(user: UserEntity) {
        userDao.updateUser(user)
    }

    suspend fun setRememberUser(userId: Long, remember: Boolean) {
        userDao.updateRememberMe(userId, remember)
    }

    suspend fun updateUserPasswordHash(userId: Long, passwordHash: String) {
        userDao.updatePasswordHash(userId, passwordHash)
    }

    suspend fun checkUserExists(): Boolean {
        return userDao.getUserCount() > 0
    }

    // CUSTOMER OPERATIONS
    suspend fun getAllCustomersList(): List<CustomerEntity> {
        return customerDao.getAllCustomers()
    }

    suspend fun getCustomerById(id: Long): CustomerEntity? {
        return customerDao.getCustomerById(id)
    }

    fun searchCustomers(query: String): Flow<List<CustomerEntity>> {
        return customerDao.searchCustomersFlow("%$query%")
    }

    suspend fun addCustomer(customer: CustomerEntity, activeUser: UserEntity?): Long {
        val id = customerDao.insertCustomer(customer)
        logActivity(
            activeUser?.role ?: "Staff",
            activeUser?.username ?: "system",
            "Added new customer: ${customer.name}"
        )
        return id
    }

    suspend fun updateCustomer(customer: CustomerEntity, activeUser: UserEntity?) {
        customerDao.updateCustomer(customer)
        logActivity(
            activeUser?.role ?: "Staff",
            activeUser?.username ?: "system",
            "Updated customer: ${customer.name}"
        )
    }

    suspend fun deleteCustomer(customer: CustomerEntity, activeUser: UserEntity?) {
        customerDao.deleteCustomer(customer)
        logActivity(
            activeUser?.role ?: "Staff",
            activeUser?.username ?: "system",
            "Deleted customer: ${customer.name}"
        )
    }

    // MILK COLLECTION OPERATIONS
    suspend fun addCollection(collection: MilkCollectionEntity, activeUser: UserEntity?): Long {
        val id = collectionDao.insertCollection(collection)
        // Auto-increment the customer's balance since we owe them money for supplying milk
        // (supplying milk means we owe them, so customer balance decreases / gets credited)
        customerDao.updateCustomerBalance(collection.customerId, -collection.amount)
        
        val customer = customerDao.getCustomerById(collection.customerId)
        val customerName = customer?.name ?: "Customer #${collection.customerId}"
        
        logActivity(
            activeUser?.role ?: "Milk Collector",
            activeUser?.username ?: "collector",
            "Collected ${collection.quantity}L milk from $customerName (Amt: ${collection.amount})"
        )
        
        // Also check if bulk supply alert is needed
        if (collection.quantity >= 30.0) {
            notificationDao.insertNotification(
                NotificationEntity(
                    title = "Bulk Collection Registered",
                    message = "$customerName supplied a massive ${collection.quantity}L of milk (Fat: ${collection.fat}%, SNF: ${collection.snf}%)."
                )
            )
        }
        
        return id
    }

    suspend fun updateCollection(collection: MilkCollectionEntity, activeUser: UserEntity?) {
        val allCols = collectionDao.getAllCollections()
        val oldCollection = allCols.find { it.id == collection.id }
        if (oldCollection != null) {
            // Reverse old balance adjustment
            customerDao.updateCustomerBalance(oldCollection.customerId, oldCollection.amount)
        }
        // Save the updated entry
        collectionDao.insertCollection(collection)
        // Apply new balance adjustment
        customerDao.updateCustomerBalance(collection.customerId, -collection.amount)
        
        logActivity(
            activeUser?.role ?: "Milk Collector",
            activeUser?.username ?: "collector",
            "Updated milk collection entry #${collection.id} (New: ${collection.quantity}L, Amt: ${collection.amount})"
        )
    }

    suspend fun removeCollection(collection: MilkCollectionEntity, activeUser: UserEntity?) {
        collectionDao.deleteCollection(collection)
        // Reverse customer balance credit
        customerDao.updateCustomerBalance(collection.customerId, collection.amount)
        
        logActivity(
            activeUser?.role ?: "Manager",
            activeUser?.username ?: "system",
            "Cancelled milk collection entry #${collection.id}"
        )
    }

    // PRODUCT OPERATIONS
    suspend fun addProduct(product: ProductEntity, activeUser: UserEntity?): Long {
        val id = productDao.insertProduct(product)
        logActivity(
            activeUser?.role ?: "Manager",
            activeUser?.username ?: "system",
            "Added product: ${product.name} @ Rate: ${product.price}"
        )
        return id
    }

    suspend fun updateProduct(product: ProductEntity, activeUser: UserEntity?) {
        productDao.updateProduct(product)
        logActivity(
            activeUser?.role ?: "Manager",
            activeUser?.username ?: "system",
            "Updated product details: ${product.name}"
        )
    }

    suspend fun deleteProduct(product: ProductEntity, activeUser: UserEntity?) {
        productDao.deleteProduct(product)
        logActivity(
            activeUser?.role ?: "Manager",
            activeUser?.username ?: "system",
            "Removed product: ${product.name}"
        )
    }

    suspend fun adjustProductStock(productId: Long, quantity: Double) {
        productDao.updateProductStock(productId, quantity)
        val p = productDao.getProductById(productId)
        if (p != null && p.stockQty < 10.0) {
            notificationDao.insertNotification(
                NotificationEntity(
                    title = "Low Stock Warning",
                    message = "${p.name} stock level is low! Current stock: ${p.stockQty} ${p.unit}."
                )
            )
        }
    }

    // INVOICE / SALES OPERATIONS
    suspend fun getAllProductsList(): List<ProductEntity> {
        return productDao.getAllProducts()
    }

    suspend fun createInvoice(
        invoice: InvoiceEntity,
        items: List<InvoiceItemEntity>,
        activeUser: UserEntity?
    ): Long {
        val invoiceId = invoiceDao.insertInvoice(invoice)
        val itemsWithId = items.map { it.copy(invoiceId = invoiceId) }
        invoiceDao.insertInvoiceItems(itemsWithId)

        // Update customer balance: since they bought products, they owe us money now
        // If paymentStatus is Pending, customer balance increases. If Paid, balance stays flat (or is increased then immediately cleared by a payment receipt).
        if (invoice.paymentStatus == "Pending") {
            customerDao.updateCustomerBalance(invoice.customerId, invoice.totalAmount)
        }

        // Deduct inventory stock for each product
        for (item in items) {
            productDao.updateProductStock(item.productId, -item.quantity)
            // Trigger alerts if inventory is low
            val p = productDao.getProductById(item.productId)
            if (p != null && p.stockQty < 10.0) {
                notificationDao.insertNotification(
                    NotificationEntity(
                        title = "Low Stock Alert",
                        message = "${p.name} is running low! Current stock: ${p.stockQty} ${p.unit}."
                    )
                )
            }
        }

        logActivity(
            activeUser?.role ?: "Staff",
            activeUser?.username ?: "system",
            "Generated Invoice #${invoice.invoiceNumber} for ${invoice.customerName} (Total: ${invoice.totalAmount})"
        )

        return invoiceId
    }

    suspend fun getInvoiceItems(invoiceId: Long): List<InvoiceItemEntity> {
        return invoiceDao.getItemsForInvoice(invoiceId)
    }

    // PAYMENT OPERATIONS
    suspend fun addPayment(payment: PaymentEntity, activeUser: UserEntity?): Long {
        val paymentId = paymentDao.insertPayment(payment)
        // Deduct customer's pending balance since they paid us
        customerDao.updateCustomerBalance(payment.customerId, -payment.amountReceived)

        val customer = customerDao.getCustomerById(payment.customerId)
        val customerName = customer?.name ?: "Customer #${payment.customerId}"

        logActivity(
            activeUser?.role ?: "Accountant",
            activeUser?.username ?: "accountant",
            "Received payment of ${payment.amountReceived} from $customerName via ${payment.paymentMethod}"
        )

        return paymentId
    }

    // EXPENSE OPERATIONS
    suspend fun addExpense(expense: ExpenseEntity, activeUser: UserEntity?): Long {
        val id = expenseDao.insertExpense(expense)
        logActivity(
            activeUser?.role ?: "Accountant",
            activeUser?.username ?: "accountant",
            "Recorded expense: ${expense.category} - ${expense.amount}"
        )
        return id
    }

    suspend fun removeExpense(expense: ExpenseEntity, activeUser: UserEntity?) {
        expenseDao.deleteExpense(expense)
        logActivity(
            activeUser?.role ?: "Accountant",
            activeUser?.username ?: "accountant",
            "Cancelled expense: ${expense.category} - ${expense.amount}"
        )
    }

    // NOTIFICATIONS
    suspend fun markNotificationAsRead(id: Long) {
        notificationDao.markAsRead(id)
    }

    suspend fun markAllNotificationsAsRead() {
        notificationDao.markAllAsRead()
    }

    // SYSTEM AUDIT LOGGING
    suspend fun logActivity(role: String, username: String, action: String) {
        activityLogDao.insertLog(
            ActivityLogEntity(
                userRole = role,
                username = username,
                action = action
            )
        )
    }

    // RATE CHART OPERATIONS
    suspend fun getActiveRateCharts(milkType: String): List<RateChartEntity> {
        return rateChartDao.getActiveRateCharts(milkType)
    }

    suspend fun findRateInChart(milkType: String, fat: Double, snf: Double): Double? {
        val match = rateChartDao.findRate(milkType, fat, snf)
        return match?.rate
    }

    suspend fun addRate(rateChart: RateChartEntity): Long {
        return rateChartDao.insertRate(rateChart)
    }

    suspend fun addRates(rates: List<RateChartEntity>) {
        rateChartDao.insertRates(rates)
    }

    suspend fun updateRate(rateChart: RateChartEntity) {
        rateChartDao.updateRate(rateChart)
    }

    suspend fun deleteRate(rateChart: RateChartEntity) {
        rateChartDao.deleteRate(rateChart)
    }

    suspend fun clearRateChart(milkType: String) {
        rateChartDao.clearRateChart(milkType)
    }

    // DAIRY PROFILE OPERATIONS
    suspend fun getDairyProfile(): DairyProfileEntity? {
        return dairyProfileDao.getProfile()
    }

    suspend fun updateDairyProfile(profile: DairyProfileEntity) {
        dairyProfileDao.updateProfile(profile)
    }
}
