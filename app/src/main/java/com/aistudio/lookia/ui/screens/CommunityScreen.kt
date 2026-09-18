package com.aistudio.lookia.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.lookia.data.model.CommunityPost
import com.aistudio.lookia.data.model.SecondChanceItem
import com.aistudio.lookia.ui.LookIAUiState
import com.aistudio.lookia.ui.components.AddSecondChanceDialog
import com.aistudio.lookia.ui.components.ConnectSecondChanceDialog
import com.aistudio.lookia.ui.components.SpiritualInspirationCard
import com.aistudio.lookia.ui.theme.CardBorder
import com.aistudio.lookia.ui.theme.EspressoTextMuted
import com.aistudio.lookia.ui.theme.EspressoTextPrimary
import com.aistudio.lookia.ui.theme.EspressoTextSecondary
import com.aistudio.lookia.ui.theme.HoneyContainer
import com.aistudio.lookia.ui.theme.HoneyPrimary
import com.aistudio.lookia.ui.theme.LinenBackground
import com.aistudio.lookia.ui.theme.SageContainer
import com.aistudio.lookia.ui.theme.SageSecondary
import com.aistudio.lookia.ui.theme.TerracottaContainer
import com.aistudio.lookia.ui.theme.TerracottaDark
import com.aistudio.lookia.ui.theme.TerracottaPrimary
import com.aistudio.lookia.ui.theme.WarmHoney
import com.aistudio.lookia.ui.theme.WarmHoneyContainer
import com.aistudio.lookia.ui.theme.WarningAmber
import com.aistudio.lookia.ui.theme.WarningAmberBg

