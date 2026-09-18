package com.jaxia.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jaxia.app.R
import com.jaxia.app.data.model.Garment
import com.jaxia.app.ui.JaxiaUiState
import com.jaxia.app.ui.components.AvatarVisualizer
import com.jaxia.app.ui.components.SpiritualInspirationCard
import com.jaxia.app.ui.format.formatCop
import com.jaxia.app.ui.format.formatCopWithCode
import com.jaxia.app.ui.theme.CardBorder
import com.jaxia.app.ui.theme.EspressoTextMuted
import com.jaxia.app.ui.theme.EspressoTextPrimary
import com.jaxia.app.ui.theme.EspressoTextSecondary
import com.jaxia.app.ui.theme.HoneyContainer
import com.jaxia.app.ui.theme.HoneyPrimary
import com.jaxia.app.ui.theme.LinenBackground
import com.jaxia.app.ui.theme.SageContainer
import com.jaxia.app.ui.theme.SageSecondary
import com.jaxia.app.ui.theme.SuccessGreen
import com.jaxia.app.ui.theme.SuccessGreenBg
import com.jaxia.app.ui.theme.TerracottaContainer
import com.jaxia.app.ui.theme.TerracottaDark
import com.jaxia.app.ui.theme.TerracottaPrimary
import com.jaxia.app.ui.theme.WarmHoney
import com.jaxia.app.ui.theme.WarmHoneyContainer

