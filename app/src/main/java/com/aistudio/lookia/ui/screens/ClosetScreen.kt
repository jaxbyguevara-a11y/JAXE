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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.lookia.data.model.Garment
import com.aistudio.lookia.ui.LookIAUiState
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

@Composable
fun ClosetScreen(
    uiState: LookIAUiState,
    onAddGarmentClick: () -> Unit,
    onIncrementWear: (Long) -> Unit,
    onDeleteGarment: (Long) -> Unit,
    onTryOnGarment: (Garment) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todos") }

    val categories = listOf("Todos", "Tops", "Pantalones y Faldas", "Abrigos y Chaquetas", "Calzado", "Accesorios y Bolsos", "Vestidos")

    val filteredList = uiState.garments.filter { garment ->
        val matchesCategory = selectedCategory == "Todos" || garment.category == selectedCategory
        val matchesSearch = garment.name.contains(searchQuery, ignoreCase = true) ||
                garment.color.contains(searchQuery, ignoreCase = true) ||
                garment.texture.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    val activeCount = uiState.garments.count { it.wearCount >= 3 }
    val forgottenCount = uiState.garments.count { it.isForgotten || it.wearCount <= 2 }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(LinenBackground)
                .testTag("closet_screen_column"),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 110.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with metrics
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Mi Clóset Digital",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = EspressoTextPrimary
                            )
                            Text(
                                text = "${uiState.garments.size} prendas inventariadas",
                                style = MaterialTheme.typography.bodySmall,
                                color = EspressoTextSecondary
                            )
                        }

                        Button(
                            onClick = onAddGarmentClick,
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("add_garment_header_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Fotografiar Prenda", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Capsule summary stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Aprovechadas", fontSize = 11.sp, color = EspressoTextMuted)
                                Text("$activeCount prendas", fontWeight = FontWeight.Bold, color = SuccessGreen)
                            }
                        }
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Olvidadas", fontSize = 11.sp, color = EspressoTextMuted)
                                Text("$forgottenCount prendas", fontWeight = FontWeight.Bold, color = WarmHoney)
                            }
                        }
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Costo/uso prom.", fontSize = 11.sp, color = EspressoTextMuted)
                                Text(formatCop(uiState.savingsMetrics.averageCostPerWear), fontWeight = FontWeight.Bold, color = EspressoTextPrimary)
                            }
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar por nombre, tela o color...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = EspressoTextMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("closet_search_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TerracottaPrimary,
                        unfocusedBorderColor = CardBorder,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true
                )
            }

            // Category Filter Row
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(items = categories, key = { it }) { cat ->
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

            // Garments List
            if (filteredList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No se encontraron prendas en esta categoría.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = EspressoTextMuted
                            )
                        }
                    }
                }
            } else {
                items(items = filteredList, key = { it.id }) { garment ->
                    GarmentItemCard(
                        garment = garment,
                        onIncrementWear = { onIncrementWear(garment.id) },
                        onDelete = { onDeleteGarment(garment.id) },
                        onTryOn = { onTryOnGarment(garment) }
                    )
                }
            }
        }

        // Floating Action Button to quickly add clothes
        FloatingActionButton(
            onClick = onAddGarmentClick,
            containerColor = TerracottaPrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 96.dp, end = 20.dp)
                .testTag("closet_fab_add")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Añadir Prenda")
        }
    }
}

@Composable
fun GarmentItemCard(
    garment: Garment,
    onIncrementWear: () -> Unit,
    onDelete: () -> Unit,
    onTryOn: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("garment_card_${garment.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(parseColorSafe(garment.colorHex, Color.Gray))
                            .border(1.dp, Color(0x33000000), RoundedCornerShape(10.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = garment.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = EspressoTextPrimary
                        )
                        Text(
                            text = "${garment.category} • ${garment.texture}",
                            style = MaterialTheme.typography.bodySmall,
                            color = EspressoTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = EspressoTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Cost per wear & status badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF7EFE8)
                ) {
                    Text(
                        text = "Costo/uso: ${formatCopWithCode(garment.costPerWear)}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = TerracottaDark,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (garment.wearCount >= 3) SuccessGreenBg else WarmHoneyContainer
                ) {
                    Text(
                        text = "${garment.wearCount} posturas",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (garment.wearCount >= 3) SuccessGreen else WarmHoney,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFEDE8E3)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = EspressoTextSecondary,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = garment.privacy,
                            style = MaterialTheme.typography.labelSmall,
                            color = EspressoTextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            if (garment.repairNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = WarmHoneyContainer.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCut,
                            contentDescription = null,
                            tint = WarmHoney,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Segunda vida: ${garment.repairNotes}",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = EspressoTextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action row (+1 postura, probar en avatar)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onIncrementWear,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = SageSecondary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Loop, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+1 Postura", fontSize = 12.sp)
                }

                Button(
                    onClick = onTryOn,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaContainer),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Probar en Avatar", color = TerracottaDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
