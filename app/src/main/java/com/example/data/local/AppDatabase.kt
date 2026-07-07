package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        CustomerEntity::class,
        MilkCollectionEntity::class,
        ProductEntity::class,
        InvoiceEntity::class,
        InvoiceItemEntity::class,
        PaymentEntity::class,
        ExpenseEntity::class,
        NotificationEntity::class,
        ActivityLogEntity::class,
        RateChartEntity::class,
        DairyProfileEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun customerDao(): CustomerDao
    abstract fun collectionDao(): CollectionDao
    abstract fun productDao(): ProductDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun paymentDao(): PaymentDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun notificationDao(): NotificationDao
    abstract fun activityLogDao(): ActivityLogDao
    abstract fun rateChartDao(): RateChartDao
    abstract fun dairyProfileDao(): DairyProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dairyhub_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                
                // Actively check and seed database on app startup if empty
                scope.launch(Dispatchers.IO) {
                    try {
                        if (instance.userDao().getUserCount() == 0) {
                            DatabaseCallback(scope).populateDatabase(instance)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database)
                }
            }
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    if (database.userDao().getUserCount() == 0) {
                        populateDatabase(database)
                    }
                }
            }
        }

        suspend fun populateDatabase(db: AppDatabase) {
            // Seed Default Dairy Profile
            db.dairyProfileDao().insertProfile(
                DairyProfileEntity(
                    id = 1,
                    dairyName = "DairyHub",
                    ownerName = "Ashutosh Kumar Maurya",
                    contactNumber = "+91 6207159208",
                    emailAddress = "ashutoshsingh1363@gmail.com",
                    address = "VILL- KRIPALPUR, POST-DEOHALIA, P.S.- DURGAWATI, BHABUA(KAIMUR) 821105",
                    gstNumber = "22AAAAA0000A1Z5",
                    panNumber = "HIOPM2988E",
                    logo = "default_logo",
                    signature = "ASHU🤍❤️💔🖤",
                    website = "https://www.dairyhub.com",
                    upiId = "6207159208@upi"
                )
            )

            // 1. Seed Users
            val users = listOf(
                UserEntity(1, "owner", "owner123", "Ashutosh Kumar Maurya (Owner)", "Owner", "6207159208", "ashutoshsingh1363@gmail.com"),
                UserEntity(2, "manager", "manager123", "Rajesh Sharma (Manager)", "Manager", "9876543211", "manager@dairyhub.com"),
                UserEntity(3, "staff", "staff123", "Suresh Patel (Staff)", "Staff", "9876543212", "staff@dairyhub.com"),
                UserEntity(4, "collector", "collector123", "Vijay Yadav (Collector)", "Milk Collector", "9876543213", "collector@dairyhub.com"),
                UserEntity(5, "accountant", "accountant123", "Anil Gupta (Accountant)", "Accountant", "9876543214", "accountant@dairyhub.com")
            )
            for (u in users) {
                db.userDao().insertUser(u)
            }

            // 2. Seed Customers
            val customers = listOf(
                CustomerEntity(
                    id = 1,
                    name = "Ramesh Singh",
                    phone = "9898012345",
                    address = "Village Rampur, Sector 1",
                    joinedDate = System.currentTimeMillis() - 10 * 24 * 3600 * 1000L,
                    balance = 1250.0,
                    code = "F-101",
                    email = "ramesh@gmail.com",
                    milkType = "Cow",
                    bankName = "State Bank of India",
                    branch = "Rampur Branch",
                    accountNumber = "10020030040",
                    ifsc = "SBIN0001234",
                    isActive = true
                ),
                CustomerEntity(
                    id = 2,
                    name = "Sita Devi",
                    phone = "9898054321",
                    address = "Village Rampur, Sector 2",
                    joinedDate = System.currentTimeMillis() - 9 * 24 * 3600 * 1000L,
                    balance = 0.0,
                    code = "F-102",
                    email = "sita@gmail.com",
                    milkType = "Buffalo",
                    bankName = "Punjab National Bank",
                    branch = "Rampur Branch",
                    accountNumber = "998877665544",
                    ifsc = "PUNB0123456",
                    isActive = true
                ),
                CustomerEntity(
                    id = 3,
                    name = "Mahendra Yadav",
                    phone = "9898123456",
                    address = "Yadav Farm, Ward 5",
                    joinedDate = System.currentTimeMillis() - 8 * 24 * 3600 * 1000L,
                    balance = 3400.0,
                    code = "F-103",
                    email = "mahendra@gmail.com",
                    milkType = "Both",
                    bankName = "HDFC Bank",
                    branch = "City Branch",
                    accountNumber = "112233445566",
                    ifsc = "HDFC0000789",
                    isActive = true
                ),
                CustomerEntity(
                    id = 4,
                    name = "Sunita Sharma",
                    phone = "9898654321",
                    address = "Main Bazaar, Gali No. 3",
                    joinedDate = System.currentTimeMillis() - 7 * 24 * 3600 * 1000L,
                    balance = -150.0,
                    code = "F-104",
                    email = "sunita@gmail.com",
                    milkType = "Cow",
                    bankName = "Bank of Baroda",
                    branch = "Rampur East",
                    accountNumber = "554433221100",
                    ifsc = "BARB0RAMPUR",
                    isActive = true
                ),
                CustomerEntity(
                    id = 5,
                    name = "Gopal Das",
                    phone = "9898321456",
                    address = "Village Rampur, Sector 4",
                    joinedDate = System.currentTimeMillis() - 6 * 24 * 3600 * 1000L,
                    balance = 850.0,
                    code = "F-105",
                    email = "gopal@gmail.com",
                    milkType = "Buffalo",
                    bankName = "ICICI Bank",
                    branch = "Highway Crossing",
                    accountNumber = "334455667788",
                    ifsc = "ICIC0001122",
                    isActive = false
                )
            )
            for (c in customers) {
                db.customerDao().insertCustomer(c)
            }

            // 3. Seed Products
            val products = listOf(
                ProductEntity(1, "Standard Milk", "Dairy", 60.0, 450.0, "Litre"),
                ProductEntity(2, "Fresh Paneer", "Dairy", 360.0, 42.0, "Kg"),
                ProductEntity(3, "Creamy Curd", "Dairy", 80.0, 95.0, "Kg"),
                ProductEntity(4, "Cow Ghee", "Dairy", 680.0, 35.0, "Kg"),
                ProductEntity(5, "Salted Butter", "Dairy", 440.0, 20.0, "Kg"),
                ProductEntity(6, "Sweet Lassi", "Dairy", 40.0, 150.0, "Packet"),
                ProductEntity(7, "Mozzarella Cheese", "Dairy", 480.0, 18.0, "Kg")
            )
            for (p in products) {
                db.productDao().insertProduct(p)
            }

            // 4. Seed Expenses
            val expenses = listOf(
                ExpenseEntity(1, "Salary", 45000.0, "Staff monthly salary payout", System.currentTimeMillis() - 5 * 24 * 3600 * 1000L),
                ExpenseEntity(2, "Diesel", 3400.0, "Milk transport tanker fuel refill", System.currentTimeMillis() - 4 * 24 * 3600 * 1000L),
                ExpenseEntity(3, "Electricity", 8500.0, "Cold storage power bill", System.currentTimeMillis() - 3 * 24 * 3600 * 1000L),
                ExpenseEntity(4, "Animal Feed", 12000.0, "Cooperative cattle feed bulk supply", System.currentTimeMillis() - 2 * 24 * 3600 * 1000L),
                ExpenseEntity(5, "Maintenance", 1800.0, "Water pump sealing repair", System.currentTimeMillis() - 1 * 24 * 3600 * 1000L)
            )
            for (e in expenses) {
                db.expenseDao().insertExpense(e)
            }

            // 5. Seed some Milk Collection
            val collections = listOf(
                MilkCollectionEntity(1, 1, "Morning", 4.2, 8.5, 28.0, 25.0, 48.5, 1212.5, System.currentTimeMillis() - 2 * 3600 * 1000L),
                MilkCollectionEntity(2, 3, "Morning", 4.8, 8.8, 29.5, 40.0, 52.8, 2112.0, System.currentTimeMillis() - 1 * 3600 * 1000L),
                MilkCollectionEntity(3, 5, "Morning", 3.8, 8.2, 27.0, 15.0, 44.2, 663.0, System.currentTimeMillis() - 30 * 60 * 1000L)
            )
            for (col in collections) {
                db.collectionDao().insertCollection(col)
            }

            // 6. Seed some Notifications
            val notifications = listOf(
                NotificationEntity(1, "Low Stock Alert", "Fresh Paneer stock level is below 10 kg! Current stock is 8 kg.", System.currentTimeMillis() - 3 * 3600 * 1000L, false),
                NotificationEntity(2, "Bulk Collection Registered", "Mahendra Yadav supplied a massive 40.0 Litres of premium quality milk in Morning shift.", System.currentTimeMillis() - 1 * 3600 * 1000L, false),
                NotificationEntity(3, "Monthly Billing Initiated", "Automatic invoicing for the past billing cycle is ready for review.", System.currentTimeMillis() - 24 * 3600 * 1000L, true)
            )
            for (n in notifications) {
                db.notificationDao().insertNotification(n)
            }

            // 7. Seed Activity Logs
            val logs = listOf(
                ActivityLogEntity(1, "Owner", "owner", "User database initialized successfully", System.currentTimeMillis() - 12 * 3600 * 1000L),
                ActivityLogEntity(2, "Manager", "manager", "Product catalog pricing verified", System.currentTimeMillis() - 10 * 3600 * 1000L),
                ActivityLogEntity(3, "Milk Collector", "collector", "Morning milk collection cycle started", System.currentTimeMillis() - 3 * 3600 * 1000L)
            )
            for (l in logs) {
                db.activityLogDao().insertLog(l)
            }

            // 8. Seed Rate Chart
            val cowRates = mutableListOf<RateChartEntity>()
            // Generate standard rates for Cow
            for (f in listOf(3.0, 3.5, 4.0, 4.5, 5.0)) {
                for (s in listOf(8.0, 8.5, 9.0)) {
                    cowRates.add(RateChartEntity(milkType = "Cow", fat = f, snf = s, rate = (f * 6.5) + (s * 3.8), isActive = true))
                }
            }
            db.rateChartDao().insertRates(cowRates)

            val buffaloRates = mutableListOf<RateChartEntity>()
            // Generate standard rates for Buffalo
            for (f in listOf(5.5, 6.0, 6.5, 7.0, 7.5)) {
                for (s in listOf(8.5, 9.0, 9.5)) {
                    buffaloRates.add(RateChartEntity(milkType = "Buffalo", fat = f, snf = s, rate = (f * 7.2) + (s * 4.2), isActive = true))
                }
            }
            db.rateChartDao().insertRates(buffaloRates)
        }
    }
}
