package com.market.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.market.domain.model.Price
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PriceDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun pricesCollection(householdId: String, itemId: String) =
        firestore.collection("households")
            .document(householdId)
            .collection("items")
            .document(itemId)
            .collection("prices")

    fun observePrices(householdId: String, itemId: String): Flow<List<Price>> = callbackFlow {
        val registration: ListenerRegistration = pricesCollection(householdId, itemId)
            .orderBy("recordedAt")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val prices = snapshot?.documents?.map { doc ->
                    Price(
                        itemId = itemId,
                        storeId = doc.id,
                        amount = doc.getDouble("amount") ?: 0.0,
                        currency = doc.getString("currency") ?: "CRC",
                        recordedBy = doc.getString("recordedBy") ?: "",
                        recordedAt = doc.getLong("recordedAt") ?: 0L
                    )
                } ?: emptyList()
                trySend(prices)
            }
        awaitClose { registration.remove() }
    }

    fun observeAllPrices(householdId: String): Flow<List<Price>> = callbackFlow {
        // Listen to all items subcollections via collection group query
        val registration: ListenerRegistration = firestore.collectionGroup("prices")
            .whereEqualTo("householdId", householdId)
            .orderBy("recordedAt")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val prices = snapshot?.documents?.mapNotNull { doc ->
                    // Extract itemId from parent path: households/{hid}/items/{itemId}/prices/{storeId}
                    val path = doc.reference.path
                    val itemsIndex = path.indexOf("/items/")
                    val pricesIndex = path.indexOf("/prices/")
                    if (itemsIndex < 0 || pricesIndex < 0) return@mapNotNull null

                    val itemId = path.substring(itemsIndex + 7, pricesIndex)
                    Price(
                        itemId = itemId,
                        storeId = doc.id,
                        amount = doc.getDouble("amount") ?: 0.0,
                        currency = doc.getString("currency") ?: "CRC",
                        recordedBy = doc.getString("recordedBy") ?: "",
                        recordedAt = doc.getLong("recordedAt") ?: 0L
                    )
                } ?: emptyList()
                trySend(prices)
            }
        awaitClose { registration.remove() }
    }

    suspend fun recordPrice(price: Price, householdId: String): Price {
        val now = System.currentTimeMillis()
        pricesCollection(householdId, price.itemId)
            .document(price.storeId)
            .set(mapOf(
                "amount" to price.amount,
                "currency" to "CRC",
                "recordedBy" to price.recordedBy,
                "recordedAt" to now,
                "householdId" to householdId
            ))
            .await()
        return price.copy(recordedAt = now, currency = "CRC")
    }

    suspend fun getCheapestStore(
        householdId: String,
        itemId: String
    ): Pair<String, Double>? {
        val snapshot = pricesCollection(householdId, itemId)
            .orderBy("amount")
            .limit(1)
            .get()
            .await()

        val doc = snapshot.documents.firstOrNull() ?: return null
        return doc.id to (doc.getDouble("amount") ?: 0.0)
    }
}
