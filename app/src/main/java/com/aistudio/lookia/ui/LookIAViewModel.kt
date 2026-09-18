package com.aistudio.lookia.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aistudio.lookia.data.DefaultSeedData
import com.aistudio.lookia.data.model.AvatarProfile
import com.aistudio.lookia.data.model.CommunityPost
import com.aistudio.lookia.data.model.DailyInspirationProvider
import com.aistudio.lookia.data.model.Garment
import com.aistudio.lookia.data.model.Outfit
import com.aistudio.lookia.data.model.OutfitFeedback
import com.aistudio.lookia.data.model.SecondChanceItem
import com.aistudio.lookia.data.model.SpiritualMessage
import com.aistudio.lookia.data.repository.LookIARepository
import com.aistudio.lookia.data.repository.SavingsMetrics
import com.aistudio.lookia.data.repository.SmartPurchaseAnalysis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LookIAUiState(
    val garments: List<Garment> = emptyList(),
    val avatarProfile: AvatarProfile = DefaultSeedData.initialAvatar,
    val outfits: List<Outfit> = emptyList(),
    val communityPosts: List<CommunityPost> = emptyList(),
    val secondChanceItems: List<SecondChanceItem> = emptyList(),
    val selectedSecondChanceFilter: String = "Todos",
    val todayInspiration: SpiritualMessage = DailyInspirationProvider.getDailyMessage(),
    // Starts at zero. The old default advertised "$420.000 saved" to a user who
    // had not yet opened the closet (AUDITORIA.md A-10).
    val savingsMetrics: SavingsMetrics = SavingsMetrics(
        reWearValueCOP = 0.0,
        closetUtilizationRate = 0,
        forgottenCount = 0,
        totalOutfitsGenerated = 0,
        averageCostPerWear = 0.0,
        garmentsRescued = 0
    ),
    val selectedWearingGarments: List<Garment> = emptyList(),
    val currentOccasion: String = "Reunión de trabajo",
    val currentMood: String = "Quiero sentirme segura",
    val generatedProposals: List<Outfit> = emptyList(),
    val smartPurchaseAnalysis: SmartPurchaseAnalysis? = null,
    val selectedCategoryFilter: String = "Todos",
    val isLoading: Boolean = true
)

private val SINGLE_SLOT_CATEGORIES =
    listOf("Tops", "Pantalones y Faldas", "Abrigos y Chaquetas", "Calzado", "Vestidos")
private val TWO_PIECE_CATEGORIES = listOf("Tops", "Pantalones y Faldas")

