package com.aistudio.lookia

import com.aistudio.lookia.data.dao.AvatarDao
import com.aistudio.lookia.data.dao.CommunityDao
import com.aistudio.lookia.data.dao.GarmentDao
import com.aistudio.lookia.data.dao.OutfitDao
import com.aistudio.lookia.data.dao.SecondChanceDao
import com.aistudio.lookia.data.model.AvatarProfile
import com.aistudio.lookia.data.model.CommunityPost
import com.aistudio.lookia.data.model.Garment
import com.aistudio.lookia.data.model.Outfit
import com.aistudio.lookia.data.model.OutfitFeedback
import com.aistudio.lookia.data.model.SecondChanceItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

/**
 * Minimal in-memory DAO doubles.
 *
 * The pure-function tests (savings, purchase evaluation, outfit generation)
 * never reach the database, but [LookIARepository]'s constructor requires the
 * five DAOs. These keep the tests free of Robolectric and of a real Room
 * instance, so they run as plain JVM unit tests.
 */
object FakeDaos {

    fun garment(initial: List<Garment> = emptyList()) = object : GarmentDao {
        val rows = MutableStateFlow(initial)
        private var nextId = (initial.maxOfOrNull { it.id } ?: 0L) + 1

        override fun getAllGarments(): Flow<List<Garment>> = rows

        override fun getGarmentsByCategory(category: String): Flow<List<Garment>> =
            flowOf(rows.value.filter { it.category == category })

        override fun getForgottenGarments(): Flow<List<Garment>> =
            flowOf(rows.value.filter { it.isForgotten })

        override suspend fun getGarmentById(id: Long): Garment? =
            rows.value.find { it.id == id }

        override suspend fun insertGarment(garment: Garment): Long {
            val assigned = if (garment.id == 0L) nextId++ else garment.id
            rows.value = rows.value.filter { it.id != assigned } + garment.copy(id = assigned)
            return assigned
        }

        override suspend fun insertAllGarments(garments: List<Garment>) {
            garments.forEach { insertGarment(it) }
        }

        override suspend fun updateGarment(garment: Garment) {
            rows.value = rows.value.map { if (it.id == garment.id) garment else it }
        }

        override suspend fun incrementWear(id: Long, timestamp: Long) {
            rows.value = rows.value.map {
                if (it.id == id) {
                    it.copy(wearCount = it.wearCount + 1, lastWornTimestamp = timestamp, isForgotten = false)
                } else {
                    it
                }
            }
        }

        override suspend fun deleteGarmentById(id: Long) {
            rows.value = rows.value.filter { it.id != id }
        }

        override suspend fun deleteAllGarments() {
            rows.value = emptyList()
        }
    }

    fun avatar(initial: AvatarProfile? = null) = object : AvatarDao {
        val row = MutableStateFlow(initial)
        override fun getAvatarProfile(): Flow<AvatarProfile?> = row
        override suspend fun saveAvatarProfile(profile: AvatarProfile) { row.value = profile }
        override suspend fun deleteAvatarProfile() { row.value = null }
    }

