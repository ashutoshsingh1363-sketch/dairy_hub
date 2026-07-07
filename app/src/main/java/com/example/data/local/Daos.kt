package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE isRemembered = 1 LIMIT 1")
    suspend fun getRememberedUser(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET isRemembered = :remember WHERE id = :userId")
    suspend fun updateRememberMe(userId: Long, remember: Boolean)

    @Query("UPDATE users SET passwordHash = :passwordHash WHERE id = :userId")
    suspend fun updatePasswordHash(userId: Long, passwordHash: String)

    @Query("SELECT * FROM users ORDER BY id DESC")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomersFlow(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers ORDER BY name ASC")
    suspend fun getAllCustomers(): List<CustomerEntity>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun getCustomerById(id: Long): CustomerEntity?

    @Query("SELECT * FROM customers WHERE name LIKE :query OR phone LIKE :query")
    fun searchCustomersFlow(query: String): Flow<List<CustomerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)

    @Query("UPDATE customers SET balance = balance + :amount WHERE id = :id")
    suspend fun updateCustomerBalance(id: Long, amount: Double)
}

@Dao
interface CollectionDao {
    @Query("SELECT * FROM milk_collections ORDER BY timestamp DESC")
    fun getAllCollectionsFlow(): Flow<List<MilkCollectionEntity>>

    @Query("SELECT * FROM milk_collections ORDER BY timestamp DESC")
    suspend fun getAllCollections(): List<MilkCollectionEntity>

    @Query("SELECT * FROM milk_collections WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getCollectionsByCustomerFlow(customerId: Long): Flow<List<MilkCollectionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: MilkCollectionEntity): Long

    @Delete
    suspend fun deleteCollection(collection: MilkCollectionEntity)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProductsFlow(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products ORDER BY name ASC")
    suspend fun getAllProducts(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("UPDATE products SET stockQty = stockQty + :quantity WHERE id = :id")
    suspend fun updateProductStock(id: Long, quantity: Double)
}

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY timestamp DESC")
    fun getAllInvoicesFlow(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices ORDER BY timestamp DESC")
    suspend fun getAllInvoices(): List<InvoiceEntity>

    @Query("SELECT * FROM invoices WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getInvoicesByCustomerFlow(customerId: Long): Flow<List<InvoiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceItems(items: List<InvoiceItemEntity>)

    @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId")
    suspend fun getItemsForInvoice(invoiceId: Long): List<InvoiceItemEntity>

    @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId")
    fun getItemsForInvoiceFlow(invoiceId: Long): Flow<List<InvoiceItemEntity>>
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY dateMillis DESC")
    fun getAllPaymentsFlow(): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments ORDER BY dateMillis DESC")
    suspend fun getAllPayments(): List<PaymentEntity>

    @Query("SELECT * FROM payments WHERE customerId = :customerId ORDER BY dateMillis DESC")
    fun getPaymentsByCustomerFlow(customerId: Long): Flow<List<PaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity): Long
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY dateMillis DESC")
    fun getAllExpensesFlow(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses ORDER BY dateMillis DESC")
    suspend fun getAllExpenses(): List<ExpenseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY dateMillis DESC")
    fun getAllNotificationsFlow(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE isRead = 0 ORDER BY dateMillis DESC")
    fun getUnreadNotificationsFlow(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead()
}

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC LIMIT 100")
    fun getRecentLogsFlow(): Flow<List<ActivityLogEntity>>

    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC")
    suspend fun getAllLogs(): List<ActivityLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActivityLogEntity): Long
}

@Dao
interface RateChartDao {
    @Query("SELECT * FROM rate_charts ORDER BY milkType ASC, fat ASC, snf ASC")
    fun getAllRateChartsFlow(): Flow<List<RateChartEntity>>

    @Query("SELECT * FROM rate_charts WHERE milkType = :milkType AND isActive = 1 ORDER BY fat ASC, snf ASC")
    suspend fun getActiveRateCharts(milkType: String): List<RateChartEntity>

    @Query("SELECT * FROM rate_charts WHERE milkType = :milkType AND fat = :fat AND snf = :snf AND isActive = 1 LIMIT 1")
    suspend fun findRate(milkType: String, fat: Double, snf: Double): RateChartEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRate(rateChart: RateChartEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRates(rates: List<RateChartEntity>)

    @Update
    suspend fun updateRate(rateChart: RateChartEntity)

    @Delete
    suspend fun deleteRate(rateChart: RateChartEntity)

    @Query("DELETE FROM rate_charts WHERE milkType = :milkType")
    suspend fun clearRateChart(milkType: String)
}

@Dao
interface DairyProfileDao {
    @Query("SELECT * FROM dairy_profile WHERE id = 1 LIMIT 1")
    fun getProfileFlow(): Flow<DairyProfileEntity?>

    @Query("SELECT * FROM dairy_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfile(): DairyProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: DairyProfileEntity)

    @Update
    suspend fun updateProfile(profile: DairyProfileEntity)
}

