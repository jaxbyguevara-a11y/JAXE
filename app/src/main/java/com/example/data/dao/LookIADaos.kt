package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AvatarProfile
import com.example.data.model.CommunityPost
import com.example.data.model.Garment
import com.example.data.model.Outfit
import com.example.data.model.OutfitFeedback
import com.example.data.model.SecondChanceItem
import kotlinx.coroutines.flow.Flow

@Dao
interface GarmentDao {
    @Query("SELECT * FROM garments ORDER BY lastWornTimestamp DESC")
    fun getAllGarments(): Flow<List<Garment>>

    @Query("SELECT * FROM garments WHERE category = :category ORDER BY lastWornTimestamp DESC")
    fun getGarmentsByCategory(category: String): Flow<List<Garment>>

    @Query("SELECT * FROM garments WHERE isForgotten = 1 ORDER BY lastWornTimestamp ASC")
    fun getForgottenGarments(): Flow<List<Garment>>

    @Query("SELECT * FROM garments WHERE id = :id")
    suspend fun getGarmentById(id: Long): Garment?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGarment(garment: Garment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllGarments(garments: List<Garment>)

    @Update
    suspend fun updateGarment(garment: Garment)

    @Query("UPDATE garments SET wearCount = wearCount + 1, lastWornTimestamp = :timestamp, isForgotten = 0 WHERE id = :id")
    suspend fun incrementWear(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM garments WHERE id = :id")
    suspend fun deleteGarmentById(id: Long)
}

@Dao
interface AvatarDao {
    @Query("SELECT * FROM avatar_profile WHERE id = 1 LIMIT 1")
    fun getAvatarProfile(): Flow<AvatarProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAvatarProfile(profile: AvatarProfile)

    @Query("UPDATE avatar_profile SET scanFrontPhoto = '', scanProfilePhoto = '', scanBackPhoto = '', creationMethod = 'manual' WHERE id = 1")
    suspend fun deleteScanData()
}

@Dao
interface OutfitDao {
    @Query("SELECT * FROM outfits ORDER BY timestamp DESC")
    fun getAllOutfits(): Flow<List<Outfit>>

    @Query("SELECT * FROM outfits WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteOutfits(): Flow<List<Outfit>>

    @Query("SELECT * FROM outfits WHERE occasion = :occasion ORDER BY timestamp DESC")
    fun getOutfitsByOccasion(occasion: String): Flow<List<Outfit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOutfit(outfit: Outfit): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllOutfits(outfits: List<Outfit>)

    @Update
    suspend fun updateOutfit(outfit: Outfit)

    @Query("UPDATE outfits SET isFavorite = NOT isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: Long)

    @Query("UPDATE outfits SET wornCount = wornCount + 1 WHERE id = :id")
    suspend fun markOutfitWorn(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedback(feedback: OutfitFeedback)

    @Query("SELECT * FROM outfit_feedbacks WHERE outfitId = :outfitId ORDER BY timestamp DESC")
    fun getFeedbacksForOutfit(outfitId: Long): Flow<List<OutfitFeedback>>

    @Query("SELECT * FROM outfit_feedbacks ORDER BY timestamp DESC")
    fun getAllFeedbacks(): Flow<List<OutfitFeedback>>
}

@Dao
interface CommunityDao {
    @Query("SELECT * FROM community_posts ORDER BY timestamp DESC")
    fun getAllPosts(): Flow<List<CommunityPost>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: CommunityPost): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPosts(posts: List<CommunityPost>)

    @Query("UPDATE community_posts SET votesA = votesA + 1, userVoted = 1 WHERE id = :id AND userVoted = 0")
    suspend fun voteOptionA(id: Long)

    @Query("UPDATE community_posts SET votesB = votesB + 1, userVoted = 2 WHERE id = :id AND userVoted = 0")
    suspend fun voteOptionB(id: Long)

    @Query("UPDATE community_posts SET isLiked = NOT isLiked, likesCount = CASE WHEN isLiked = 1 THEN likesCount - 1 ELSE likesCount + 1 END WHERE id = :id")
    suspend fun toggleLike(id: Long)
}

@Dao
interface SecondChanceDao {
    @Query("SELECT * FROM second_chance_items ORDER BY timestamp DESC")
    fun getAllItems(): Flow<List<SecondChanceItem>>

    @Query("SELECT * FROM second_chance_items WHERE actionType = :actionType ORDER BY timestamp DESC")
    fun getItemsByAction(actionType: String): Flow<List<SecondChanceItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: SecondChanceItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllItems(items: List<SecondChanceItem>)

    @Query("UPDATE second_chance_items SET isUserInterested = NOT isUserInterested, interestedCount = CASE WHEN isUserInterested = 1 THEN interestedCount - 1 ELSE interestedCount + 1 END WHERE id = :id")
    suspend fun toggleInterest(id: Long)

    @Query("DELETE FROM second_chance_items WHERE id = :id")
    suspend fun deleteItemById(id: Long)
}