    fun outfit(initial: List<Outfit> = emptyList()) = object : OutfitDao {
        val rows = MutableStateFlow(initial)
        val feedbacks = MutableStateFlow(emptyList<OutfitFeedback>())
        private var nextId = (initial.maxOfOrNull { it.id } ?: 0L) + 1

        override fun getAllOutfits(): Flow<List<Outfit>> = rows

        override fun getFavoriteOutfits(): Flow<List<Outfit>> =
            flowOf(rows.value.filter { it.isFavorite })

        override fun getOutfitsByOccasion(occasion: String): Flow<List<Outfit>> =
            flowOf(rows.value.filter { it.occasion == occasion })

        override suspend fun insertOutfit(outfit: Outfit): Long {
            val assigned = if (outfit.id == 0L) nextId++ else outfit.id
            rows.value = rows.value.filter { it.id != assigned } + outfit.copy(id = assigned)
            return assigned
        }

        override suspend fun insertAllOutfits(outfits: List<Outfit>) {
            outfits.forEach { insertOutfit(it) }
        }

        override suspend fun updateOutfit(outfit: Outfit) {
            rows.value = rows.value.map { if (it.id == outfit.id) outfit else it }
        }

        override suspend fun toggleFavorite(id: Long) {
            rows.value = rows.value.map {
                if (it.id == id) it.copy(isFavorite = !it.isFavorite) else it
            }
        }

        override suspend fun markOutfitWorn(id: Long) {
            rows.value = rows.value.map {
                if (it.id == id) it.copy(wornCount = it.wornCount + 1) else it
            }
        }

        override suspend fun insertFeedback(feedback: OutfitFeedback) {
            feedbacks.value = feedbacks.value + feedback
        }

        override fun getFeedbacksForOutfit(outfitId: Long): Flow<List<OutfitFeedback>> =
            flowOf(feedbacks.value.filter { it.outfitId == outfitId })

        override fun getAllFeedbacks(): Flow<List<OutfitFeedback>> = feedbacks

        override suspend fun deleteAllOutfits() { rows.value = emptyList() }

        override suspend fun deleteTransientProposals() {
            rows.value = rows.value.filter { it.isFavorite || it.wornCount > 0 }
        }

        override suspend fun deleteAllFeedbacks() { feedbacks.value = emptyList() }
    }

    fun community(initial: List<CommunityPost> = emptyList()) = object : CommunityDao {
        val rows = MutableStateFlow(initial)
        private var nextId = (initial.maxOfOrNull { it.id } ?: 0L) + 1

        override fun getAllPosts(): Flow<List<CommunityPost>> = rows

        override suspend fun insertPost(post: CommunityPost): Long {
            val assigned = if (post.id == 0L) nextId++ else post.id
            rows.value = rows.value.filter { it.id != assigned } + post.copy(id = assigned)
            return assigned
        }

        override suspend fun insertAllPosts(posts: List<CommunityPost>) {
            posts.forEach { insertPost(it) }
        }

        override suspend fun voteOptionA(id: Long) {
            rows.value = rows.value.map {
                if (it.id == id && it.userVoted == 0) it.copy(votesA = it.votesA + 1, userVoted = 1) else it
            }
        }

        override suspend fun voteOptionB(id: Long) {
            rows.value = rows.value.map {
                if (it.id == id && it.userVoted == 0) it.copy(votesB = it.votesB + 1, userVoted = 2) else it
            }
        }

        override suspend fun toggleLike(id: Long) {
            rows.value = rows.value.map {
                if (it.id == id) {
                    it.copy(
                        isLiked = !it.isLiked,
                        likesCount = if (it.isLiked) it.likesCount - 1 else it.likesCount + 1,
                    )
                } else {
                    it
                }
            }
        }

        override suspend fun deleteAllPosts() { rows.value = emptyList() }
    }

    fun secondChance(initial: List<SecondChanceItem> = emptyList()) = object : SecondChanceDao {
        val rows = MutableStateFlow(initial)
        private var nextId = (initial.maxOfOrNull { it.id } ?: 0L) + 1

        override fun getAllItems(): Flow<List<SecondChanceItem>> = rows

        override fun getItemsByAction(actionType: String): Flow<List<SecondChanceItem>> =
            flowOf(rows.value.filter { it.actionType == actionType })

        override suspend fun insertItem(item: SecondChanceItem): Long {
            val assigned = if (item.id == 0L) nextId++ else item.id
            rows.value = rows.value.filter { it.id != assigned } + item.copy(id = assigned)
            return assigned
        }

        override suspend fun insertAllItems(items: List<SecondChanceItem>) {
            items.forEach { insertItem(it) }
        }

        override suspend fun toggleInterest(id: Long) {
            rows.value = rows.value.map {
                if (it.id == id) {
                    it.copy(
                        isUserInterested = !it.isUserInterested,
                        interestedCount = if (it.isUserInterested) {
                            it.interestedCount - 1
                        } else {
                            it.interestedCount + 1
                        },
                    )
                } else {
                    it
                }
            }
        }

        override suspend fun deleteItemById(id: Long) {
            rows.value = rows.value.filter { it.id != id }
        }

        override suspend fun deleteAllItems() { rows.value = emptyList() }
    }
}
