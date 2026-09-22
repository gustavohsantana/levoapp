package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Delivery
import kotlinx.coroutines.flow.Flow

@Dao
interface DeliveryDao {
    @Query("SELECT * FROM deliveries ORDER BY updatedAt DESC")
    fun getAllDeliveries(): Flow<List<Delivery>>

    @Query("SELECT * FROM deliveries WHERE id = :id LIMIT 1")
    fun getDeliveryById(id: String): Flow<Delivery?>

    @Query("SELECT * FROM deliveries WHERE id = :id LIMIT 1")
    suspend fun getDeliveryByIdSync(id: String): Delivery?

    @Query("SELECT * FROM deliveries WHERE isSynced = 0")
    suspend fun getUnsyncedDeliveries(): List<Delivery>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDelivery(delivery: Delivery)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeliveries(deliveries: List<Delivery>)

    @Update
    suspend fun updateDelivery(delivery: Delivery)

    @Delete
    suspend fun deleteDelivery(delivery: Delivery)

    @Query("SELECT COUNT(*) FROM deliveries")
    fun getDeliveryCount(): Flow<Int>
}
