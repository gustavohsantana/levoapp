package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.SyncAction
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncQueueDao {
    @Query("SELECT * FROM sync_queue ORDER BY createdAt ASC")
    fun getAllPendingActions(): Flow<List<SyncAction>>

    @Query("SELECT * FROM sync_queue ORDER BY createdAt ASC")
    suspend fun getAllPendingActionsSync(): List<SyncAction>

    @Query("SELECT * FROM sync_queue ORDER BY createdAt ASC")
    suspend fun getPendingActionsList(): List<SyncAction>

    @Query("SELECT COUNT(*) FROM sync_queue")
    fun getPendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM sync_queue")
    suspend fun getPendingCountSync(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAction(action: SyncAction): Long

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun deleteActionById(id: Long)

    @Delete
    suspend fun deleteAction(action: SyncAction)

    @Query("DELETE FROM sync_queue")
    suspend fun clearAll()
}
