package com.market.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.market.domain.model.Store
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun storesCollection(householdId: String) =
        firestore.collection("households").document(householdId).collection("stores")

    fun observeStores(householdId: String): Flow<List<Store>> = callbackFlow {
        val registration: ListenerRegistration = storesCollection(householdId)
            .orderBy("order")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val stores = snapshot?.documents?.map { doc ->
                    Store(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        householdId = householdId,
                        order = doc.getLong("order")?.toInt() ?: 0,
                        createdAt = doc.getLong("createdAt") ?: 0L
                    )
                } ?: emptyList()
                trySend(stores)
            }
        awaitClose { registration.remove() }
    }

    suspend fun addStore(store: Store): Store {
        val now = System.currentTimeMillis()
        // Determine next order value
        val snapshot = storesCollection(store.householdId)
            .orderBy("order")
            .get()
            .await()
        val maxOrder = snapshot.documents.maxOfOrNull {
            it.getLong("order")?.toInt() ?: 0
        } ?: 0

        val ref = storesCollection(store.householdId).document()
        ref.set(mapOf(
            "name" to store.name,
            "order" to maxOrder + 1,
            "createdAt" to now
        )).await()
        return store.copy(id = ref.id, order = maxOrder + 1, createdAt = now)
    }

    suspend fun updateStore(store: Store) {
        storesCollection(store.householdId).document(store.id).update(mapOf(
            "name" to store.name
        )).await()
    }

    suspend fun deleteStore(householdId: String, storeId: String) {
        storesCollection(householdId).document(storeId).delete().await()
    }

    suspend fun reorderStores(householdId: String, storeIds: List<String>) {
        val col = storesCollection(householdId)
        firestore.runBatch { batch ->
            storeIds.forEachIndexed { index, storeId ->
                batch.update(col.document(storeId), "order", index + 1)
            }
        }.await()
    }
}
