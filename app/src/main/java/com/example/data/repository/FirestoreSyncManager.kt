package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.model.CustomerEntity
import com.example.data.model.MilkCollectionEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirestoreSyncManager(private val context: Context) {
    private val TAG = "FirestoreSyncManager"

    // Programmatic safe Firestore instance initializer
    val db: FirebaseFirestore? by lazy {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId("1:513867921310:android:17cc1685746a42db87accd6724276017")
                    .setProjectId("dairyhub-dhubxz")
                    .setApiKey("AIzaSyA_mockKeyForGracefulInitialization")
                    .build()
                FirebaseApp.initializeApp(context, options)
                Log.d(TAG, "Firebase initialized programmatically.")
            }
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Firebase Firestore: ${e.message}", e)
            null
        }
    }

    fun isAvailable(): Boolean = db != null

    // Helper to upload a single farmer profile
    private suspend fun uploadFarmerItem(farmer: CustomerEntity): Void? = suspendCancellableCoroutine { continuation ->
        val firestore = db
        if (firestore == null) {
            continuation.resumeWithException(IllegalStateException("Firestore is not available"))
            return@suspendCancellableCoroutine
        }

        val data = mapOf(
            "id" to farmer.id,
            "name" to farmer.name,
            "phone" to farmer.phone,
            "address" to farmer.address,
            "joinedDate" to farmer.joinedDate,
            "balance" to farmer.balance,
            "code" to farmer.code,
            "email" to farmer.email,
            "milkType" to farmer.milkType,
            "bankName" to farmer.bankName,
            "branch" to farmer.branch,
            "accountNumber" to farmer.accountNumber,
            "ifsc" to farmer.ifsc,
            "customerType" to farmer.customerType,
            "paymentMode" to farmer.paymentMode,
            "isActive" to farmer.isActive,
            "cowCount" to farmer.cowCount,
            "buffaloCount" to farmer.buffaloCount,
            "lastSynced" to System.currentTimeMillis()
        )

        firestore.collection("farmers").document(farmer.id.toString())
            .set(data, SetOptions.merge())
            .addOnSuccessListener { result ->
                continuation.resume(result)
            }
            .addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }
    }

    // Helper to upload a single milk collection production log
    private suspend fun uploadLogItem(log: MilkCollectionEntity): Void? = suspendCancellableCoroutine { continuation ->
        val firestore = db
        if (firestore == null) {
            continuation.resumeWithException(IllegalStateException("Firestore is not available"))
            return@suspendCancellableCoroutine
        }

        val data = mapOf(
            "id" to log.id,
            "customerId" to log.customerId,
            "shift" to log.shift,
            "fat" to log.fat,
            "snf" to log.snf,
            "clr" to log.clr,
            "quantity" to log.quantity,
            "rate" to log.rate,
            "amount" to log.amount,
            "timestamp" to log.timestamp,
            "milkType" to log.milkType,
            "lastSynced" to System.currentTimeMillis()
        )

        firestore.collection("production_logs").document(log.id.toString())
            .set(data, SetOptions.merge())
            .addOnSuccessListener { result ->
                continuation.resume(result)
            }
            .addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }
    }

    // Upload all Farmers to Firestore in batches/sequence
    suspend fun uploadFarmers(
        farmers: List<CustomerEntity>,
        onProgress: (Int, Int) -> Unit,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (!isAvailable()) {
            onFailure(IllegalStateException("Firestore cannot be initialized. Check internet connection or configuration."))
            return
        }
        try {
            var count = 0
            for (farmer in farmers) {
                uploadFarmerItem(farmer)
                count++
                onProgress(count, farmers.size)
            }
            onSuccess()
        } catch (e: Exception) {
            Log.e(TAG, "Failed uploading farmers: ${e.message}", e)
            onFailure(e)
        }
    }

    // Upload all production logs to Firestore in sequence
    suspend fun uploadProductionLogs(
        logs: List<MilkCollectionEntity>,
        onProgress: (Int, Int) -> Unit,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (!isAvailable()) {
            onFailure(IllegalStateException("Firestore is not available"))
            return
        }
        try {
            var count = 0
            for (log in logs) {
                uploadLogItem(log)
                count++
                onProgress(count, logs.size)
            }
            onSuccess()
        } catch (e: Exception) {
            Log.e(TAG, "Failed uploading production logs: ${e.message}", e)
            onFailure(e)
        }
    }

    // Download farmers from Firestore
    suspend fun downloadFarmers(
        onSuccess: (List<CustomerEntity>) -> Unit,
        onFailure: (Exception) -> Unit
    ) = suspendCancellableCoroutine<Unit> { continuation ->
        val firestore = db
        if (firestore == null) {
            val ex = IllegalStateException("Firestore is not available")
            onFailure(ex)
            continuation.resume(Unit)
            return@suspendCancellableCoroutine
        }

        firestore.collection("farmers").get()
            .addOnSuccessListener { snapshot ->
                val list = mutableListOf<CustomerEntity>()
                for (doc in snapshot.documents) {
                    try {
                        val id = doc.getLong("id") ?: continue
                        val name = doc.getString("name") ?: ""
                        val phone = doc.getString("phone") ?: ""
                        val address = doc.getString("address") ?: ""
                        val joinedDate = doc.getLong("joinedDate") ?: System.currentTimeMillis()
                        val balance = doc.getDouble("balance") ?: 0.0
                        val code = doc.getString("code") ?: ""
                        val email = doc.getString("email") ?: ""
                        val milkType = doc.getString("milkType") ?: "Both"
                        val bankName = doc.getString("bankName") ?: ""
                        val branch = doc.getString("branch") ?: ""
                        val accountNumber = doc.getString("accountNumber") ?: ""
                        val ifsc = doc.getString("ifsc") ?: ""
                        val customerType = doc.getString("customerType") ?: "Farmer"
                        val paymentMode = doc.getString("paymentMode") ?: "Cash"
                        val isActive = doc.getBoolean("isActive") ?: true
                        val cowCount = doc.getLong("cowCount")?.toInt() ?: 0
                        val buffaloCount = doc.getLong("buffaloCount")?.toInt() ?: 0

                        list.add(
                            CustomerEntity(
                                id = id,
                                name = name,
                                phone = phone,
                                address = address,
                                joinedDate = joinedDate,
                                balance = balance,
                                code = code,
                                email = email,
                                milkType = milkType,
                                bankName = bankName,
                                branch = branch,
                                accountNumber = accountNumber,
                                ifsc = ifsc,
                                customerType = customerType,
                                paymentMode = paymentMode,
                                isActive = isActive,
                                cowCount = cowCount,
                                buffaloCount = buffaloCount
                            )
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing farmer doc: ${e.message}")
                    }
                }
                onSuccess(list)
                continuation.resume(Unit)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
                continuation.resume(Unit)
            }
    }

    // Download production logs from Firestore
    suspend fun downloadProductionLogs(
        onSuccess: (List<MilkCollectionEntity>) -> Unit,
        onFailure: (Exception) -> Unit
    ) = suspendCancellableCoroutine<Unit> { continuation ->
        val firestore = db
        if (firestore == null) {
            val ex = IllegalStateException("Firestore is not available")
            onFailure(ex)
            continuation.resume(Unit)
            return@suspendCancellableCoroutine
        }

        firestore.collection("production_logs").get()
            .addOnSuccessListener { snapshot ->
                val list = mutableListOf<MilkCollectionEntity>()
                for (doc in snapshot.documents) {
                    try {
                        val id = doc.getLong("id") ?: continue
                        val customerId = doc.getLong("customerId") ?: continue
                        val shift = doc.getString("shift") ?: "Morning"
                        val fat = doc.getDouble("fat") ?: 0.0
                        val snf = doc.getDouble("snf") ?: 0.0
                        val clr = doc.getDouble("clr") ?: 0.0
                        val quantity = doc.getDouble("quantity") ?: 0.0
                        val rate = doc.getDouble("rate") ?: 0.0
                        val amount = doc.getDouble("amount") ?: 0.0
                        val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                        val milkType = doc.getString("milkType") ?: "Cow"

                        list.add(
                            MilkCollectionEntity(
                                id = id,
                                customerId = customerId,
                                shift = shift,
                                fat = fat,
                                snf = snf,
                                clr = clr,
                                quantity = quantity,
                                rate = rate,
                                amount = amount,
                                timestamp = timestamp,
                                milkType = milkType
                            )
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing log doc: ${e.message}")
                    }
                }
                onSuccess(list)
                continuation.resume(Unit)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
                continuation.resume(Unit)
            }
    }
}
