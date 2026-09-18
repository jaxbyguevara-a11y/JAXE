package com.aistudio.lookia.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.aistudio.lookia.ui.theme.CardBorder
import com.aistudio.lookia.ui.theme.EspressoTextPrimary
import com.aistudio.lookia.ui.theme.EspressoTextSecondary
import com.aistudio.lookia.ui.theme.HoneyPrimary
import com.aistudio.lookia.ui.theme.SageContainer
import com.aistudio.lookia.ui.theme.SageSecondary
import com.aistudio.lookia.ui.theme.TerracottaContainer
import com.aistudio.lookia.ui.theme.TerracottaPrimary

@Composable
fun AddSecondChanceDialog(
    userGarments: List<Garment>,
    onDismiss: () -> Unit,
    onPublish: (
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
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedActionType by remember { mutableStateOf("Regalo con amor") }
    var selectedCategory by remember { mutableStateOf("Tops") }
    var size by remember { mutableStateOf("M") }
    var condition by remember { mutableStateOf("Excelente estado") }
    var colorName by remember { mutableStateOf("Verde Salvia") }
    var selectedColorHex by remember { mutableStateOf("#6B8E7B") }
    var exchangeOrPrice by remember { mutableStateOf("Gratis (Regalo)") }
    var story by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("Bogotá, Colombia") }

    val actionTypes = listOf(
        Triple("Regalo con amor", Icons.Default.CardGiftcard, Color(0xFF2E7D32)),
        Triple("Cambio / Trueque", Icons.Default.Loop, HoneyPrimary),
        Triple("Venta consciente", Icons.Default.Sell, TerracottaPrimary)
    )

    val categories = listOf("Tops", "Pantalones y Faldas", "Abrigos y Chaquetas", "Calzado", "Accesorios y Bolsos", "Vestidos")
    val conditions = listOf("Nueva con etiqueta", "Como nueva", "Excelente estado", "Upcycling con amor")
    val colors = listOf(
        Pair("Verde Salvia", "#6B8E7B"),
        Pair("Terracota", "#A84A33"),
        Pair("Blanco Lino", "#F4F1EA"),
        Pair("Miel Cálida", "#C28E38"),
        Pair("Azul Denim", "#2C3E50"),
        Pair("Café Moca", "#5E4233"),
        Pair("Negro Noche", "#212121")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("add_second_chance_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Loop,
                    contentDescription = null,
                    tint = TerracottaPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Dar Segunda Oportunidad",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EspressoTextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = SageContainer.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = SageSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Moda circular con propósito: dale vida a las prendas que ya no usas y bendice a otra mujer.",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = EspressoTextPrimary
                        )
                    }
                }

                Text(
                    text = "¿Qué deseas hacer con la prenda?",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = EspressoTextPrimary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    actionTypes.forEach { (type, icon, color) ->
                        val isSelected = selectedActionType == type
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    selectedActionType = type
                                    if (type == "Regalo con amor") {
                                        exchangeOrPrice = "Gratis (Regalo)"
                                    } else if (type == "Cambio / Trueque" && exchangeOrPrice == "Gratis (Regalo)") {
                                        exchangeOrPrice = "Cambio por otra prenda talla M"
                                    } else if (type == "Venta consciente" && exchangeOrPrice == "Gratis (Regalo)") {
                                        exchangeOrPrice = "$30.000 COP"
                                    }
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) color.copy(alpha = 0.15f) else Color(0xFFF9F6F3),
                            border = androidx.compose.foundation.BorderStroke(
                                if (isSelected) 1.5.dp else 1.dp,
                                if (isSelected) color else CardBorder
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) color else EspressoTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = type,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) color else EspressoTextSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // If user has garments in closet, allow selecting one as shortcut
                if (userGarments.isNotEmpty()) {
                    Text(
                        text = "O elige directo de tu clóset:",
                        style = MaterialTheme.typography.labelSmall,
                        color = EspressoTextSecondary
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(items = userGarments.take(6), key = { it.id }) { garment ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White,
                                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        title = garment.name
                                        selectedCategory = garment.category
                                        colorName = garment.color
                                        selectedColorHex = garment.colorHex
                                    }
                            ) {
                                Text(
                                    text = garment.name,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = EspressoTextPrimary
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nombre o descripción de la prenda") },
                    placeholder = { Text("Ej: Blusa seda lino con botones de coco") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Text(
                    text = "Categoría:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = EspressoTextPrimary
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(items = categories, key = { it }) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TerracottaPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = size,
                        onValueChange = { size = it },
                        label = { Text("Talla") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Ciudad / Zona") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Text(
                    text = "Estado de la prenda:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = EspressoTextPrimary
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(items = conditions, key = { it }) { cond ->
                        FilterChip(
                            selected = condition == cond,
                            onClick = { condition = cond },
                            label = { Text(cond, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HoneyPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = exchangeOrPrice,
                    onValueChange = { exchangeOrPrice = it },
                    label = {
                        Text(
                            when (selectedActionType) {
                                "Regalo con amor" -> "Condición de regalo"
                                "Cambio / Trueque" -> "¿Qué te gustaría recibir a cambio?"
                                else -> "Precio consciente (COP)"
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = story,
                    onValueChange = { story = it },
                    label = { Text("Historia o mensaje para la próxima dueña") },
                    placeholder = { Text("Ej: La usé en momentos muy felices y quiero que traiga alegría a quien la reciba...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalTitle = title.ifBlank { "$selectedCategory $selectedActionType" }
                    onPublish(
                        finalTitle,
                        selectedCategory,
                        selectedActionType,
                        size,
                        condition,
                        colorName,
                        selectedColorHex,
                        exchangeOrPrice,
                        story,
                        location
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Publicar con propósito")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
