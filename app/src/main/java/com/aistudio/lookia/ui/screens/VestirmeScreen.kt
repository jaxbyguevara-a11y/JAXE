package com.aistudio.lookia.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.lookia.data.model.Garment
import com.aistudio.lookia.data.model.Outfit
import com.aistudio.lookia.ui.LookIAUiState
import com.aistudio.lookia.ui.components.AvatarVisualizer
import com.aistudio.lookia.ui.components.parseColorSafe
import com.aistudio.lookia.ui.format.formatCop
import com.aistudio.lookia.ui.format.formatCopWithCode
import com.aistudio.lookia.ui.theme.CardBorder
import com.aistudio.lookia.ui.theme.EspressoTextMuted
import com.aistudio.lookia.ui.theme.EspressoTextPrimary
import com.aistudio.lookia.ui.theme.EspressoTextSecondary
import com.aistudio.lookia.ui.theme.LinenBackground
import com.aistudio.lookia.ui.theme.SageContainer
import com.aistudio.lookia.ui.theme.SageSecondary
import com.aistudio.lookia.ui.theme.SuccessGreen
import com.aistudio.lookia.ui.theme.SuccessGreenBg
import com.aistudio.lookia.ui.theme.TerracottaContainer
import com.aistudio.lookia.ui.theme.TerracottaDark
import com.aistudio.lookia.ui.theme.TerracottaPrimary
import com.aistudio.lookia.ui.theme.WarmHoney
import com.aistudio.lookia.ui.theme.WarmHoneyContainer

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VestirmeScreen(
    uiState: LookIAUiState,
    onRequestOutfits: (occasion: String, mood: String) -> Unit,
    onTryOnOutfit: (List<Long>) -> Unit,
    onWearOutfitToday: (Outfit) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedOccasion by remember { mutableStateOf(uiState.currentOccasion) }
    var selectedMood by remember { mutableStateOf(uiState.currentMood) }
    var customNeedInput by remember { mutableStateOf("") }

    val popularOccasions = listOf(
        "Reunión de trabajo",
        "Entrevista laboral",
        "Cita romántica",
        "Cumpleaños",
        "Matrimonio",
        "Viaje",
        "Universidad",
        "Día casual",
        "Evento elegante",
        "Videollamada",
        "Clima frío",
        "Clima cálido"
    )

    val popularMoods = listOf(
        "Quiero sentirme segura",
        "Quiero verme moderna",
        "Quiero verme elegante sin comprar nada",
        "Comodidad absoluta",
        "Creativa y auténtica"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(LinenBackground)
            .testTag("vestirme_screen_column"),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Vísteme para...",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = EspressoTextPrimary
                )
                Text(
                    text = "LookIA buscará primero dentro de tu clóset y creará 3 combinaciones sin comprar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = EspressoTextSecondary
                )
            }
        }

        // 1. Selector de Ocasión
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. ¿Para qué ocasión te vistes hoy?",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = EspressoTextPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        popularOccasions.forEach { occasion ->
                            val isSelected = selectedOccasion == occasion
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedOccasion = occasion
                                    onRequestOutfits(selectedOccasion, selectedMood)
                                },
                                label = { Text(occasion, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TerracottaPrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFFF9F5F1),
                                    labelColor = EspressoTextSecondary
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = customNeedInput,
                        onValueChange = { customNeedInput = it },
                        placeholder = { Text("O escribe otra necesidad o evento...", fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_occasion_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TerracottaPrimary,
                            unfocusedBorderColor = CardBorder
                        ),
                        trailingIcon = {
                            if (customNeedInput.isNotBlank()) {
                                IconButton(onClick = {
                                    selectedOccasion = customNeedInput
                                    onRequestOutfits(selectedOccasion, selectedMood)
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Buscar",
                                        tint = TerracottaPrimary
                                    )
                                }
                            }
                        },
                        singleLine = true
                    )
                }
            }
        }

        // 2. Selector Emocional: "¿Cómo quieres sentirte?"
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = SageSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "2. ¿Cómo quieres sentirte hoy?",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = EspressoTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        popularMoods.forEach { mood ->
                            val isSelected = selectedMood == mood
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedMood = mood
                                    onRequestOutfits(selectedOccasion, selectedMood)
                                },
                                label = { Text(mood, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SageSecondary,
                                    selectedLabelColor = Color.White,
                                    containerColor = SageContainer.copy(alpha = 0.4f),
                                    labelColor = EspressoTextSecondary
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Section Title: 3 Propuestas
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "3 Propuestas desde tu clóset",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EspressoTextPrimary
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SuccessGreenBg
                ) {
                    Text(
                        text = "100% con tus prendas",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = SuccessGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Outfits List
        items(items = uiState.generatedProposals, key = { it.id }) { outfit ->
            val garmentIds = outfit.garmentIds.split(",")
                .mapNotNull { it.trim().toLongOrNull() }
            val piecesInOutfit = uiState.garments.filter { it.id in garmentIds }

            OutfitProposalCard(
                outfit = outfit,
                pieces = piecesInOutfit,
                profile = uiState.avatarProfile,
                onTryOn = { onTryOnOutfit(garmentIds) },
                onWearToday = { onWearOutfitToday(outfit) },
                onToggleFavorite = { onToggleFavorite(outfit.id) }
            )
        }
    }
}

@Composable
fun OutfitProposalCard(
    outfit: Outfit,
    pieces: List<Garment>,
    profile: com.aistudio.lookia.data.model.AvatarProfile,
    onTryOn: () -> Unit,
    onWearToday: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val badgeColors = when (outfit.proposalType) {
        "Recomendado" -> Pair(TerracottaContainer, TerracottaDark)
        "Cómodo" -> Pair(SageContainer, SageSecondary)
        else -> Pair(WarmHoneyContainer, WarmHoney)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("outfit_proposal_${outfit.proposalType.lowercase()}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with badge and favorite button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeColors.first
                ) {
                    Text(
                        text = outfit.proposalType.uppercase(),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeColors.second,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SuccessGreenBg
                    ) {
                        Text(
                            text = "Valor del look en tu clóset: ${formatCopWithCode(outfit.estimatedSavings)}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = SuccessGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (outfit.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint = if (outfit.isFavorite) TerracottaPrimary else EspressoTextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = outfit.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = EspressoTextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Dressed Avatar Visualizer representation
            AvatarVisualizer(
                profile = profile,
                wearingGarments = pieces,
                height = 250.dp,
                showPrivacyBadge = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Pieces list tags
            Text(
                text = "Prendas de tu clóset utilizadas:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = EspressoTextSecondary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                pieces.forEach { piece ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF9F6F3))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(parseColorSafe(piece.colorHex, Color.Gray))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = piece.name,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = EspressoTextPrimary
                            )
                        }
                        Text(
                            text = piece.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = EspressoTextMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Hairstyle and Makeup recommendations
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFAF5EF)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "✨ Recomendaciones conscientes:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TerracottaDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Peinado: ${outfit.hairstyleTip}",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = EspressoTextSecondary
                    )
                    Text(
                        text = "• Maquillaje: ${outfit.makeupTip}",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = EspressoTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onWearToday,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Usar este look hoy", fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = onTryOn,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Probar en Avatar", fontSize = 13.sp)
                }
            }
        }
    }
}