@Composable
fun CommunityScreen(
    uiState: LookIAUiState,
    onVoteOption: (postId: Long, option: Int) -> Unit,
    onToggleLike: (postId: Long) -> Unit,
    onCreatePost: (question: String, desc: String, optA: String, optB: String, tag: String) -> Unit,
    onToggleSecondChanceInterest: (Long) -> Unit = {},
    onPublishSecondChanceItem: (
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
    ) -> Unit = { _, _, _, _, _, _, _, _, _, _ -> },
    onCycleDailyInspiration: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedCommunityTab by remember { mutableStateOf(0) } // 0: Segunda Oportunidad, 1: Consultas de Estilo
    var showCreatePostDialog by remember { mutableStateOf(false) }
    var showAddSecondChanceDialog by remember { mutableStateOf(false) }
    var itemToConnect by remember { mutableStateOf<SecondChanceItem?>(null) }
    var selectedFilter by remember { mutableStateOf("Todos") }

    val challenges = listOf(
        "7 días sin comprar ropa",
        "Una prenda, 5 looks",
        "Segunda vida a una prenda",
        "Semana del clóset consciente",
        "Trueque entre amigas",
        "30 looks con 10 prendas"
    )

    val secondChanceFilters = listOf(
        "Todos",
        "Regalo con amor",
        "Cambio / Trueque",
        "Venta consciente"
    )

    val filteredSecondChanceItems = if (selectedFilter == "Todos") {
        uiState.secondChanceItems
    } else {
        uiState.secondChanceItems.filter { it.actionType == selectedFilter }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(LinenBackground)
                .testTag("community_screen_column"),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 110.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Header with Title & Privacy Promise
            item {
                Column {
                    Text(
                        text = "Comunidad con Propósito",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = EspressoTextPrimary
                    )
                    Text(
                        text = "Moda circular, apoyo mutuo y elevación espiritual sin juicios.",
                        style = MaterialTheme.typography.bodySmall,
                        color = EspressoTextSecondary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF7F2ED)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = SageSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Espacio privado y seguro. Viste tu alma con autenticidad y solidaridad.",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = EspressoTextSecondary
                            )
                        }
                    }
                }
            }

            // 2. Daily Spiritual Inspiration & Superación Card
            item {
                SpiritualInspirationCard(
                    message = uiState.todayInspiration,
                    onPreviousDay = { onCycleDailyInspiration(-1) },
                    onNextDay = { onCycleDailyInspiration(1) }
                )
            }

            // 3. Sub-Navigation Tabs: Segunda Oportunidad vs Consultas de Looks
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    TabRow(
                        selectedTabIndex = selectedCommunityTab,
                        containerColor = Color.Transparent,
                        contentColor = TerracottaPrimary,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedCommunityTab]),
                                color = TerracottaPrimary
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedCommunityTab == 0,
                            onClick = { selectedCommunityTab = 0 },
                            text = {
                                Text(
                                    text = "Segunda Oportunidad",
                                    fontWeight = if (selectedCommunityTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Loop,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                        Tab(
                            selected = selectedCommunityTab == 1,
                            onClick = { selectedCommunityTab = 1 },
                            text = {
                                Text(
                                    text = "Consultas & Retos",
                                    fontWeight = if (selectedCommunityTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.QuestionAnswer,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }
            }

            // 4. Content according to selected Sub-Tab
            if (selectedCommunityTab == 0) {
                // --- TAB 0: SEGUNDA OPORTUNIDAD (CAMBIO, REGALO, VENTA) ---
                item {
                    Column {
                        // Section description banner
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = SageContainer.copy(alpha = 0.4f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🌱", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Espacio de Moda Circular",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = EspressoTextPrimary
                                    )
                                    Text(
                                        text = "Regala con amor, haz trueque o vende prendas a precios conscientes para que circulen.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = EspressoTextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Filter Chips
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(items = secondChanceFilters, key = { it }) { filter ->
                                val isSelected = selectedFilter == filter
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedFilter = filter },
                                    label = {
                                        Text(
                                            text = when (filter) {
                                                "Regalo con amor" -> "🎁 Regalo"
                                                "Cambio / Trueque" -> "🔄 Cambio"
                                                "Venta consciente" -> "🏷️ Venta"
                                                else -> "Todos"
                                            },
                                            fontSize = 11.sp
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = TerracottaPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                if (filteredSecondChanceItems.isEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("✨", fontSize = 32.sp)
                                Text(
                                    text = "Aún no hay prendas en esta categoría",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = EspressoTextPrimary
                                )
                                Text(
                                    text = "¡Sé la primera en compartir una prenda para regalo, trueque o venta consciente!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = EspressoTextSecondary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { showAddSecondChanceDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Dar segunda oportunidad a una prenda")
                                }
                            }
                        }
                    }
                } else {
                    items(items = filteredSecondChanceItems, key = { it.id }) { item ->
                        SecondChanceItemCard(
                            item = item,
                            onToggleInterest = { onToggleSecondChanceInterest(item.id) },
                            onConnect = { itemToConnect = item }
                        )
                    }
                }
            } else {
                // --- TAB 1: CONSULTAS & RETOS DE LOOKS ---
                item {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = WarmHoney,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Retos Conscientes Activos",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = EspressoTextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(items = challenges, key = { it }) { challenge ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color.White,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .clip(CircleShape)
                                                .background(WarmHoneyContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("🌱", fontSize = 12.sp)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = challenge,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = EspressoTextPrimary,
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = "Comunidad LookIA",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = EspressoTextMuted,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // LookIA has no backend: every post below except the user's own
                // is seed content shipped with the app. Saying so plainly keeps
                // the listing clear of Google Play's deceptive-content policy
                // and stops users believing they are talking to real people
                // (AUDITORIA.md A-09).
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = WarningAmberBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Contenido de ejemplo. La comunidad aún no está " +
                                    "conectada, así que estas publicaciones son " +
                                    "demostrativas y no provienen de otras usuarias. " +
                                    "Lo que publiques se guarda solo en tu dispositivo.",
                                style = MaterialTheme.typography.bodySmall,
                                color = EspressoTextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                // Community Advice Consultations Feed
                items(
                    items = uiState.communityPosts,
                    key = { it.id }
                ) { post ->
                    CommunityPostCard(
                        post = post,
                        onVote = { opt -> onVoteOption(post.id, opt) },
                        onToggleLike = { onToggleLike(post.id) }
                    )
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = {
                if (selectedCommunityTab == 0) {
                    showAddSecondChanceDialog = true
                } else {
                    showCreatePostDialog = true
                }
            },
            containerColor = TerracottaPrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 96.dp, end = 20.dp)
                .testTag("community_fab_create")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = if (selectedCommunityTab == 0) "Publicar prenda" else "Pedir Consejo"
            )
        }
    }

    // Dialog: Create Outfit Question Post
    if (showCreatePostDialog) {
        CreatePostDialog(
            onDismiss = { showCreatePostDialog = false },
            onSubmit = { q, d, optA, optB, tag ->
                onCreatePost(q, d, optA, optB, tag)
                showCreatePostDialog = false
            }
        )
    }

    // Dialog: Add Second Chance Item (Cambio, Regalo o Venta)
    if (showAddSecondChanceDialog) {
        AddSecondChanceDialog(
            userGarments = uiState.garments,
            onDismiss = { showAddSecondChanceDialog = false },
            onPublish = { title, cat, act, size, cond, col, hex, exch, story, loc ->
                onPublishSecondChanceItem(title, cat, act, size, cond, col, hex, exch, story, loc)
                showAddSecondChanceDialog = false
            }
        )
    }

    // Dialog: Connect with owner of Second Chance Item
    itemToConnect?.let { item ->
        ConnectSecondChanceDialog(
            item = item,
            onDismiss = { itemToConnect = null },
            onConfirmConnect = {
                onToggleSecondChanceInterest(item.id)
            }
        )
    }
}

@Composable
fun SecondChanceItemCard(
    item: SecondChanceItem,
    onToggleInterest: () -> Unit,
    onConnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (badgeBg, badgeColor, badgeIcon) = when (item.actionType) {
        "Regalo con amor" -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), Icons.Default.CardGiftcard)
        "Cambio / Trueque" -> Triple(WarmHoneyContainer, Color(0xFF8A6200), Icons.Default.Loop)
        else -> Triple(TerracottaContainer, TerracottaPrimary, Icons.Default.Sell)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("second_chance_item_${item.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Action type badge + Owner info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = badgeBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = badgeIcon,
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = item.actionType,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(TerracottaContainer.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👤", fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.ownerName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = EspressoTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title & category
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = EspressoTextPrimary,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Attributes chips: Size, Condition, Color swatch, City
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFF7F3EE)
                ) {
                    Text(
                        text = "Talla ${item.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = EspressoTextPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFF7F3EE)
                ) {
                    Text(
                        text = item.condition,
                        style = MaterialTheme.typography.labelSmall,
                        color = EspressoTextPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(parseSafeColor(item.colorHex))
                            .border(0.5.dp, Color.Black.copy(alpha = 0.2f), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.color,
                        style = MaterialTheme.typography.labelSmall,
                        color = EspressoTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Exchange requirement / price banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFFDFBF9),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEE7DF))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Propuesta:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = EspressoTextSecondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.exchangeOrPrice,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (item.actionType == "Regalo con amor") Color(0xFF2E7D32) else TerracottaPrimary
                    )
                }
            }

            if (item.story.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "“${item.story}”",
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = EspressoTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer: Location, Interested count & Connect button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = EspressoTextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.location,
                        style = MaterialTheme.typography.labelSmall,
                        color = EspressoTextMuted,
                        fontSize = 11.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Interest count indicator
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onToggleInterest() }
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (item.isUserInterested) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Interesada",
                            tint = if (item.isUserInterested) TerracottaPrimary else EspressoTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${item.interestedCount}",
                            style = MaterialTheme.typography.labelSmall,
                            color = EspressoTextSecondary
                        )
                    }

                    Button(
                        onClick = onConnect,
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 12.dp,
                            vertical = 6.dp
                        )
                    ) {
                        Text(
                            text = "Me interesa",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CommunityPostCard(
    post: CommunityPost,
    onVote: (Int) -> Unit,
    onToggleLike: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalVotes = (post.votesA + post.votesB).coerceAtLeast(1)
    val pctA = ((post.votesA.toFloat() / totalVotes.toFloat()) * 100).toInt()
    val pctB = 100 - pctA

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("community_post_${post.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Author row with anonymous avatar tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(TerracottaContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👤", fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = post.authorName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = EspressoTextPrimary
                        )
                        Text(
                            text = "Avatar • ${post.authorAvatarBody}",
                            style = MaterialTheme.typography.bodySmall,
                            color = EspressoTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF3ECE6)
                ) {
                    Text(
                        text = post.challengeTag,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = TerracottaDark,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = post.question,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = EspressoTextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = post.lookDescription,
                style = MaterialTheme.typography.bodySmall,
                color = EspressoTextSecondary,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Voting Options A & B Cards
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Option A
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onVote(1) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (post.userVoted == 1) TerracottaContainer else Color(0xFFFAF7F4),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (post.userVoted == 1) TerracottaPrimary else CardBorder
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "A) ${post.optionATitle}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = EspressoTextPrimary
                            )
                            Text(
                                text = "$pctA%",
                                fontWeight = FontWeight.Bold,
                                color = if (post.userVoted == 1) TerracottaDark else EspressoTextMuted
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { pctA / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = TerracottaPrimary,
                            trackColor = Color(0xFFEDE6E0)
                        )
                    }
                }

                // Option B
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onVote(2) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (post.userVoted == 2) SageContainer else Color(0xFFFAF7F4),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (post.userVoted == 2) SageSecondary else CardBorder
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "B) ${post.optionBTitle}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = EspressoTextPrimary
                            )
                            Text(
                                text = "$pctB%",
                                fontWeight = FontWeight.Bold,
                                color = if (post.userVoted == 2) SageSecondary else EspressoTextMuted
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { pctB / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = SageSecondary,
                            trackColor = Color(0xFFEDE6E0)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Actions footer (Likes & comments count)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onToggleLike() }
                ) {
                    Icon(
                        imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (post.isLiked) TerracottaPrimary else EspressoTextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${post.likesCount} inspiradas",
                        style = MaterialTheme.typography.bodySmall,
                        color = EspressoTextSecondary,
                        fontSize = 12.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.QuestionAnswer,
                        contentDescription = null,
                        tint = EspressoTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    // The comment count used to be rendered here, but no
                    // comments screen exists anywhere in the app, so the number
                    // led nowhere (AUDITORIA.md A-09).
                    Text(
                        text = "Comentarios: próximamente",
                        style = MaterialTheme.typography.bodySmall,
                        color = EspressoTextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CreatePostDialog(
    onDismiss: () -> Unit,
    onSubmit: (question: String, desc: String, optA: String, optB: String, tag: String) -> Unit
) {
    var question by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var optA by remember { mutableStateOf("") }
    var optB by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf("Una prenda, 5 looks") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pedir opinión a la comunidad") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Tu consulta se compartirá únicamente con la representación de tu avatar.",
                    style = MaterialTheme.typography.bodySmall,
                    color = EspressoTextMuted
                )
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text("Pregunta") },
                    placeholder = { Text("Ej: ¿Cuál look uso para una entrevista de trabajo?") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Contexto o piezas usadas") },
                    placeholder = { Text("Ej: Ambas opciones usan mi blazer terracota...") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = optA,
                    onValueChange = { optA = it },
                    label = { Text("Opción A") },
                    placeholder = { Text("Ej: Con pantalón sastre y mocasines") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = optB,
                    onValueChange = { optB = it },
                    label = { Text("Opción B") },
                    placeholder = { Text("Ej: Con falda plisada y botines") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (question.isNotBlank()) {
                        onSubmit(question, desc, optA.ifBlank { "Opción 1" }, optB.ifBlank { "Opción 2" }, selectedTag)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
            ) {
                Text("Publicar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

fun parseSafeColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        Color(0xFFA84A33)
    }
}
