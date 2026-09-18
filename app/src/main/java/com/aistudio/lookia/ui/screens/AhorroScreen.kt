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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.aistudio.lookia.data.repository.SmartPurchaseAnalysis
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
fun AhorroScreen(
    uiState: LookIAUiState,
    onEvaluatePurchase: (name: String, price: Double, category: String, color: String, style: String) -> Unit,
    onClearPurchaseAnalysis: () -> Unit,
    onRescuedGarmentClick: (Garment) -> Unit,
    modifier: Modifier = Modifier
) {
    val metrics = uiState.savingsMetrics
    val forgottenGarments = uiState.garments.filter { it.isForgotten || it.wearCount <= 2 }

    var prospectiveName by remember { mutableStateOf("") }
    var prospectivePrice by remember { mutableStateOf("190000") }
    var prospectiveCategory by remember { mutableStateOf("Tops") }
    var prospectiveColor by remember { mutableStateOf("Verde") }
    var prospectiveStyle by remember { mutableStateOf("Formal") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(LinenBackground)
            .testTag("ahorro_screen_column"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
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
                    text = "Motor de Ahorro y Consciencia",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = EspressoTextPrimary
                )
                Text(
                    text = "“Vístete con lo que tienes” convertido en una herramienta financiera medible.",
                    style = MaterialTheme.typography.bodySmall,
                    color = EspressoTextSecondary
                )
            }
        }

        // Primary Big Savings Milestone Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SuccessGreenBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC8E6C9))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(SuccessGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Savings,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            // Labelled as re-wear value, not "money saved": the
                            // figure is the purchase value of repeat wears, and
                            // no money changed hands (AUDITORIA.md A-10).
                            Text(
                                text = "Valor reutilizado de tu clóset",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF2E7D32)
                            )
                            Text(
                                text = formatCopWithCode(metrics.reWearValueCOP),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1B5E20)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.8f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "✨ Cómo se calcula",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20)
                            )
                            // The old copy asserted a fixed "$180.000 saved" and
                            // a count of "impulse purchases avoided" that nothing
                            // measured. This states the actual formula instead.
                            Text(
                                text = "Sumamos el precio de cada prenda por cada uso " +
                                    "adicional al primero. Es el valor que ya tenías en el " +
                                    "clóset y volviste a aprovechar, no dinero recibido. " +
                                    "Llevas ${metrics.garmentsRescued} prenda(s) con 3 o más usos.",
                                style = MaterialTheme.typography.bodySmall,
                                color = EspressoTextPrimary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // Closet Utilization Gauge Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                        Text(
                            text = "Aprovechamiento del Clóset",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = EspressoTextPrimary
                        )
                        Text(
                            text = "${metrics.closetUtilizationRate}% utilizado",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TerracottaPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { metrics.closetUtilizationRate / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = TerracottaPrimary,
                        trackColor = Color(0xFFF0EBE5)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${uiState.garments.count { it.wearCount >= 3 }} de ${uiState.garments.size} prendas tienen más de 3 usos frecuentes. El objetivo consciente es superar el 85%.",
                        style = MaterialTheme.typography.bodySmall,
                        color = EspressoTextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Rescate de Prendas Olvidadas
        item {
            Column {
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
                            text = "Prendas por rescatar (${forgottenGarments.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = EspressoTextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (forgottenGarments.isEmpty()) {
                    Text(
                        text = "¡Felicitaciones! Todas las prendas de tu clóset están en rotación activa.",
                        style = MaterialTheme.typography.bodySmall,
                        color = EspressoTextSecondary
                    )
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(items = forgottenGarments, key = { it.id }) { garment ->
                            Surface(
                                modifier = Modifier
                                    .width(220.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { onRescuedGarmentClick(garment) },
                                color = Color.White,
                                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(parseColorSafe(garment.colorHex, Color.Gray))
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = WarmHoneyContainer
                                        ) {
                                            Text(
                                                text = "${garment.wearCount} usos",
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = WarmHoney,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = garment.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = EspressoTextPrimary,
                                        maxLines = 1
                                    )

                                    Text(
                                        text = "Costo/uso: ${formatCopWithCode(garment.costPerWear)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = EspressoTextMuted,
                                        fontSize = 11.sp
                                    )

                                    if (garment.repairNotes.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "✂️ ${garment.repairNotes}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TerracottaDark,
                                            fontSize = 10.sp,
                                            maxLines = 2
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Button(
                                        onClick = { onRescuedGarmentClick(garment) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Crear Look", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: "Compra Inteligente" Evaluator
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("compra_inteligente_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SageContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = SageSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Evaluador de Compra Inteligente",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = EspressoTextPrimary
                            )
                            Text(
                                text = "¿Tentada por una prenda en una tienda? Evalúala antes de gastar.",
                                style = MaterialTheme.typography.bodySmall,
                                color = EspressoTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = prospectiveName,
                        onValueChange = { prospectiveName = it },
                        label = { Text("Nombre de la prenda en vitrina") },
                        placeholder = { Text("Ej: Blusa seda verde oliva") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = prospectivePrice,
                            onValueChange = { prospectivePrice = it },
                            label = { Text("Precio (COP)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = prospectiveCategory,
                            onValueChange = { prospectiveCategory = it },
                            label = { Text("Categoría") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val price = prospectivePrice.toDoubleOrNull() ?: 180000.0
                            val name = prospectiveName.ifBlank { "Blusa de seda verde" }
                            onEvaluatePurchase(name, price, prospectiveCategory, prospectiveColor, prospectiveStyle)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("evaluate_purchase_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = SageSecondary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Analizar contra mi clóset", fontWeight = FontWeight.Bold)
                    }

                    // Display Analysis Results if available
                    val analysis = uiState.smartPurchaseAnalysis
                    if (analysis != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        PurchaseAnalysisResultBox(
                            analysis = analysis,
                            onDismiss = onClearPurchaseAnalysis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PurchaseAnalysisResultBox(
    analysis: SmartPurchaseAnalysis,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (badgeBg, badgeText, icon) = when (analysis.statusBadge) {
        "Compra útil" -> Triple(SuccessGreenBg, SuccessGreen, Icons.Default.CheckCircle)
        "Compra opcional" -> Triple(WarmHoneyContainer, WarmHoney, Icons.Default.Info)
        "Compra repetida" -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), Icons.Default.Warning)
        else -> Triple(TerracottaContainer, TerracottaDark, Icons.Default.ContentCut) // "Mejor reutiliza"
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("smart_purchase_result_box"),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFF9F7F5),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = icon, contentDescription = null, tint = badgeText, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = analysis.statusBadge.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = badgeText
                        )
                    }
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = EspressoTextMuted)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = analysis.badgeReason,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = EspressoTextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = analysis.adviceMessage,
                style = MaterialTheme.typography.bodySmall,
                color = EspressoTextSecondary,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Prendas similares", fontSize = 10.sp, color = EspressoTextMuted)
                        Text("${analysis.similarItemsOwned.size} en clóset", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EspressoTextPrimary)
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Looks posibles", fontSize = 10.sp, color = EspressoTextMuted)
                        Text("+${analysis.outfitsPossible} outfits", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SuccessGreen)
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Costo/uso est.", fontSize = 10.sp, color = EspressoTextMuted)
                        Text(formatCop(analysis.estimatedCostPerWear), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EspressoTextPrimary)
                    }
                }
            }
        }
    }
}
