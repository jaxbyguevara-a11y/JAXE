package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.DefaultSeedData
import com.example.data.model.AvatarProfile
import com.example.data.model.CommunityPost
import com.example.data.model.DailyInspirationProvider
import com.example.data.model.Garment
import com.example.data.model.Outfit
import com.example.data.model.OutfitFeedback
import com.example.data.model.SecondChanceItem
import com.example.data.model.SpiritualMessage
import com.example.data.repository.LookIARepository
import com.example.data.repository.SavingsMetrics
import com.example.data.repository.SmartPurchaseAnalysis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LookIAUiState(
    val garments: List<Garment> = emptyList(),
    val avatarProfile: AvatarProfile = DefaultSeedData.initialAvatar,
    val outfits: List<Outfit> = emptyList(),
    val communityPosts: List<CommunityPost> = emptyList(),
    val secondChanceItems: List<SecondChanceItem> = emptyList(),
    val selectedSecondChanceFilter: String = "Todos",
    val todayInspiration: SpiritualMessage = DailyInspirationProvider.getDailyMessage(),
    val savingsMetrics: SavingsMetrics = SavingsMetrics(
        totalSavingsCOP = 420000.0,
        closetUtilizationRate = 78,
        forgottenCount = 3,
        totalOutfitsGenerated = 8,
        averageCostPerWear = 12500.0,
        impulsePurchasesAvoided = 5
    ),
    val selectedWearingGarments: List<Garment> = emptyList(),
    val currentOccasion: String = "Reunión de trabajo",
    val currentMood: String = "Quiero sentirme segura",
    val generatedProposals: List<Outfit> = emptyList(),
    val smartPurchaseAnalysis: SmartPurchaseAnalysis? = null,
    val selectedCategoryFilter: String = "Todos",
    val activeTab: Int = 0 // 0: Inicio, 1: Vísteme, 2: Mi Avatar, 3: Mi Clóset, 4: Mi Ahorro, 5: Comunidad
)

class LookIAViewModel(
    private val repository: LookIARepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LookIAUiState())
    val uiState: StateFlow<LookIAUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.allGarments,
                repository.avatarProfile,
                repository.allOutfits,
                repository.communityPosts,
                repository.secondChanceItems
            ) { garments, avatar, outfits, posts, secondChance ->
                val profile = avatar ?: DefaultSeedData.initialAvatar
                val metrics = repository.calculateSavingsMetrics(garments, outfits)

                // Initialize default wearing garments if empty
                val currentWearing = _uiState.value.selectedWearingGarments.ifEmpty {
                    garments.filter { it.id == 1L || it.id == 3L || it.id == 6L || it.id == 10L }
                }

                val proposals = if (_uiState.value.generatedProposals.isEmpty()) {
                    repository.generateOutfitsForOccasion(
                        _uiState.value.currentOccasion,
                        _uiState.value.currentMood,
                        garments
                    )
                } else {
                    _uiState.value.generatedProposals
                }

                _uiState.value.copy(
                    garments = garments,
                    avatarProfile = profile,
                    outfits = outfits,
                    communityPosts = posts,
                    secondChanceItems = secondChance,
                    savingsMetrics = metrics,
                    selectedWearingGarments = currentWearing,
                    generatedProposals = proposals
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun selectTab(tab: Int) {
        _uiState.value = _uiState.value.copy(activeTab = tab)
    }

    fun setCategoryFilter(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategoryFilter = category)
    }

    // Toggle garment on/off in live Probador Virtual
    fun toggleGarmentWearing(garment: Garment) {
        val current = _uiState.value.selectedWearingGarments.toMutableList()
        val exists = current.any { it.id == garment.id }
        if (exists) {
            current.removeAll { it.id == garment.id }
        } else {
            // Replace piece of same category if single-slot (e.g. tops, bottoms, outerwear, shoes)
            if (garment.category in listOf("Tops", "Pantalones y Faldas", "Abrigos y Chaquetas", "Calzado", "Vestidos")) {
                current.removeAll { it.category == garment.category || (garment.category == "Vestidos" && it.category in listOf("Tops", "Pantalones y Faldas")) }
            }
            current.add(garment)
        }
        _uiState.value = _uiState.value.copy(selectedWearingGarments = current)
    }

    fun wearGarmentSet(garmentIds: List<Long>) {
        val all = _uiState.value.garments
        val selected = all.filter { it.id in garmentIds }
        _uiState.value = _uiState.value.copy(selectedWearingGarments = selected)
    }

    fun clearProbador() {
        _uiState.value = _uiState.value.copy(selectedWearingGarments = emptyList())
    }

    // Generate outfits by occasion and mood
    fun requestOutfits(occasion: String, mood: String) {
        val proposals = repository.generateOutfitsForOccasion(
            occasion,
            mood,
            _uiState.value.garments
        )
        _uiState.value = _uiState.value.copy(
            currentOccasion = occasion,
            currentMood = mood,
            generatedProposals = proposals
        )
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
            val currentWearing = _uiState.value.selectedWearingGarments.filter { it.id != id }
            _uiState.value = _uiState.value.copy(selectedWearingGarments = currentWearing)
        }
    }

    fun updateAvatar(profile: AvatarProfile) {
        viewModelScope.launch {
            repository.saveAvatar(profile)
        }
    }

    fun deleteAvatarScan() {
        viewModelScope.launch {
            repository.deleteAvatarScan()
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
        val analysis = repository.evaluateSmartPurchase(
            name = name,
            price = price,
            category = category,
            color = color,
            style = style,
            allGarments = _uiState.value.garments
        )
        _uiState.value = _uiState.value.copy(smartPurchaseAnalysis = analysis)
    }

    fun clearSmartPurchaseAnalysis() {
        _uiState.value = _uiState.value.copy(smartPurchaseAnalysis = null)
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
            val user = _uiState.value.avatarProfile
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
        _uiState.value = _uiState.value.copy(selectedSecondChanceFilter = filter)
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
            val user = _uiState.value.avatarProfile
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
        val currentIndex = all.indexOfFirst { it.dayNumber == _uiState.value.todayInspiration.dayNumber }
        val newIndex = (currentIndex + dayOffset + all.size) % all.size
        _uiState.value = _uiState.value.copy(todayInspiration = all[newIndex])
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
