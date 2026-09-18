package com.jaxia.app.ui.components

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jaxia.app.ui.theme.CardBorder
import com.jaxia.app.ui.theme.EspressoTextMuted
import com.jaxia.app.ui.theme.EspressoTextPrimary
import com.jaxia.app.ui.theme.EspressoTextSecondary
import com.jaxia.app.ui.theme.SageContainer
import com.jaxia.app.ui.theme.SageSecondary
import com.jaxia.app.ui.theme.TerracottaContainer
import com.jaxia.app.ui.theme.TerracottaPrimary

@Composable
fun AddGarmentDialog(
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        category: String,
        color: String,
        colorHex: String,
        texture: String,
        style: String,
        price: Double,
        repairNotes: String,
        privacy: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Tops") }
    var colorName by remember { mutableStateOf("Terracota") }
    var selectedColorHex by remember { mutableStateOf("#B85D43") }
    var texture by remember { mutableStateOf("Lino puro") }
    var style by remember { mutableStateOf("Elegante relajado") }
    var priceStr by remember { mutableStateOf("120000") }
    var repairNotes by remember { mutableStateOf("") }
    var privacy by remember { mutableStateOf("Privada") }

    val categories = listOf("Tops", "Pantalones y Faldas", "Abrigos y Chaquetas", "Calzado", "Accesorios y Bolsos", "Vestidos")
    val colors = listOf(
        Pair("Terracota", "#B85D43"),
        Pair("Verde Salvia", "#6B8E7B"),
        Pair("Blanco Lino", "#F4F1EA"),
        Pair("Azul Denim", "#3F51B5"),
        Pair("Negro Noche", "#212121"),
        Pair("Miel Cálida", "#C28E38"),
        Pair("Rosa Palo", "#D4A5A5")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("add_garment_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = TerracottaPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Añadir Prenda a mi Clóset",
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
                // Background removal simulator badge
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
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = SageSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Fondo eliminado automáticamente • Clasificación inteligente",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = EspressoTextPrimary
                        )
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre de la prenda") },
                    placeholder = { Text("Ej: Blusa fluida con cuello en V") },
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

                Text(
                    text = "Color predominante:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = EspressoTextPrimary
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(items = colors, key = { it.first }) { (cName, hex) ->
                        val isSelected = selectedColorHex.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(parseColorSafe(hex))
                                .border(
                                    if (isSelected) 3.dp else 1.dp,
                                    if (isSelected) TerracottaPrimary else Color(0x33000000),
                                    CircleShape
                                )
                                .clickable {
                                    selectedColorHex = hex
                                    colorName = cName
                                }
                                // The swatch is a colour circle with no text, so
                                // TalkBack had nothing to announce. The check
                                // glyph inside stays decorative; the selected
                                // state is exposed here instead.
                                .semantics {
                                    contentDescription = cName
                                    role = Role.RadioButton
                                    selected = isSelected
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = if (hex == "#F4F1EA") Color.Black else Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = texture,
                        onValueChange = { texture = it },
                        label = { Text("Textura/Tela") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = style,
                        onValueChange = { style = it },
                        label = { Text("Estilo") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text("Precio aproximado (COP)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = repairNotes,
                    onValueChange = { repairNotes = it },
                    label = { Text("Ideas de segunda vida / arreglos (opcional)") },
                    placeholder = { Text("Ej: Cambiar botones, ajustar largo...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalName = name.ifBlank { "$selectedCategory $colorName" }
                    val price = priceStr.toDoubleOrNull() ?: 100000.0
                    onSave(finalName, selectedCategory, colorName, selectedColorHex, texture, style, price, repairNotes, privacy)
                },
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Guardar en mi clóset")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
