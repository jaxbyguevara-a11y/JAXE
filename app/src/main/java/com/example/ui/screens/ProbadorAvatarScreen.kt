package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.AvatarProfile
import com.example.data.model.Garment
import com.example.ui.LookIAUiState
import com.example.ui.components.AvatarVisualizer
import com.example.ui.components.parseColorSafe
import com.example.ui.theme.CardBorder
import com.example.ui.theme.EspressoTextMuted
import com.example.ui.theme.EspressoTextPrimary
import com.example.ui.theme.EspressoTextSecondary
import com.example.ui.theme.LinenBackground
import com.example.ui.theme.SageContainer
import com.example.ui.theme.SageSecondary
import com.example.ui.theme.TerracottaContainer
import com.example.ui.theme.TerracottaDark
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.theme.WarmHoney
import com.example.ui.theme.WarmHoneyContainer

@Composable
fun ProbadorAvatarScreen(
    uiState: LookIAUiState,
    onToggleGarmentWearing: (Garment) -> Unit,
    onClearWearing: () -> Unit,
    onUpdateAvatar: (AvatarProfile) -> Unit,
    onDeleteAvatarScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    var subTab by remember { mutableIntStateOf(0) } // 0: Probador Virtual, 1: Personalizar Manual, 2: Escaneo Corporal
    var showDeleteScanDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LinenBackground)
            .testTag("probador_avatar_screen")
    ) {
        // Tab Row
        TabRow(
            selectedTabIndex = subTab,
            containerColor = Color.White,
            contentColor = TerracottaPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[subTab]),
                    color = TerracottaPrimary
                )
            }
        ) {
            Tab(
                selected = subTab == 0,
                onClick = { subTab = 0 },
                text = { Text("Probador Virtual", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = subTab == 1,
                onClick = { subTab = 1 },
                text = { Text("Mi Avatar", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = subTab == 2,
                onClick = { subTab = 2 },
                text = { Text("Escanear Cuerpo", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
            )
        }

        when (subTab) {
            0 -> ProbadorVirtualContent(
                uiState = uiState,
                onToggleGarmentWearing = onToggleGarmentWearing,
                onClearWearing = onClearWearing
            )
            1 -> PersonalizarAvatarContent(
                profile = uiState.avatarProfile,
                onSaveProfile = onUpdateAvatar
            )
            2 -> EscanearCuerpoContent(
                profile = uiState.avatarProfile,
                onSaveProfile = onUpdateAvatar,
                onDeleteScan = { showDeleteScanDialog = true }
            )
        }
    }

    if (showDeleteScanDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteScanDialog = false },
            title = { Text("Eliminar escaneo corporal definitivamente") },
            text = {
                Text("Se borrarán permanentemente las fotografías y datos antropométricos asociados al escáner. Tu avatar continuará activo en modo personalizado manual.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteAvatarScan()
                        showDeleteScanDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                ) {
                    Text("Eliminar definitivamente")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteScanDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// Sub-Tab 0: Probador Virtual en Vivo
@Composable
fun ProbadorVirtualContent(
    uiState: LookIAUiState,
    onToggleGarmentWearing: (Garment) -> Unit,
    onClearWearing: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("Todos") }
    val categories = listOf("Todos", "Tops", "Pantalones y Faldas", "Abrigos y Chaquetas", "Calzado", "Accesorios y Bolsos", "Vestidos")

    val filteredGarments = if (selectedCategory == "Todos") {
        uiState.garments
    } else {
        uiState.garments.filter { it.category == selectedCategory }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp, 16.dp, 16.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Disclaimer & Avatar Display
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Probador Virtual en Vivo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = EspressoTextPrimary
                        )
                        Text(
                            text = "Toca prendas de tu clóset para combinarlas sobre tu avatar.",
                            style = MaterialTheme.typography.bodySmall,
                            color = EspressoTextSecondary
                        )
                    }

                    if (uiState.selectedWearingGarments.isNotEmpty()) {
                        TextButton(
                            onClick = onClearWearing,
                            modifier = Modifier.testTag("probador_clear_btn")
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Desvestir", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Avatar Canvas Visualizer
                AvatarVisualizer(
                    profile = uiState.avatarProfile,
                    wearingGarments = uiState.selectedWearingGarments,
                    height = 290.dp,
                    showPrivacyBadge = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Orientation disclaimer from user requirement
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF7EFE8)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = TerracottaPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Simulación orientativa de estilo y proporciones según material y corte.",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = EspressoTextSecondary
                        )
                    }
                }
            }
        }

        // Active Wearing Pills
        item {
            Column {
                Text(
                    text = "Prendas puestas (${uiState.selectedWearingGarments.size}):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = EspressoTextPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (uiState.selectedWearingGarments.isEmpty()) {
                    Text(
                        text = "Selecciona una prenda abajo para comenzar a probar combinaciones.",
                        style = MaterialTheme.typography.bodySmall,
                        color = EspressoTextMuted
                    )
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.selectedWearingGarments) { garment ->
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = TerracottaContainer,
                                border = androidx.compose.foundation.BorderStroke(1.dp, TerracottaPrimary.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(parseColorSafe(garment.colorHex, Color.Gray))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = garment.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TerracottaDark,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Quitar",
                                        tint = TerracottaPrimary,
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable { onToggleGarmentWearing(garment) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Category Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TerracottaPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = EspressoTextSecondary
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
        }

        // Closet Garments Grid for Probador
        items(filteredGarments) { garment ->
            val isWearing = uiState.selectedWearingGarments.any { it.id == garment.id }
            ProbadorGarmentRow(
                garment = garment,
                isWearing = isWearing,
                onToggle = { onToggleGarmentWearing(garment) }
            )
        }
    }
}

@Composable
fun ProbadorGarmentRow(
    garment: Garment,
    isWearing: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onToggle() },
        shape = RoundedCornerShape(14.dp),
        color = if (isWearing) TerracottaContainer.copy(alpha = 0.5f) else Color.White,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isWearing) TerracottaPrimary else CardBorder
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(parseColorSafe(garment.colorHex, Color.Gray))
                        .border(1.dp, Color(0x33000000), RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = garment.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = EspressoTextPrimary
                    )
                    Text(
                        text = "${garment.category} • ${garment.texture} • ${garment.color}",
                        style = MaterialTheme.typography.bodySmall,
                        color = EspressoTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isWearing) TerracottaPrimary else Color(0xFFF3ECE6)
            ) {
                Text(
                    text = if (isWearing) "Puesto ✓" else "+ Probar",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isWearing) Color.White else EspressoTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Sub-Tab 1: Opción B - Personalizar Avatar Manualmente
@Composable
fun PersonalizarAvatarContent(
    profile: AvatarProfile,
    onSaveProfile: (AvatarProfile) -> Unit
) {
    var name by remember { mutableStateOf(profile.userName) }
    var bodyType by remember { mutableStateOf(profile.bodyType) }
    var heightCm by remember { mutableFloatStateOf(profile.heightCm.toFloat()) }
    var selectedSkinTone by remember { mutableStateOf(profile.skinToneHex) }
    var selectedHairColor by remember { mutableStateOf(profile.hairColorHex) }
    var hairStyle by remember { mutableStateOf(profile.hairStyle) }
    var topSize by remember { mutableStateOf(profile.topSize) }
    var bottomSize by remember { mutableStateOf(profile.bottomSize) }
    var footwearSize by remember { mutableStateOf(profile.footwearSize) }
    var comfortPreference by remember { mutableStateOf(profile.comfortPreference) }

    val bodyTypes = listOf("Reloj de arena", "Rectangular", "Triángulo", "Triángulo invertido", "Ovalado")
    val hairStyles = listOf("Ondulado largo", "Lacio medio", "Rizado corto", "Corte Bob", "Recogido elegante")
    val skinTones = listOf("#F7D7C4", "#E8BFA3", "#D99B77", "#B97652", "#874D32", "#4F2B1D")
    val hairColors = listOf("#1A1A1A", "#3B2219", "#5D3724", "#8B5A2B", "#D2A26C", "#9E2A2B")
    val comfortOptions = listOf("Muy holgado", "Equilibrado", "Ajustado y estilizado")

    val tempProfile = profile.copy(
        userName = name,
        bodyType = bodyType,
        heightCm = heightCm.toInt(),
        skinToneHex = selectedSkinTone,
        hairColorHex = selectedHairColor,
        hairStyle = hairStyle,
        topSize = topSize,
        bottomSize = bottomSize,
        footwearSize = footwearSize,
        comfortPreference = comfortPreference
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp, 16.dp, 16.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Opción B: Personalizar Avatar",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = EspressoTextPrimary
            )
            Text(
                text = "Crea un avatar a tu imagen sin imponer estereotipos de belleza ni tallas irreales.",
                style = MaterialTheme.typography.bodySmall,
                color = EspressoTextSecondary
            )
        }

        // Live Preview of custom avatar
        item {
            AvatarVisualizer(
                profile = tempProfile,
                height = 260.dp,
                showPrivacyBadge = true
            )
        }

        // Silueta Corporal
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Silueta corporal aproximada:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = EspressoTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(bodyTypes) { bType ->
                            FilterChip(
                                selected = bodyType == bType,
                                onClick = { bodyType = bType },
                                label = { Text(bType, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TerracottaPrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFFF8F4F0),
                                    labelColor = EspressoTextSecondary
                                )
                            )
                        }
                    }
                }
            }
        }

        // Estatura aproximada Slider
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Estatura aproximada:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = EspressoTextPrimary
                        )
                        Text(
                            text = "${heightCm.toInt()} cm",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = TerracottaPrimary
                        )
                    }
                    Slider(
                        value = heightCm,
                        onValueChange = { heightCm = it },
                        valueRange = 145f..190f,
                        steps = 45,
                        colors = SliderDefaults.colors(
                            thumbColor = TerracottaPrimary,
                            activeTrackColor = TerracottaPrimary
                        )
                    )
                }
            }
        }

        // Tono de piel & Color de Cabello
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Tono de piel:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = EspressoTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        skinTones.forEach { toneHex ->
                            val isSelected = selectedSkinTone.equals(toneHex, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(parseColorSafe(toneHex))
                                    .border(
                                        if (isSelected) 3.dp else 1.dp,
                                        if (isSelected) TerracottaPrimary else Color(0x33000000),
                                        CircleShape
                                    )
                                    .clickable { selectedSkinTone = toneHex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Color de cabello:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = EspressoTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        hairColors.forEach { hHex ->
                            val isSelected = selectedHairColor.equals(hHex, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(parseColorSafe(hHex))
                                    .border(
                                        if (isSelected) 3.dp else 1.dp,
                                        if (isSelected) TerracottaPrimary else Color(0x33000000),
                                        CircleShape
                                    )
                                    .clickable { selectedHairColor = hHex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Estilo de cabello:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = EspressoTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(hairStyles) { style ->
                            FilterChip(
                                selected = hairStyle == style,
                                onClick = { hairStyle = style },
                                label = { Text(style, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SageSecondary,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFFF8F4F0),
                                    labelColor = EspressoTextSecondary
                                )
                            )
                        }
                    }
                }
            }
        }

        // Tallas y Comodidad
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Nivel de comodidad deseado:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = EspressoTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(comfortOptions) { comf ->
                            FilterChip(
                                selected = comfortPreference == comf,
                                onClick = { comfortPreference = comf },
                                label = { Text(comf, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TerracottaPrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFFF8F4F0),
                                    labelColor = EspressoTextSecondary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Talla Top", fontSize = 11.sp, color = EspressoTextMuted)
                            Text(topSize, fontWeight = FontWeight.Bold, color = EspressoTextPrimary)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Talla Inferior", fontSize = 11.sp, color = EspressoTextMuted)
                            Text(bottomSize, fontWeight = FontWeight.Bold, color = EspressoTextPrimary)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Calzado", fontSize = 11.sp, color = EspressoTextMuted)
                            Text(footwearSize, fontWeight = FontWeight.Bold, color = EspressoTextPrimary)
                        }
                    }
                }
            }
        }

        // Save button
        item {
            Button(
                onClick = { onSaveProfile(tempProfile) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_avatar_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar cambios en Avatar", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Sub-Tab 2: Opción A - Escaneo Corporal con Consentimiento y Privacidad
@Composable
fun EscanearCuerpoContent(
    profile: AvatarProfile,
    onSaveProfile: (AvatarProfile) -> Unit,
    onDeleteScan: () -> Unit
) {
    var consentGranted by remember { mutableStateOf(profile.hasScanConsent) }
    var scanSimulated by remember { mutableStateOf(profile.creationMethod == "scan") }
    var isProcessing by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp, 16.dp, 16.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Opción A: Escanear cuerpo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = EspressoTextPrimary
            )
            Text(
                text = "Crea un avatar 3D aproximado con fotos de frente, perfil y espalda con ropa ajustada y fondo neutro.",
                style = MaterialTheme.typography.bodySmall,
                color = EspressoTextSecondary
            )
        }

        // Privacy & Consent Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F7F4)),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = TerracottaPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Privacidad y Protección de Datos",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = EspressoTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "• Tus fotografías nunca son visibles públicamente ni compartidas con terceros.\n• Se utilizan exclusivamente en tu dispositivo para deducir proporciones del avatar.\n• Cifrado local de medidas.\n• Prohibido el uso para publicidad.\n• Puedes eliminar tus datos definitivamente en cualquier momento.",
                        style = MaterialTheme.typography.bodySmall,
                        color = EspressoTextSecondary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Autorizo el análisis de proporciones",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = EspressoTextPrimary
                        )
                        Switch(
                            checked = consentGranted,
                            onCheckedChange = {
                                consentGranted = it
                                onSaveProfile(profile.copy(hasScanConsent = it))
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = TerracottaPrimary)
                        )
                    }
                }
            }
        }

        // Three Photo Steps
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Fotografías requeridas:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = EspressoTextPrimary
                )

                PhotoStepCard(
                    title = "Foto 1: Frente",
                    desc = "Brazos ligeramente separados, ropa ajustada, fondo neutro",
                    isCaptured = scanSimulated
                )
                PhotoStepCard(
                    title = "Foto 2: Perfil lateral",
                    desc = "Postura natural para calcular curvatura de silueta",
                    isCaptured = scanSimulated
                )
                PhotoStepCard(
                    title = "Foto 3: Espalda",
                    desc = "Para calibrar ancho de hombros y caída de prendas",
                    isCaptured = scanSimulated
                )
            }
        }

        // Action Buttons
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        scanSimulated = true
                        onSaveProfile(profile.copy(creationMethod = "scan", hasScanConsent = true))
                    },
                    enabled = consentGranted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("start_scan_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (scanSimulated) "Actualizar escaneo corporal" else "Iniciar escaneo guiado",
                        fontWeight = FontWeight.Bold
                    )
                }

                if (scanSimulated) {
                    OutlinedButton(
                        onClick = onDeleteScan,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("delete_scan_btn"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFC62828))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Eliminar definitivamente escaneo corporal")
                    }
                }
            }
        }
    }
}

@Composable
fun PhotoStepCard(
    title: String,
    desc: String,
    isCaptured: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isCaptured) SageContainer else Color(0xFFF3ECE6)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isCaptured) Icons.Default.Check else Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = if (isCaptured) SageSecondary else EspressoTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = EspressoTextPrimary
                    )
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = EspressoTextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isCaptured) Color(0xFFE8F5E9) else Color(0xFFF3ECE6)
            ) {
                Text(
                    text = if (isCaptured) "Listo" else "Pendiente",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isCaptured) Color(0xFF2E7D32) else EspressoTextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}
