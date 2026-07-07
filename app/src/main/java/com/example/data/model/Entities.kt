package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val passwordHash: String,
    val fullName: String,
    val role: String, // Owner, Manager, Staff, Milk Collector, Accountant, Customer
    val phone: String,
    val email: String,
    val joinedDate: Long = System.currentTimeMillis(),
    val isRemembered: Boolean = false
)

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val address: String,
    val joinedDate: Long = System.currentTimeMillis(),
    val balance: Double = 0.0, // positive = pending dues from customer, negative = credit
    val code: String = "",
    val email: String = "",
    val milkType: String = "Both", // Cow, Buffalo, Both
    val bankName: String = "",
    val branch: String = "",
    val accountNumber: String = "",
    val ifsc: String = "",
    val customerType: String = "Farmer", // Farmer, Retailer, Wholesaler, Staff
    val paymentMode: String = "Cash", // Cash, UPI, Credit, Bank Transfer
    val isActive: Boolean = true,
    val cowCount: Int = 0,
    val buffaloCount: Int = 0
)

@Entity(
    tableName = "milk_collections",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["customerId"])]
)
data class MilkCollectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val shift: String, // Morning, Evening
    val fat: Double, // % fat
    val snf: Double, // % Solid-Not-Fat
    val clr: Double, // Corrected Lactometer Reading
    val quantity: Double, // in Litres
    val rate: Double, // Rate per Litre (auto calculated based on fat/snf)
    val amount: Double, // Total amount (qty * rate)
    val timestamp: Long = System.currentTimeMillis(),
    val milkType: String = "Cow" // Cow, Buffalo
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String, // Milk, Paneer, Curd, Butter, Ghee, Lassi, Cheese
    val category: String, // Dairy, Packet, Container
    val price: Double,
    val stockQty: Double,
    val unit: String // Litre, Kg, Packet
)

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val customerName: String,
    val subtotal: Double,
    val discount: Double,
    val gst: Double,
    val totalAmount: Double,
    val paymentStatus: String, // Paid, Pending
    val timestamp: Long = System.currentTimeMillis(),
    val invoiceNumber: String,
    val barcodeData: String,
    val qrData: String
)

@Entity(
    tableName = "invoice_items",
    foreignKeys = [
        ForeignKey(
            entity = InvoiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["invoiceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["invoiceId"])]
)
data class InvoiceItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceId: Long,
    val productId: Long,
    val productName: String,
    val quantity: Double,
    val price: Double,
    val amount: Double
)

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["customerId"])]
)
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val amountReceived: Double,
    val dateMillis: Long = System.currentTimeMillis(),
    val paymentMethod: String, // Cash, UPI, Bank Transfer, Card
    val notes: String,
    val transactionId: String
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String, // Salary, Diesel, Electricity, Maintenance, Animal Feed, Others
    val amount: Double,
    val description: String,
    val dateMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val dateMillis: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userRole: String,
    val username: String,
    val action: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "rate_charts")
data class RateChartEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val milkType: String, // Cow, Buffalo
    val fat: Double,
    val snf: Double,
    val rate: Double,
    val effectiveDate: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

@Entity(tableName = "dairy_profile")
data class DairyProfileEntity(
    @PrimaryKey val id: Int = 1,
    val dairyName: String = "DairyHub",
    val ownerName: String = "Ashutosh Kumar Maurya",
    val contactNumber: String = "+91 6207159208",
    val emailAddress: String = "ashutoshsingh1363@gmail.com",
    val address: String = "VILL- KRIPALPUR, POST-DEOHALIA, P.S.- DURGAWATI, BHABUA(KAIMUR) 821105",
    val gstNumber: String = "22AAAAA0000A1Z5",
    val panNumber: String = "HIOPM2988E",
    val logo: String = "default_logo",
    val signature: String = "ASHU🤍❤️💔🖤",
    val website: String = "https://www.dairyhub.com",
    val upiId: String = "6207159208@upi"
)

