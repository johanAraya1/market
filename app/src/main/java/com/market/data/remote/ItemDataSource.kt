package com.market.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.market.domain.model.ShoppingItem
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun itemsCollection(householdId: String) =
        firestore.collection("households").document(householdId).collection("items")

    fun observeItems(householdId: String): Flow<List<ShoppingItem>> = callbackFlow {
        val registration: ListenerRegistration = itemsCollection(householdId)
            .orderBy("createdAt")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.map { doc ->
                    ShoppingItem(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        householdId = householdId,
                        storeId = doc.getString("storeId"),
                        isChecked = doc.getBoolean("isChecked") ?: false,
                        checkedBy = doc.getString("checkedBy"),
                        checkedAt = doc.getLong("checkedAt"),
                        checkReason = doc.getString("checkReason"),
                        createdBy = doc.getString("createdBy") ?: "",
                        createdAt = doc.getLong("createdAt") ?: 0L,
                        updatedAt = doc.getLong("updatedAt") ?: 0L
                    )
                } ?: emptyList()
                trySend(items)
            }
        awaitClose { registration.remove() }
    }

    suspend fun addItem(item: ShoppingItem): ShoppingItem {
        val ref = itemsCollection(item.householdId).document()
        val now = System.currentTimeMillis()
        ref.set(mapOf(
            "name" to item.name,
            "storeId" to item.storeId,
            "isChecked" to false,
            "createdBy" to item.createdBy,
            "createdAt" to now,
            "updatedAt" to now
        )).await()
        return item.copy(id = ref.id, createdAt = now, updatedAt = now)
    }

    suspend fun updateItem(item: ShoppingItem) {
        itemsCollection(item.householdId).document(item.id).update(mapOf(
            "name" to item.name,
            "storeId" to item.storeId,
            "isChecked" to item.isChecked,
            "checkedBy" to item.checkedBy,
            "checkedAt" to item.checkedAt,
            "checkReason" to item.checkReason,
            "updatedAt" to System.currentTimeMillis()
        )).await()
    }

    suspend fun deleteItem(householdId: String, itemId: String) {
        itemsCollection(householdId).document(itemId).delete().await()
    }
}
