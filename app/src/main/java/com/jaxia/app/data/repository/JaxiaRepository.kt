package com.jaxia.app.data.repository

import com.jaxia.app.data.DefaultSeedData
import com.jaxia.app.ui.format.formatCop
import com.jaxia.app.data.dao.AvatarDao
import com.jaxia.app.data.dao.CommunityDao
import com.jaxia.app.data.dao.GarmentDao
import com.jaxia.app.data.dao.OutfitDao
import com.jaxia.app.data.dao.SecondChanceDao
import com.jaxia.app.data.model.AvatarProfile
import com.jaxia.app.data.model.CommunityPost
import com.jaxia.app.data.model.Garment
import com.jaxia.app.data.model.Outfit
import com.jaxia.app.data.model.OutfitFeedback
import com.jaxia.app.data.model.SecondChanceItem
import kotlinx.coroutines.CoroutineScope
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

private const val ACTIVE_WEAR_THRESHOLD = 3
private const val FORGOTTEN_WEAR_THRESHOLD = 2

class JaxiaRepository(
    private val garmentDao: GarmentDao,
    private val avatarDao: AvatarDao,
    private val outfitDao: OutfitDao,
    private val communityDao: CommunityDao,
    private val secondChanceDao: SecondChanceDao,
    externalScope: CoroutineScope,
) {
    val allGarments: Flow<List<Garment>> = garmentDao.getAllGarments()
    val forgottenGarments: Flow<List<Garment>> = garmentDao.getForgottenGarments()
    val avatarProfile: Flow<AvatarProfile?> = avatarDao.getAvatarProfile()
    val allOutfits: Flow<List<Outfit>> = outfitDao.getAllOutfits()
    val communityPosts: Flow<List<CommunityPost>> = communityDao.getAllPosts()
    val allFeedbacks: Flow<List<OutfitFeedback>> = outfitDao.getAllFeedbacks()
    val secondChanceItems: Flow<List<SecondChanceItem>> = secondChanceDao.getAllItems()

    init {
        // Seeding runs on the application-owned scope passed in by
        // JaxiaApplication. It used to spin up its own
        // CoroutineScope(Dispatchers.IO) that nothing could cancel, leaking one
        // scope per Activity re-creation (AUDITORIA.md A-03).
        externalScope.launch { seedInitialDataIfNeeded() }
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

    /**
     * Erases every piece of user-entered data: closet, avatar, outfits,
     * feedback, community posts and listings. Backs the in-app "delete my data"
     * control that Google Play requires (AUDITORIA.md A-11).
     *
     * Seed content is reinserted afterwards so the app returns to a usable
     * first-run state rather than an empty shell.
     */
    suspend fun deleteAllUserData() {
        garmentDao.deleteAllGarments()
        avatarDao.deleteAvatarProfile()
        outfitDao.deleteAllFeedbacks()
        outfitDao.deleteAllOutfits()
        communityDao.deleteAllPosts()
        secondChanceDao.deleteAllItems()
        seedInitialDataIfNeeded()
    }

    suspend fun saveOutfit(outfit: Outfit): Long = outfitDao.insertOutfit(outfit)

    /**
     * Generates proposals and persists them so each one gets a real primary key.
     *
     * Previously the generated outfits were handed to the UI with the default
     * `id = 0`. Every proposal therefore shared the same key, so "marcar como
     * favorito" ran `UPDATE outfits ... WHERE id = 0`, matched no row and
     * silently did nothing — and outfit feedback was filed against a
     * non-existent outfit (AUDITORIA.md A-13).
     */
    suspend fun generateAndPersistProposals(
        occasion: String,
        mood: String,
        allGarments: List<Garment>
    ): List<Outfit> {
        outfitDao.deleteTransientProposals()
        return generateOutfitsForOccasion(occasion, mood, allGarments).map { outfit ->
            outfit.copy(id = outfitDao.insertOutfit(outfit))
        }
    }

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

    /**
     * Derives closet metrics from data the user actually entered.
     *
     * The previous implementation invented the headline number: it multiplied
     * outfit count by a hardcoded "180.000 COP saved per outfit", floored the
     * outfit count at 3, and so reported "you saved $540.000" to somebody who
     * had never created an outfit (AUDITORIA.md A-10).
     *
     * Everything below is now traceable to a real input:
     *  - [reWearValueCOP] is the purchase price of garments the user has worn
     *    more than once, counted once per additional wear. It answers "what
     *    would these outfits have cost at this garment's own price, had I
     *    bought instead of re-worn?" — it is explicitly a *re-wear value*, not
     *    money that appeared in anyone's account, and the UI must say so.
     *  - [closetUtilizationRate] is a plain percentage of garments worn 3+ times.
     *  - [averageCostPerWear] is total spend divided by total wears.
     *
     * No floors, no magic constants, and zero inputs produce zero outputs.
     */
    fun calculateSavingsMetrics(garments: List<Garment>, outfits: List<Outfit>): SavingsMetrics {
        if (garments.isEmpty()) {
            return SavingsMetrics(
                reWearValueCOP = 0.0,
                closetUtilizationRate = 0,
                forgottenCount = 0,
                totalOutfitsGenerated = outfits.size,
                averageCostPerWear = 0.0,
                garmentsRescued = 0
            )
        }

        val activeGarments = garments.filter { it.wearCount >= ACTIVE_WEAR_THRESHOLD }
        val utilization = ((activeGarments.size.toFloat() / garments.size) * 100).toInt()
        val forgotten = garments.filter { it.isForgotten || it.wearCount <= FORGOTTEN_WEAR_THRESHOLD }

        val totalSpent = garments.sumOf { it.originalPrice }
        val totalWears = garments.sumOf { it.wearCount }
        val avgCostPerWear = if (totalWears > 0) totalSpent / totalWears else 0.0

        // Value unlocked by re-wearing: every wear after the first is one more
        // use of something already paid for.
        val reWearValue = garments.sumOf { garment ->
            val additionalWears = (garment.wearCount - 1).coerceAtLeast(0)
            garment.originalPrice * additionalWears
        }

        return SavingsMetrics(
            reWearValueCOP = reWearValue,
            closetUtilizationRate = utilization,
            forgottenCount = forgotten.size,
            totalOutfitsGenerated = outfits.size,
            averageCostPerWear = avgCostPerWear,
            garmentsRescued = activeGarments.size
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
                    "Antes de gastar ${formatCop(price)}, rescata tu '${similar.first().name}'. Lograrás el mismo look auténtico sin gastar un solo peso."
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
                    "Esta prenda dialoga con tus piezas actuales. Si la adquieres, su costo por uso estimado será muy bajo (${formatCop(estimatedCPW)} por postura)."
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

    /**
     * Builds three outfit proposals out of garments the user already owns.
     *
     * This is a deterministic rule engine, not a model. It is named accordingly
     * throughout the UI — the app previously shipped an unused `firebase-ai`
     * dependency and a Gemini capability flag while running exactly this code
     * (AUDITORIA.md §7).
     *
     * [occasion] and [mood] used to be accepted and then ignored, so every
     * request returned the same three looks with the same titles. They now
     * drive garment scoring and the copy.
     */
    fun generateOutfitsForOccasion(
        occasion: String,
        mood: String,
        allGarments: List<Garment>
    ): List<Outfit> {
        if (allGarments.isEmpty()) return emptyList()

        val tops = allGarments.filter { it.category == "Tops" }
        val bottoms = allGarments.filter { it.category == "Pantalones y Faldas" }
        val outers = allGarments.filter { it.category == "Abrigos y Chaquetas" }
        val shoes = allGarments.filter { it.category == "Calzado" }
        val accessories = allGarments.filter { it.category == "Accesorios y Bolsos" }
        val dresses = allGarments.filter { it.category == "Vestidos" }

        // Style keywords the occasion and mood pull towards. A garment whose
        // style/name matches scores higher and is picked first.
        val occasionKeywords = when {
            occasion.contains("trabajo", true) || occasion.contains("reunión", true) ->
                listOf("Elegante", "Sastre", "Blazer", "Formal", "Estructurado")
            occasion.contains("cita", true) || occasion.contains("noche", true) ->
                listOf("Elegante", "Vestido", "Seda", "Satín")
            occasion.contains("casual", true) || occasion.contains("finde", true) ->
                listOf("Casual", "Denim", "Jeans", "Cómodo", "Sneakers")
            occasion.contains("deporte", true) || occasion.contains("activo", true) ->
                listOf("Deportivo", "Cómodo", "Sneakers", "Algodón")
            else -> listOf("Versátil", "Clásico")
        }
        val moodKeywords = when {
            mood.contains("segura", true) || mood.contains("poder", true) ->
                listOf("Estructurado", "Elegante", "Sastre")
            mood.contains("cómoda", true) || mood.contains("relaj", true) ->
                listOf("Cómodo", "Suelto", "Algodón", "Lino")
            mood.contains("creativa", true) || mood.contains("audaz", true) ->
                listOf("Estampado", "Color", "Vintage")
            else -> emptyList()
        }
        val keywords = occasionKeywords + moodKeywords

        fun Garment.score(): Int =
            keywords.count { kw ->
                style.contains(kw, true) || name.contains(kw, true) || texture.contains(kw, true)
            }

        fun List<Garment>.bestFor(): Garment? =
            maxByOrNull { it.score() * 10 - it.wearCount }

        fun List<Garment>.mostForgotten(): Garment? =
            minByOrNull { it.wearCount }

        /** Sum of what the garments in this look originally cost. */
        fun lookValue(ids: List<Long>): Double =
            allGarments.filter { it.id in ids }.sumOf { it.originalPrice }

        fun buildOutfit(
            title: String,
            proposalType: String,
            ids: List<Long>,
            hairstyleTip: String,
            makeupTip: String,
        ) = Outfit(
            title = title,
            occasion = occasion,
            mood = mood,
            proposalType = proposalType,
            garmentIds = ids.joinToString(","),
            hairstyleTip = hairstyleTip,
            makeupTip = makeupTip,
            // The real purchase value of the pieces in this look. Presented as
            // "what this look is worth, already in your closet" — never as money
            // saved (AUDITORIA.md A-10).
            estimatedSavings = lookValue(ids)
        )

        val outfits = mutableListOf<Outfit>()
        val accessory = accessories.bestFor()

        // 1. Best match for the stated occasion and mood.
        val recommended = listOfNotNull(
            outers.bestFor()?.id,
            tops.bestFor()?.id,
            bottoms.bestFor()?.id,
            shoes.bestFor()?.id,
            accessory?.id,
        )
        if (recommended.isNotEmpty()) {
            outfits.add(
                buildOutfit(
                    title = "Para $occasion: equilibrio auténtico",
                    proposalType = "Recomendado",
                    ids = recommended,
                    hairstyleTip = "Cabello con ondas ligeras o recogido medio natural",
                    makeupTip = "Piel luminosa, rubor en crema cálido y bálsamo labial",
                )
            )
        }

        // 2. Comfort-first alternative: a dress if there is one, otherwise the
        //    softest top/bottom pairing.
        val comfortShoe = shoes.filter {
            it.style.contains("Cómodo", true) || it.name.contains("Sneakers", true)
        }.bestFor() ?: shoes.lastOrNull()
        val casualOuter = outers.filter { it.name.contains("Denim", true) }.bestFor()
            ?: outers.lastOrNull()

        val comfortable = dresses.bestFor()?.let { dress ->
            listOfNotNull(casualOuter?.id, dress.id, comfortShoe?.id, accessory?.id)
        } ?: listOfNotNull(
            casualOuter?.id,
            tops.filter { it.texture.contains("Algodón", true) || it.texture.contains("Lino", true) }
                .bestFor()?.id ?: tops.bestFor()?.id,
            bottoms.filter { it.name.contains("Jeans", true) }.bestFor()?.id
                ?: bottoms.bestFor()?.id,
            comfortShoe?.id,
        )
        if (comfortable.isNotEmpty()) {
            outfits.add(
                buildOutfit(
                    title = "Alternativa cómoda: libertad y confianza",
                    proposalType = "Cómodo",
                    ids = comfortable,
                    hairstyleTip = "Coleta alta desenfadada o melena suelta relajada",
                    makeupTip = "Look 'no-makeup': cejas peinadas, bálsamo y protector con color",
                )
            )
        }

        // 3. Rescue look: deliberately centred on the least-worn piece.
        val rescuedPiece = allGarments.filter { it.isForgotten }.mostForgotten()
            ?: allGarments.mostForgotten()
        val rescued = listOfNotNull(
            outers.filter { it.id != rescuedPiece?.id }.bestFor()?.id,
            rescuedPiece?.id,
            bottoms.filter { it.id != rescuedPiece?.id }.bestFor()?.id,
            shoes.filter { it.id != rescuedPiece?.id }.bestFor()?.id,
            accessory?.id,
        ).distinct()
        if (rescued.isNotEmpty()) {
            outfits.add(
                buildOutfit(
                    title = rescuedPiece?.let { "Rescate con estilo: tu ${it.name}" }
                        ?: "Rescate con estilo",
                    proposalType = "Creativo",
                    ids = rescued,
                    hairstyleTip = "Trenza lateral desestructurada o accesorio vintage",
                    makeupTip = "Delineado sutil en tonos tierra y labial terracota suave",
                )
            )
        }

        return outfits
    }
}

/**
 * Closet metrics, all derived from user-entered data.
 *
 * [reWearValueCOP] is deliberately *not* called "savings": it is the accumulated
 * purchase value of repeat wears, not money saved. The UI must label it as such
 * (AUDITORIA.md A-10).
 */
data class SavingsMetrics(
    val reWearValueCOP: Double,
    val closetUtilizationRate: Int,
    val forgottenCount: Int,
    val totalOutfitsGenerated: Int,
    val averageCostPerWear: Double,
    val garmentsRescued: Int
)
