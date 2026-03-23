package io.github.hitchclimber.universaltimers.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface BundleDao {
    @Query("SELECT * FROM bundles ORDER BY name ASC")
    fun observeAll(): Flow<List<BundleEntity>>

    @Upsert
    suspend fun upsert(entity: BundleEntity)

    @Delete
    suspend fun delete(entity: BundleEntity)
}
