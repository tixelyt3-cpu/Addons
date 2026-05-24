package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AddonDao {
    @Query("SELECT * FROM addons ORDER BY timestamp DESC")
    fun getAllAddons(): Flow<List<AddonEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddon(addon: AddonEntity)

    @Query("UPDATE addons SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Int, isFavorite: Boolean)

    @Query("UPDATE addons SET isSaved = :isSaved WHERE id = :id")
    suspend fun updateSaved(id: Int, isSaved: Boolean)

    @Query("UPDATE addons SET downloadsCount = downloadsCount + 1 WHERE id = :id")
    suspend fun incrementDownloads(id: Int)

    @Query("SELECT * FROM followed_creators")
    fun getFollowedCreators(): Flow<List<FollowedCreatorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun followCreator(creator: FollowedCreatorEntity)

    @Query("DELETE FROM followed_creators WHERE creatorName = :creatorName")
    suspend fun unfollowCreator(creatorName: String)
}
