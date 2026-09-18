package com.jaxia.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jaxia.app.data.model.Outfit
import com.jaxia.app.data.repository.JaxiaRepository
import com.jaxia.app.ui.JaxiaViewModel
import com.jaxia.app.ui.JaxiaViewModelFactory
import com.jaxia.app.ui.components.AddGarmentDialog
import com.jaxia.app.ui.components.FeedbackSurveyDialog
import com.jaxia.app.ui.screens.AhorroScreen
import com.jaxia.app.ui.screens.ClosetScreen
import com.jaxia.app.ui.screens.CommunityScreen
import com.jaxia.app.ui.screens.HomeScreen
import com.jaxia.app.ui.screens.ProbadorAvatarScreen
import com.jaxia.app.ui.screens.VestirmeScreen
import com.jaxia.app.ui.screens.WelcomeScreen
import com.jaxia.app.ui.theme.CardBorder
import com.jaxia.app.ui.theme.EspressoTextPrimary
import com.jaxia.app.ui.theme.EspressoTextSecondary
import com.jaxia.app.ui.theme.LinenBackground
import com.jaxia.app.ui.theme.JaxiaTheme
import com.jaxia.app.ui.theme.TerracottaContainer
import com.jaxia.app.ui.theme.TerracottaPrimary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // The repository and database are owned by the Application, not rebuilt
        // here on every configuration change. Building them per-onCreate leaked
        // one uncancellable coroutine scope per rotation (AUDITORIA.md A-03).
        val repository = (application as JaxiaApplication).repository

        setContent {
            JaxiaTheme {
                // El ViewModel se construye ANTES de la bienvenida a propósito:
                // así Room carga el clóset mientras la pantalla está visible y
                // la app entra con los datos ya listos, sin parpadeo de lista
                // vacía.
                val viewModel: JaxiaViewModel = viewModel(
                    factory = JaxiaViewModelFactory(repository)
                )

                // rememberSaveable para que una rotación durante la bienvenida
                // no la reinicie, ni la vuelva a mostrar una vez terminada.
                var showWelcome by rememberSaveable { mutableStateOf(true) }

                if (showWelcome) {
                    WelcomeScreen(onFinished = { showWelcome = false })
                } else {
                    JaxiaMainApp(viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JaxiaMainApp(viewModel: JaxiaViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    // rememberSaveable: the previous `remember` sent the user back to "Inicio"
    // and dropped any open dialog on every rotation (AUDITORIA.md A-05).
    var activeTab by rememberSaveable { mutableStateOf(0) } // 0: Inicio, 1: Vísteme, 2: Probador, 3: Clóset, 4: Ahorro, 5: Comunidad
    var showAddGarmentDialog by rememberSaveable { mutableStateOf(false) }
    // Only the id is saveable; the Outfit itself is re-read from state.
    var feedbackTargetOutfitId by rememberSaveable { mutableStateOf<Long?>(null) }
    val feedbackTargetOutfit: Outfit? = remember(feedbackTargetOutfitId, uiState.generatedProposals, uiState.outfits) {
        feedbackTargetOutfitId?.let { id ->
            uiState.generatedProposals.find { it.id == id } ?: uiState.outfits.find { it.id == id }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "JAXIA",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = TerracottaPrimary
                    )
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (activeTab == 5) TerracottaContainer else Color(0xFFF7F3EE),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (activeTab == 5) TerracottaPrimary else Color(0xFFEADBCE)
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { activeTab = if (activeTab == 5) 0 else 5 }
                            .padding(end = 12.dp)
                            .testTag("top_bar_community_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (activeTab == 5) Icons.Filled.People else Icons.Outlined.People,
                                contentDescription = "Comunidad con propósito",
                                tint = if (activeTab == 5) TerracottaPrimary else EspressoTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Comunidad",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (activeTab == 5) TerracottaPrimary else EspressoTextSecondary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 6.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TerracottaPrimary,
                        selectedTextColor = TerracottaPrimary,
                        indicatorColor = TerracottaContainer
                    ),
                    modifier = Modifier.testTag("nav_tab_home")
                )
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Vísteme") },
                    label = { Text("Vísteme", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TerracottaPrimary,
                        selectedTextColor = TerracottaPrimary,
                        indicatorColor = TerracottaContainer
                    ),
                    modifier = Modifier.testTag("nav_tab_vestirme")
                )
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Probador") },
                    label = { Text("Probador", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TerracottaPrimary,
                        selectedTextColor = TerracottaPrimary,
                        indicatorColor = TerracottaContainer
                    ),
                    modifier = Modifier.testTag("nav_tab_probador")
                )
                NavigationBarItem(
                    selected = activeTab == 3,
                    onClick = { activeTab = 3 },
                    icon = { Icon(Icons.Default.ShoppingBag, contentDescription = "Clóset") },
                    label = { Text("Clóset", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TerracottaPrimary,
                        selectedTextColor = TerracottaPrimary,
                        indicatorColor = TerracottaContainer
                    ),
                    modifier = Modifier.testTag("nav_tab_closet")
                )
                NavigationBarItem(
                    selected = activeTab == 4,
                    onClick = { activeTab = 4 },
                    icon = { Icon(Icons.Default.Savings, contentDescription = "Ahorro") },
                    label = { Text("Ahorro", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TerracottaPrimary,
                        selectedTextColor = TerracottaPrimary,
                        indicatorColor = TerracottaContainer
                    ),
                    modifier = Modifier.testTag("nav_tab_ahorro")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                0 -> HomeScreen(
                    uiState = uiState,
                    onNavigateToTab = { tab -> activeTab = tab },
                    onRescuedGarmentClick = { garment ->
                        viewModel.toggleGarmentWearing(garment)
                        activeTab = 2 // jump to probador virtual with this garment
                    },
                    onSelectRecommendedOutfit = { garmentIds ->
                        viewModel.wearGarmentSet(garmentIds)
                        activeTab = 2
                    },
                    onCycleDailyInspiration = { step ->
                        viewModel.cycleDailyInspiration(step)
                    }
                )
                1 -> VestirmeScreen(
                    uiState = uiState,
                    onRequestOutfits = { occ, mood ->
                        viewModel.requestOutfits(occ, mood)
                    },
                    onTryOnOutfit = { garmentIds ->
                        viewModel.wearGarmentSet(garmentIds)
                        activeTab = 2
                    },
                    onWearOutfitToday = { outfit ->
                        feedbackTargetOutfitId = outfit.id
                    },
                    onToggleFavorite = { id ->
                        viewModel.toggleFavoriteOutfit(id)
                    }
                )
                2 -> ProbadorAvatarScreen(
                    uiState = uiState,
                    onToggleGarmentWearing = { garment ->
                        viewModel.toggleGarmentWearing(garment)
                    },
                    onClearWearing = {
                        viewModel.clearProbador()
                    },
                    onUpdateAvatar = { profile ->
                        viewModel.updateAvatar(profile)
                    },
                    onDeleteAllData = {
                        viewModel.deleteAllUserData()
                    }
                )
                3 -> ClosetScreen(
                    uiState = uiState,
                    onAddGarmentClick = { showAddGarmentDialog = true },
                    onIncrementWear = { id -> viewModel.markGarmentWorn(id) },
                    onDeleteGarment = { id -> viewModel.deleteGarment(id) },
                    onTryOnGarment = { garment ->
                        viewModel.toggleGarmentWearing(garment)
                        activeTab = 2
                    }
                )
                4 -> AhorroScreen(
                    uiState = uiState,
                    onEvaluatePurchase = { name, price, cat, col, sty ->
                        viewModel.evaluateSmartPurchase(name, price, cat, col, sty)
                    },
                    onClearPurchaseAnalysis = {
                        viewModel.clearSmartPurchaseAnalysis()
                    },
                    onRescuedGarmentClick = { garment ->
                        viewModel.toggleGarmentWearing(garment)
                        activeTab = 2
                    }
                )
                5 -> CommunityScreen(
                    uiState = uiState,
                    onVoteOption = { postId, opt -> viewModel.voteCommunity(postId, opt) },
                    onToggleLike = { postId -> viewModel.toggleCommunityLike(postId) },
                    onCreatePost = { q, d, optA, optB, tag ->
                        viewModel.postCommunityConsultation(q, d, optA, optB, tag)
                    },
                    onToggleSecondChanceInterest = { id ->
                        viewModel.toggleSecondChanceInterest(id)
                    },
                    onPublishSecondChanceItem = { title, cat, act, size, cond, col, hex, exch, story, loc ->
                        viewModel.addSecondChanceItem(title, cat, act, size, cond, col, hex, exch, story, loc)
                    },
                    onCycleDailyInspiration = { step ->
                        viewModel.cycleDailyInspiration(step)
                    }
                )
            }
        }
    }

    // Add Garment Dialog
    if (showAddGarmentDialog) {
        AddGarmentDialog(
            onDismiss = { showAddGarmentDialog = false },
            onSave = { name, cat, col, hex, tex, sty, price, rep, priv ->
                viewModel.addGarment(name, cat, col, hex, tex, sty, price, rep, priv)
                showAddGarmentDialog = false
            }
        )
    }

    // Feedback Survey Dialog ("Autentica tu look")
    feedbackTargetOutfit?.let { outfit ->
        FeedbackSurveyDialog(
            outfit = outfit,
            onDismiss = { feedbackTargetOutfitId = null },
            onSubmit = { comfortable, authentic, confident, wouldRewear, fitOccasion, notes ->
                viewModel.recordFeedback(
                    outfit.id,
                    comfortable,
                    authentic,
                    confident,
                    wouldRewear,
                    fitOccasion,
                    notes
                )
                feedbackTargetOutfitId = null
            }
        )
    }
}
