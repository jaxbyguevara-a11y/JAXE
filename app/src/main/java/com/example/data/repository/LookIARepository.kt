package com.example.data.repository

import com.example.data.DefaultSeedData
import com.example.data.dao.AvatarDao
import com.example.data.dao.CommunityDao
import com.example.data.dao.GarmentDao
import com.example.data.dao.OutfitDao
import com.example.data.dao.SecondChanceDao
import com.example.data.model.AvatarProfile
import com.example.data.model.CommunityPost
import com.example.data.model.Garment
import com.example.data.model.Outfit
import com.example.data.model.OutfitFeedback
import com.example.data.model.SecondChanceItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class SmartPurchaseAnalysis(
    val itemName: String,
    val price: Double,
    val category: String,
    val color: String,
    val style: String,
    val statusBadge: String, // "Compra útil", "Compra opcional", "Compra repetida", "Mejor reutiliza"
    val badgeReason: String,
    val similarItemsOwned: List<Garment>,
    val outfitsPossible: Int,
    val estimatedCostPerWear: Double,
    val adviceMessage: String
)

class LookIARepository(
    private val garmentDao: GarmentDao,
    private val avatarDao: AvatarDao,
    private val outfitDao: OutfitDao,
    private val communityDao: CommunityDao,
    private val secondChanceDao: SecondChanceDao
) {
    val allGarments: Flow<List<Garment>> = garmentDao.getAllGarments()
    val forgottenGarments: Flow<List<Garment>> = garmentDao.getForgottenGarments()
    val avatarProfile: Flow<AvatarProfile?> = avatarDao.getAvatarProfile()
    val allOutfits: Flow<List<Outfit>> = outfitDao.getAllOutfits()
    val communityPosts: Flow<List<CommunityPost>> = communityDao.getAllPosts()
    val allFeedbacks: Flow<List<OutfitFeedback>> = outfitDao.getAllFeedbacks()
    val secondChanceItems: Flow<List<SecondChanceItem>> = secondChanceDao.getAllItems()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedInitialDataIfNeeded()
        }
    }

    private suspend fun seedInitialDataIfNeeded() {
        val currentGarments = garmentDao.getAllGarments().first()
        if (currentGarments.isEmpty()) {
            garmentDao.insertAllGarments(DefaultSeedData.initialGarments)
        }
        val currentAvatar = avatarDao.getAvatarProfile().first()
        if (currentAvatar == null) {
            avatarDao.saveAvatarProfile(DefaultSeedData.initialAvatar)
        }
        val currentOutfits = outfitDao.getAllOutfits().first()
        if (currentOutfits.isEmpty()) {
            outfitDao.insertAllOutfits(DefaultSeedData.initialOutfits)
        }
        val currentPosts = communityDao.getAllPosts().first()
        if (currentPosts.isEmpty()) {
            communityDao.insertAllPosts(DefaultSeedData.initialCommunityPosts)
        }
        val currentSecondChance = secondChanceDao.getAllItems().first()
        if (currentSecondChance.isEmpty()) {
            secondChanceDao.insertAllItems(DefaultSeedData.initialSecondChanceItems)
        }
    }

    suspend fun insertGarment(garment: Garment): Long = garmentDao.insertGarment(garment)

    suspend fun updateGarment(garment: Garment) = garmentDao.updateGarment(garment)

    suspend fun deleteGarment(id: Long) = garmentDao.deleteGarmentById(id)

    suspend fun incrementWear(id: Long) = garmentDao.incrementWear(id)

    suspend fun saveAvatar(profile: AvatarProfile) = avatarDao.saveAvatarProfile(profile)

    suspend fun deleteAvatarScan() = avatarDao.deleteScanData()

    suspend fun saveOutfit(outfit: Outfit): Long = outfitDao.insertOutfit(outfit)

    suspend fun toggleFavoriteOutfit(id: Long) = outfitDao.toggleFavorite(id)

    suspend fun recordOutfitFeedback(feedback: OutfitFeedback) {
        outfitDao.insertFeedback(feedback)
        outfitDao.markOutfitWorn(feedback.outfitId)
    }

    suspend fun voteCommunity(postId: Long, option: Int) {
        if (option == 1) {
            communityDao.voteOptionA(postId)
        } else if (option == 2) {
            communityDao.voteOptionB(postId)
        }
    }

    suspend fun toggleCommunityLike(postId: Long) = communityDao.toggleLike(postId)

    suspend fun createCommunityPost(post: CommunityPost): Long = communityDao.insertPost(post)

    suspend fun insertSecondChanceItem(item: SecondChanceItem): Long = secondChanceDao.insertItem(item)

    suspend fun toggleSecondChanceInterest(id: Long) = secondChanceDao.toggleInterest(id)

    suspend fun deleteSecondChanceItem(id: Long) = secondChanceDao.deleteItemById(id)

    // Core Savings Calculation Engine
    fun calculateSavingsMetrics(garments: List<Garment>, outfits: List<Outfit>): SavingsMetrics {
        if (garments.isEmpty()) {
            return SavingsMetrics(
                totalSavingsCOP = 0.0,
                closetUtilizationRate = 0,
                forgottenCount = 0,
                totalOutfitsGenerated = outfits.size,
                averageCostPerWear = 0.0,
                impulsePurchasesAvoided = 0
            )
        }
        val activeGarments = garments.filter { it.wearCount >= 3 }
        val utilization = ((activeGarments.size.toFloat() / garments.size.toFloat()) * 100).toInt()
        val forgotten = garments.filter { it.isForgotten || it.wearCount <= 2 }

        val totalSpent = garments.sumOf { it.originalPrice }
        val totalWears = garments.sumOf { it.wearCount }.coerceAtLeast(1)
        val avgCostPerWear = totalSpent / totalWears

        val totalOutfitsCreated = outfits.size.coerceAtLeast(3)
        // Each conscious outfit avoids an impulsive purchase estimated at ~ $180,000 COP
        val totalSavingsCOP = (totalOutfitsCreated * 180000.0) + (activeGarments.size * 35000.0)
        val impulseAvoided = (totalOutfitsCreated * 0.85).toInt().coerceAtLeast(4)

        return SavingsMetrics(
            totalSavingsCOP = totalSavingsCOP,
            closetUtilizationRate = utilization,
            forgottenCount = forgotten.size,
            totalOutfitsGenerated = totalOutfitsCreated,
            averageCostPerWear = avgCostPerWear,
            impulsePurchasesAvoided = impulseAvoided
        )
    }

    // Smart Purchase Evaluator
    fun evaluateSmartPurchase(
        name: String,
        price: Double,
        category: String,
        color: String,
        style: String,
        allGarments: List<Garment>
    ): SmartPurchaseAnalysis {
        val similar = allGarments.filter {
            it.category.equals(category, ignoreCase = true) ||
            it.name.contains(name, ignoreCase = true) ||
            (it.color.contains(color, ignoreCase = true) && it.category.equals(category, ignoreCase = true))
        }

        // Count complementary garments to form new outfits
        val complementary = allGarments.filter { !it.category.equals(category, ignoreCase = true) }
        val possibleOutfits = (complementary.size * 0.6).toInt().coerceAtLeast(2)

        val estimatedCPW = if (possibleOutfits > 0) price / (possibleOutfits * 3) else price

        val (badge, reason, advice) = when {
            similar.isNotEmpty() && similar.any { it.wearCount < 3 } -> {
                Triple(
                    "Mejor reutiliza",
                    "Ya tienes ${similar.size} prenda(s) muy similar(es) en tu clóset, y algunas aún tienen pocos usos.",
                    "Antes de gastar $${price.toInt()}, rescata tu '${similar.first().name}'. Lograrás el mismo look auténtico sin gastar un solo peso."
                )
            }
            similar.size >= 2 -> {
                Triple(
                    "Compra repetida",
                    "Tu clóset ya cubre esta necesidad con ${similar.size} alternativas parecidas.",
                    "Ahorra este dinero para prendas que realmente llenen un vacío en tu cápsula o simplemente celébralo en tu contador de ahorro."
                )
            }
            possibleOutfits >= 6 -> {
                Triple(
                    "Compra útil",
                    "Amplía considerablemente tu clóset combinando con al menos $possibleOutfits prendas existentes.",
                    "Esta prenda dialoga con tus piezas actuales. Si la adquieres, su costo por uso estimado será muy bajo ($${estimatedCPW.toInt()} por postura)."
                )
            }
            else -> {
                Triple(
                    "Compra opcional",
                    "Combina con algunas prendas pero no es indispensable para tu estilo personal.",
                    "Evalúa si puedes crear un look equivalente utilizando tus piezas favoritas actuales."
                )
            }
        }

        return SmartPurchaseAnalysis(
            itemName = name,
            price = price,
            category = category,
            color = color,
            style = style,
            statusBadge = badge,
            badgeReason = reason,
            similarItemsOwned = similar,
            outfitsPossible = possibleOutfits,
            estimatedCostPerWear = estimatedCPW,
            adviceMessage = advice
        )
    }

    // Outfit generator from existing closet pieces
    fun generateOutfitsForOccasion(
        occasion: String,
        mood: String,
        allGarments: List<Garment>
    ): List<Outfit> {
        val tops = allGarments.filter { it.category == "Tops" }
        val bottoms = allGarments.filter { it.category == "Pantalones y Faldas" }
        val outers = allGarments.filter { it.category == "Abrigos y Chaquetas" }
        val shoes = allGarments.filter { it.category == "Calzado" }
        val accessories = allGarments.filter { it.category == "Accesorios y Bolsos" }
        val dresses = allGarments.filter { it.category == "Vestidos" }

        val outfits = mutableListOf<Outfit>()

        // 1. Look recomendado
        val top1 = tops.firstOrNull() ?: allGarments.firstOrNull()
        val bot1 = bottoms.firstOrNull() ?: allGarments.getOrNull(1)
        val outer1 = outers.firstOrNull()
        val shoe1 = shoes.firstOrNull() ?: allGarments.lastOrNull()
        val acc1 = accessories.firstOrNull()

        val list1 = listOfNotNull(outer1?.id, top1?.id, bot1?.id, shoe1?.id, acc1?.id)
        outfits.add(
            Outfit(
                title = "Look Recomendado: Equilibrio Auténtico",
                occasion = occasion,
                mood = mood,
                proposalType = "Recomendado",
                garmentIds = list1.joinToString(","),
                hairstyleTip = "Cabello con ondas ligeras o recogido medio natural",
                makeupTip = "Piel luminosa, rubor en crema cálido y bálsamo labial",
                estimatedSavings = 195000.0
            )
        )

        // 2. Alternativa cómoda
        val dress1 = dresses.firstOrNull()
        val shoeComfort = shoes.find { it.style.contains("Cómodo", ignoreCase = true) || it.name.contains("Sneakers", ignoreCase = true) }
            ?: shoes.lastOrNull()
        val outerCasual = outers.find { it.name.contains("Denim", ignoreCase = true) } ?: outers.lastOrNull()

        val list2 = if (dress1 != null) {
            listOfNotNull(outerCasual?.id, dress1.id, shoeComfort?.id, acc1?.id)
        } else {
            val topCasual = tops.getOrNull(1) ?: top1
            val botCasual = bottoms.find { it.name.contains("Jeans", ignoreCase = true) } ?: bot1
            listOfNotNull(outerCasual?.id, topCasual?.id, botCasual?.id, shoeComfort?.id)
        }
        outfits.add(
            Outfit(
                title = "Alternativa Cómoda: Libertad y Confianza",
                occasion = occasion,
                mood = mood,
                proposalType = "Cómodo",
                garmentIds = list2.joinToString(","),
                hairstyleTip = "Coleta alta desenfadada o melena suelta relajada",
                makeupTip = "Look 'no-makeup': cejas peinadas, bálsamo y protector con color",
                estimatedSavings = 170000.0
            )
        )

        // 3. Alternativa creativa (rescata prendas olvidadas o combinaciones audaces)
        val forgottenPiece = allGarments.find { it.isForgotten } ?: tops.lastOrNull()
        val creativeShoe = shoes.find { it.name.contains("Mocasines", ignoreCase = true) || it.name.contains("Botines", ignoreCase = true) }
            ?: shoes.firstOrNull()
        val creativeOuter = outers.find { it.name.contains("Blazer", ignoreCase = true) } ?: outer1
        val creativeBot = bottoms.find { it.name.contains("Plisada", ignoreCase = true) || it.name.contains("Sastre", ignoreCase = true) } ?: bot1

        val list3 = listOfNotNull(creativeOuter?.id, forgottenPiece?.id, creativeBot?.id, creativeShoe?.id, acc1?.id)
        outfits.add(
            Outfit(
                title = "Alternativa Creativa: Rescate con Estilo",
                occasion = occasion,
                mood = mood,
                proposalType = "Creativo",
                garmentIds = list3.joinToString(","),
                hairstyleTip = "Trenza lateral desestructurada o accesorio vintage",
                makeupTip = "Delineado sutil en tonos tierra y labial terracota suave",
                estimatedSavings = 220000.0
            )
        )

        return outfits
    }
}

data class SavingsMetrics(
    val totalSavingsCOP: Double,
    val closetUtilizationRate: Int,
    val forgottenCount: Int,
    val totalOutfitsGenerated: Int,
    val averageCostPerWear: Double,
    val impulsePurchasesAvoided: Int
)