@Composable
fun HomeScreen(
    uiState: JaxiaUiState,
    onNavigateToTab: (Int) -> Unit,
    onRescuedGarmentClick: (Garment) -> Unit,
    onSelectRecommendedOutfit: (List<Long>) -> Unit,
    onCycleDailyInspiration: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val forgottenPiece = uiState.garments.find { it.isForgotten } ?: uiState.garments.firstOrNull()
    val recommendedOutfit = uiState.outfits.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(LinenBackground)
            .testTag("home_screen_column"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header with branding & emotional quote
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hola, ${uiState.avatarProfile.userName} ✨",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = EspressoTextPrimary
                        )
                        Text(
                            text = "Vístete con lo que tienes. Autentica tu estilo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TerracottaPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Avatar mini preview
                    Surface(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .clickable { onNavigateToTab(2) }
                            .testTag("home_avatar_shortcut"),
                        color = TerracottaContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Face,
                                contentDescription = "Mi Avatar",
                                tint = TerracottaPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Emotional message card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF7EFE8),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEADBCE))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Spa,
                            contentDescription = null,
                            tint = TerracottaPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "“Como te sientes, te ves. Tu mejor look es el que refleja quién eres.”",
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                            color = EspressoTextSecondary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // 2. Editorial Hero Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_jaxia_hero),
                        contentDescription = "JAXIA Moda Consciente",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0xCC261E1B)
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Tu clóset tiene infinitas historias",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "No necesitas comprar más ropa, sino descubrir lo que ya posees.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE8DFD8)
                        )
                    }
                }
            }
        }

        // 3. Daily Spiritual & Personal Superación Message
        item {
            SpiritualInspirationCard(
                message = uiState.todayInspiration,
                onPreviousDay = { onCycleDailyInspiration(-1) },
                onNextDay = { onCycleDailyInspiration(1) }
            )
        }

        // 4. Day's Weather & Upcoming Event row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Weather Widget
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(WarmHoneyContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = "Clima",
                                tint = WarmHoney,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "21°C • Templado",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = EspressoTextPrimary
                            )
                            Text(
                                text = "Ideal para capas ligeras",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = EspressoTextMuted
                            )
                        }
                    }
                }

                // Event Widget
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SageContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = "Evento",
                                tint = SageSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Reunión laboral",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = EspressoTextPrimary
                            )
                            Text(
                                text = "10:30 AM • Presencial",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = EspressoTextMuted
                            )
                        }
                    }
                }
            }
        }

        // 4. Monthly Savings Milestone Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToTab(4) }
                    .testTag("home_savings_banner"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SuccessGreenBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC8E6C9))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(SuccessGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Savings,
                                contentDescription = "Ahorro",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Has ahorrado $420.000 COP",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )
                            Text(
                                text = "Reutilizaste 15 prendas y evitaste 4 compras impulsivas este mes.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF1B5E20),
                                fontSize = 12.sp
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Ver más",
                        tint = SuccessGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 5. Recommended Outfit of the Day
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Outfit recomendado de hoy",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EspressoTextPrimary
                    )
                    Text(
                        text = "Vísteme hoy →",
                        style = MaterialTheme.typography.labelMedium,
                        color = TerracottaPrimary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onNavigateToTab(1) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = TerracottaContainer
                            ) {
                                Text(
                                    text = "Recomendado para Reunión",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TerracottaDark,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Ahorro: $190.000 COP",
                                style = MaterialTheme.typography.labelSmall,
                                color = SuccessGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Render live dressed avatar miniature
                        AvatarVisualizer(
                            profile = uiState.avatarProfile,
                            wearingGarments = uiState.selectedWearingGarments,
                            height = 240.dp,
                            showPrivacyBadge = false
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = recommendedOutfit?.title ?: "Equilibrio Auténtico con Lino y Terracota",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = EspressoTextPrimary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Peinado: ${recommendedOutfit?.hairstyleTip ?: "Recogido bajo pulido"} • Maquillaje: ${recommendedOutfit?.makeupTip ?: "Tonos cálidos y nude"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = EspressoTextSecondary,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onNavigateToTab(1) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("home_wear_outfit_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Ver alternativas", fontSize = 13.sp)
                            }

                            OutlinedButton(
                                onClick = { onNavigateToTab(2) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("home_probador_shortcut"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Probar en Avatar", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        // 6. Prenda olvidada sugerida para rescatar
        if (forgottenPiece != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = WarmHoneyContainer),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFECD8BE))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ContentCut,
                                    contentDescription = null,
                                    tint = WarmHoney,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "¡Rescata una prenda olvidada!",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = WarmHoney
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.White
                            ) {
                                Text(
                                    text = "Solo ${forgottenPiece.wearCount} posturas",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EspressoTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = forgottenPiece.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = EspressoTextPrimary
                        )

                        Text(
                            text = "Costo por uso actual: ${formatCopWithCode(forgottenPiece.costPerWear)}. Con 3 usos más, bajará a la mitad.",
                            style = MaterialTheme.typography.bodySmall,
                            color = EspressoTextSecondary,
                            fontSize = 12.sp
                        )

                        if (forgottenPiece.repairNotes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "💡 Tip de ajuste: ${forgottenPiece.repairNotes}",
                                style = MaterialTheme.typography.bodySmall,
                                color = EspressoTextPrimary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { onRescuedGarmentClick(forgottenPiece) },
                            colors = ButtonDefaults.buttonColors(containerColor = WarmHoney),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("home_rescue_garment_btn")
                        ) {
                            Text("Combinar ahora en mi avatar", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 7. Quick feature grid
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Explorar JAXIA",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EspressoTextPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(
                        title = "Mi Clóset",
                        subtitle = "${uiState.garments.size} prendas guardadas",
                        icon = Icons.Default.ShoppingBag,
                        containerColor = TerracottaContainer,
                        iconTint = TerracottaPrimary,
                        modifier = Modifier.weight(1f)
                    ) { onNavigateToTab(3) }

                    QuickActionCard(
                        title = "Compra Inteligente",
                        subtitle = "Evalúa antes de comprar",
                        icon = Icons.Default.MonetizationOn,
                        containerColor = SageContainer,
                        iconTint = SageSecondary,
                        modifier = Modifier.weight(1f)
                    ) { onNavigateToTab(4) }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(
                        title = "Segunda Oportunidad",
                        subtitle = "${uiState.secondChanceItems.size} prendas para circular",
                        icon = Icons.Default.Loop,
                        containerColor = HoneyContainer,
                        iconTint = HoneyPrimary,
                        modifier = Modifier.weight(1f)
                    ) { onNavigateToTab(5) }

                    QuickActionCard(
                        title = "Comunidad",
                        subtitle = "${uiState.communityPosts.size} consultas y retos",
                        icon = Icons.Default.People,
                        containerColor = SageContainer,
                        iconTint = SageSecondary,
                        modifier = Modifier.weight(1f)
                    ) { onNavigateToTab(5) }
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(containerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = EspressoTextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = EspressoTextMuted
            )
        }
    }
}
