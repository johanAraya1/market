package com.market.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.market.domain.model.Trip
import com.market.domain.model.TripItem
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun tripsCollection(householdId: String) =
        firestore.collection("households").document(householdId).collection("trips")

    fun observeTrips(householdId: String): Flow<List<Trip>> = callbackFlow {
        val registration: ListenerRegistration = tripsCollection(householdId)
            .orderBy("completedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val trips = snapshot?.documents?.map { doc ->
                    @Suppress("UNCHECKED_CAST")
                    val itemsList = (doc.get("items") as? List<Map<String, Any>>)?.map { map ->
                        TripItem(
                            name = map["name"] as? String ?: "",
                            storeId = map["storeId"] as? String,
                            storeName = map["storeName"] as? String,
                            price = (map["price"] as? Number)?.toDouble(),
                            quantity = (map["quantity"] as? Number)?.toInt() ?: 1
                        )
                    } ?: emptyList()

                    Trip(
                        id = doc.id,
                        householdId = householdId,
                        completedBy = doc.getString("completedBy") ?: "",
                        completedByName = doc.getString("completedByName") ?: "",
                        completedAt = doc.getLong("completedAt") ?: 0L,
                        totalEstimated = doc.getDouble("totalEstimated") ?: 0.0,
                        items = itemsList
                    )
                } ?: emptyList()
                trySend(trips)
            }
        awaitClose { registration.remove() }
    }

    suspend fun completeTrip(trip: Trip): Trip {
        val now = System.currentTimeMillis()
        val ref = tripsCollection(trip.householdId).document()
        ref.set(mapOf(
            "completedBy" to trip.completedBy,
            "completedByName" to trip.completedByName,
            "completedAt" to now,
            "totalEstimated" to trip.totalEstimated,
            "items" to trip.items.map { item ->
                mapOf(
                    "name" to item.name,
                    "storeId" to item.storeId,
                    "storeName" to item.storeName,
                    "price" to item.price,
                    "quantity" to item.quantity
                )
            }
        )).await()
        return trip.copy(id = ref.id, completedAt = now)
    }

    suspend fun deleteTrip(householdId: String, tripId: String) {
        tripsCollection(householdId).document(tripId).delete().await()
    }

    fun observeTripDetail(householdId: String, tripId: String): Flow<Trip?> = callbackFlow {
        val registration: ListenerRegistration = tripsCollection(householdId).document(tripId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val doc = snapshot
                if (doc == null || !doc.exists()) {
                    trySend(null)
                    return@addSnapshotListener
                }

                @Suppress("UNCHECKED_CAST")
                val itemsList = (doc.get("items") as? List<Map<String, Any>>)?.map { map ->
                    TripItem(
                        name = map["name"] as? String ?: "",
                        storeId = map["storeId"] as? String,
                        storeName = map["storeName"] as? String,
                        price = (map["price"] as? Number)?.toDouble(),
                        quantity = (map["quantity"] as? Number)?.toInt() ?: 1
                    )
                } ?: emptyList()

                trySend(
                    Trip(
                        id = doc.id,
                        householdId = householdId,
                        completedBy = doc.getString("completedBy") ?: "",
                        completedByName = doc.getString("completedByName") ?: "",
                        completedAt = doc.getLong("completedAt") ?: 0L,
                        totalEstimated = doc.getDouble("totalEstimated") ?: 0.0,
                        items = itemsList
                    )
                )
            }
        awaitClose { registration.remove() }
    }
}