class LookIAViewModel(
    private val repository: LookIARepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LookIAUiState())
    val uiState: StateFlow<LookIAUiState> = _uiState.asStateFlow()

    /**
     * Guards the one-time proposal seeding, so a Room emission arriving after
     * the user has already requested outfits does not overwrite them.
     */
    private var hasSeededProposals = false

    init {
        viewModelScope.launch {
            combine(
                repository.allGarments,
                repository.avatarProfile,
                repository.allOutfits,
                repository.communityPosts,
                repository.secondChanceItems
            ) { garments, avatar, outfits, posts, secondChance ->
                // Pure projection of the database rows. It must NOT read
                // _uiState: the previous version did, which let a Room emission
                // clobber proposals the user had just generated, and lose
                // try-on selections written concurrently (AUDITORIA.md A-04).
                DatabaseSnapshot(
                    garments = garments,
                    avatarProfile = avatar ?: DefaultSeedData.initialAvatar,
                    outfits = outfits,
                    communityPosts = posts,
                    secondChanceItems = secondChance,
                    savingsMetrics = repository.calculateSavingsMetrics(garments, outfits)
                )
            }.collect { snapshot ->
                // All merging happens in one atomic update, so concurrent UI
                // callbacks cannot be lost.
                _uiState.update { current ->
                    // Keep try-on selections pointing at rows that still exist,
                    // carrying their latest values.
                    val refreshedWearing = current.selectedWearingGarments
                        .mapNotNull { sel -> snapshot.garments.find { it.id == sel.id } }

                    current.copy(
                        garments = snapshot.garments,
                        avatarProfile = snapshot.avatarProfile,
                        outfits = snapshot.outfits,
                        communityPosts = snapshot.communityPosts,
                        secondChanceItems = snapshot.secondChanceItems,
                        savingsMetrics = snapshot.savingsMetrics,
                        selectedWearingGarments = refreshedWearing,
                        isLoading = false
                    )
                }

                // Seed the first proposal set once the closet has loaded.
                // Persisting happens outside the update block because it
                // suspends and update {} must stay side-effect free.
                if (!hasSeededProposals && snapshot.garments.isNotEmpty()) {
                    hasSeededProposals = true
                    val seeded = repository.generateAndPersistProposals(
                        _uiState.value.currentOccasion,
                        _uiState.value.currentMood,
                        snapshot.garments
                    )
                    _uiState.update { it.copy(generatedProposals = seeded) }
                }
            }
        }
    }

    /** Immutable projection of the five database streams. */
    private data class DatabaseSnapshot(
        val garments: List<Garment>,
        val avatarProfile: AvatarProfile,
        val outfits: List<Outfit>,
        val communityPosts: List<CommunityPost>,
        val secondChanceItems: List<SecondChanceItem>,
        val savingsMetrics: SavingsMetrics,
    )

    fun setCategoryFilter(category: String) {
        _uiState.update { it.copy(selectedCategoryFilter = category) }
    }

    // Toggle garment on/off in the live Probador Virtual.
    fun toggleGarmentWearing(garment: Garment) {
        _uiState.update { state ->
            val current = state.selectedWearingGarments.toMutableList()
            if (current.any { it.id == garment.id }) {
                current.removeAll { it.id == garment.id }
            } else {
                // Single-slot categories replace whatever occupies the slot; a
                // dress additionally displaces a separate top and bottom.
                if (garment.category in SINGLE_SLOT_CATEGORIES) {
                    current.removeAll {
                        it.category == garment.category ||
                            (garment.category == "Vestidos" && it.category in TWO_PIECE_CATEGORIES)
                    }
                }
                current.add(garment)
            }
            state.copy(selectedWearingGarments = current)
        }
    }

    fun wearGarmentSet(garmentIds: List<Long>) {
        _uiState.update { state ->
            state.copy(selectedWearingGarments = state.garments.filter { it.id in garmentIds })
        }
    }

    fun clearProbador() {
        _uiState.update { it.copy(selectedWearingGarments = emptyList()) }
    }

    // Generate outfits by occasion and mood.
    fun requestOutfits(occasion: String, mood: String) {
        viewModelScope.launch {
            hasSeededProposals = true
            val garments = _uiState.value.garments
            val proposals = repository.generateAndPersistProposals(occasion, mood, garments)
            _uiState.update {
                it.copy(
                    currentOccasion = occasion,
                    currentMood = mood,
                    generatedProposals = proposals
                )
            }
        }
    }

    fun addGarment(
        name: String,
        category: String,
        color: String,
        colorHex: String,
        texture: String,
        style: String,
        price: Double,
        repairNotes: String = "",
        privacy: String = "Privada"
    ) {
        viewModelScope.launch {
            repository.insertGarment(
                Garment(
                    name = name,
                    category = category,
                    color = color,
                    colorHex = colorHex,
                    texture = texture,
                    style = style,
                    originalPrice = price,
                    wearCount = 0,
                    isForgotten = false,
                    privacy = privacy,
                    repairNotes = repairNotes
                )
            )
        }
    }

    fun markGarmentWorn(id: Long) {
        viewModelScope.launch {
            repository.incrementWear(id)
        }
    }

    fun deleteGarment(id: Long) {
        viewModelScope.launch {
            repository.deleteGarment(id)
            _uiState.update { state ->
                state.copy(
                    selectedWearingGarments =
                        state.selectedWearingGarments.filter { it.id != id }
                )
            }
        }
    }

    fun updateAvatar(profile: AvatarProfile) {
        viewModelScope.launch {
            repository.saveAvatar(profile)
        }
    }

    /**
     * Wipes every piece of user data and returns the app to first-run state.
     * Backs the in-app deletion control Google Play requires (AUDITORIA.md A-11).
     */
    fun deleteAllUserData() {
        viewModelScope.launch {
            hasSeededProposals = false
            _uiState.update {
                it.copy(
                    selectedWearingGarments = emptyList(),
                    generatedProposals = emptyList(),
                    smartPurchaseAnalysis = null,
                )
            }
            repository.deleteAllUserData()
        }
    }

    fun toggleFavoriteOutfit(outfitId: Long) {
        viewModelScope.launch {
            repository.toggleFavoriteOutfit(outfitId)
        }
    }

    fun recordFeedback(
        outfitId: Long,
        feltComfortable: Boolean,
        feltAuthentic: Boolean,
        feltConfident: Boolean,
        wouldRewear: Boolean,
        fitOccasion: Boolean,
        notes: String
    ) {
        viewModelScope.launch {
            repository.recordOutfitFeedback(
                OutfitFeedback(
                    outfitId = outfitId,
                    feltComfortable = feltComfortable,
                    feltAuthentic = feltAuthentic,
                    feltConfident = feltConfident,
                    wouldRewear = wouldRewear,
                    fitOccasionWell = fitOccasion,
                    notes = notes
                )
            )
        }
    }

    fun evaluateSmartPurchase(
        name: String,
        price: Double,
        category: String,
        color: String,
        style: String
    ) {
        _uiState.update { state ->
            state.copy(
                smartPurchaseAnalysis = repository.evaluateSmartPurchase(
                    name = name,
                    price = price,
                    category = category,
                    color = color,
                    style = style,
                    allGarments = state.garments
                )
            )
        }
    }

    fun clearSmartPurchaseAnalysis() {
        _uiState.update { it.copy(smartPurchaseAnalysis = null) }
    }

    fun voteCommunity(postId: Long, option: Int) {
        viewModelScope.launch {
            repository.voteCommunity(postId, option)
        }
    }

    fun toggleCommunityLike(postId: Long) {
        viewModelScope.launch {
            repository.toggleCommunityLike(postId)
        }
    }

    fun postCommunityConsultation(
        question: String,
        desc: String,
        optA: String,
        optB: String,
        tag: String
    ) {
        viewModelScope.launch {
            val user = _uiState.value.avatarProfile // snapshot read; no mutation follows
            repository.createCommunityPost(
                CommunityPost(
                    authorName = user.userName,
                    authorAvatarBody = user.bodyType,
                    question = question,
                    lookDescription = desc,
                    optionATitle = optA,
                    optionBTitle = optB,
                    votesA = 0,
                    votesB = 0,
                    userVoted = 0,
                    likesCount = 1,
                    isLiked = false,
                    commentsCount = 0,
                    challengeTag = tag
                )
            )
        }
    }

    fun setSecondChanceFilter(filter: String) {
        _uiState.update { it.copy(selectedSecondChanceFilter = filter) }
    }

    fun addSecondChanceItem(
        title: String,
        category: String,
        actionType: String,
        size: String,
        condition: String,
        color: String,
        colorHex: String,
        exchangeOrPrice: String,
        story: String,
        location: String
    ) {
        viewModelScope.launch {
            val user = _uiState.value.avatarProfile // snapshot read; no mutation follows
            repository.insertSecondChanceItem(
                SecondChanceItem(
                    ownerName = user.userName,
                    ownerAvatarBody = user.bodyType,
                    title = title,
                    category = category,
                    actionType = actionType,
                    size = size,
                    condition = condition,
                    color = color,
                    colorHex = colorHex,
                    exchangeOrPrice = exchangeOrPrice,
                    story = story,
                    location = location.ifBlank { "Comunidad Local" }
                )
            )
        }
    }

    fun toggleSecondChanceInterest(id: Long) {
        viewModelScope.launch {
            repository.toggleSecondChanceInterest(id)
        }
    }

    fun cycleDailyInspiration(dayOffset: Int) {
        val all = DailyInspirationProvider.getAllMessages()
        if (all.isEmpty()) return
        _uiState.update { state ->
            val currentIndex =
                all.indexOfFirst { it.dayNumber == state.todayInspiration.dayNumber }
                    .coerceAtLeast(0)
            // Add all.size before the modulo so a negative offset still lands
            // on a valid index.
            val newIndex = ((currentIndex + dayOffset) % all.size + all.size) % all.size
            state.copy(todayInspiration = all[newIndex])
        }
    }
}

class LookIAViewModelFactory(
    private val repository: LookIARepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LookIAViewModel::class.java)) {
            return LookIAViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
