package com.aistudio.lookia.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.lookia.data.model.AvatarProfile
import com.aistudio.lookia.data.model.Garment
import com.aistudio.lookia.ui.theme.LinenBackground
import com.aistudio.lookia.ui.theme.SageSecondary
import com.aistudio.lookia.ui.theme.TerracottaPrimary

fun parseColorSafe(hex: String, fallback: Color = Color(0xFFD59E7C)): Color {
    return try {
        val cleanHex = hex.removePrefix("#")
        if (cleanHex.length == 6) {
            Color(android.graphics.Color.parseColor("#$cleanHex"))
        } else if (cleanHex.length == 8) {
            Color(android.graphics.Color.parseColor("#$cleanHex"))
        } else {
            fallback
        }
    } catch (e: Exception) {
        fallback
    }
}

/**
 * High-fidelity, body-positive avatar visualizer rendered dynamically on Compose Canvas.
 * Adapts to height, silhouette body type (Reloj de arena, Rectangular, Triángulo, Triángulo invertido, Ovalado),
 * skin tone, hair style & hair color, and layers wearing garments (outerwear, top, bottom, shoes, accessories).
 */
@Composable
fun AvatarVisualizer(
    profile: AvatarProfile,
    wearingGarments: List<Garment> = emptyList(),
    modifier: Modifier = Modifier,
    height: Dp = 320.dp,
    showPrivacyBadge: Boolean = true
) {
    val skinColor = parseColorSafe(profile.skinToneHex, Color(0xFFD59E7C))
    val hairColor = parseColorSafe(profile.hairColorHex, Color(0xFF332018))

    val topGarment = wearingGarments.find { it.category == "Tops" || it.category == "Vestidos" }
    val bottomGarment = wearingGarments.find { it.category == "Pantalones y Faldas" }
    val outerGarment = wearingGarments.find { it.category == "Abrigos y Chaquetas" }
    val shoeGarment = wearingGarments.find { it.category == "Calzado" }
    val accGarment = wearingGarments.find { it.category == "Accesorios y Bolsos" }

    Box(
        modifier = modifier
            .height(height)
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFDFBF9),
                        Color(0xFFF3ECE6)
                    )
                )
            )
            .border(1.dp, Color(0xFFE5DDD5), RoundedCornerShape(24.dp))
            .testTag("avatar_visualizer_box"),
        contentAlignment = Alignment.Center
    ) {
        // Floor subtle shadow
        Canvas(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(24.dp)
                .align(Alignment.BottomCenter)
                .padding(bottom = 14.dp)
        ) {
            drawOval(
                color = Color(0x22000000),
                topLeft = Offset(0f, 0f),
                size = Size(size.width, size.height)
            )
        }

        // Avatar body and layered garments
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp, horizontal = 20.dp)
                .testTag("avatar_canvas")
        ) {
            val canvasW = size.width
            val canvasH = size.height
            val centerX = canvasW / 2f

            // Height scaling factor based on profile.heightCm (150cm to 185cm range)
            val heightNorm = ((profile.heightCm.coerceIn(150, 185) - 150f) / 35f) // 0.0 to 1.0
            val scaleFactor = 0.88f + (heightNorm * 0.14f)

            // Proportional points
            val headRadius = 24f * scaleFactor
            val headCenterY = 38f * scaleFactor
            val neckTop = headCenterY + headRadius
            val neckBottom = neckTop + 14f * scaleFactor
            val shoulderY = neckBottom + 6f
            
            // Shoulder width & hip width based on body type
            val (shoulderWidth, waistWidth, hipWidth) = when (profile.bodyType) {
                "Triángulo" -> Triple(44f * scaleFactor, 38f * scaleFactor, 58f * scaleFactor)
                "Triángulo invertido" -> Triple(58f * scaleFactor, 40f * scaleFactor, 42f * scaleFactor)
                "Rectangular" -> Triple(48f * scaleFactor, 44f * scaleFactor, 48f * scaleFactor)
                "Ovalado" -> Triple(48f * scaleFactor, 54f * scaleFactor, 52f * scaleFactor)
                else -> Triple(50f * scaleFactor, 36f * scaleFactor, 54f * scaleFactor) // Reloj de arena
            }

            val waistY = shoulderY + 54f * scaleFactor
            val hipY = waistY + 44f * scaleFactor
            val crotchY = hipY + 18f * scaleFactor
            val kneeY = crotchY + 68f * scaleFactor
            val ankleY = kneeY + 68f * scaleFactor
            val feetY = ankleY + 16f * scaleFactor

            // --- 1. LEGS (Skin base) ---
            val legSpacing = 16f * scaleFactor
            val legWidth = 14f * scaleFactor

            // Left leg skin
            drawRoundRect(
                color = skinColor,
                topLeft = Offset(centerX - legSpacing - legWidth / 2f, hipY),
                size = Size(legWidth, ankleY - hipY),
                cornerRadius = CornerRadius(6f, 6f)
            )
            // Right leg skin
            drawRoundRect(
                color = skinColor,
                topLeft = Offset(centerX + legSpacing - legWidth / 2f, hipY),
                size = Size(legWidth, ankleY - hipY),
                cornerRadius = CornerRadius(6f, 6f)
            )

            // --- 2. BOTTOM GARMENT (Pantalón, Falda, o default) ---
            if (bottomGarment != null) {
                val botColor = parseColorSafe(bottomGarment.colorHex, Color(0xFF4A6572))
                val isSkirt = bottomGarment.name.contains("Falda", ignoreCase = true) || bottomGarment.name.contains("Vestido", ignoreCase = true)
                if (isSkirt) {
                    val skirtPath = Path().apply {
                        moveTo(centerX - waistWidth * 0.85f, waistY)
                        lineTo(centerX + waistWidth * 0.85f, waistY)
                        lineTo(centerX + hipWidth * 1.25f, kneeY + 12f)
                        lineTo(centerX - hipWidth * 1.25f, kneeY + 12f)
                        close()
                    }
                    drawPath(skirtPath, botColor)
                } else {
                    // Pants
                    val pantW = legWidth * 1.35f
                    // Left leg pant
                    drawRoundRect(
                        color = botColor,
                        topLeft = Offset(centerX - legSpacing - pantW / 2f, waistY + 10f),
                        size = Size(pantW, ankleY - waistY - 6f),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                    // Right leg pant
                    drawRoundRect(
                        color = botColor,
                        topLeft = Offset(centerX + legSpacing - pantW / 2f, waistY + 10f),
                        size = Size(pantW, ankleY - waistY - 6f),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                    // Hip connection
                    drawRoundRect(
                        color = botColor,
                        topLeft = Offset(centerX - hipWidth, waistY),
                        size = Size(hipWidth * 2f, crotchY - waistY + 6f),
                        cornerRadius = CornerRadius(12f, 12f)
                    )
                }
            } else {
                // Minimal baseline neutral underwear/legging
                drawRoundRect(
                    color = Color(0xFFE3DAD1),
                    topLeft = Offset(centerX - hipWidth * 0.9f, waistY + 14f),
                    size = Size(hipWidth * 1.8f, crotchY - waistY + 2f),
                    cornerRadius = CornerRadius(8f, 8f)
                )
            }

            // --- 3. SHOES / FEET ---
            val shoeColor = if (shoeGarment != null) {
                parseColorSafe(shoeGarment.colorHex, Color(0xFF222222))
            } else {
                skinColor
            }
            // Left shoe
            drawRoundRect(
                color = shoeColor,
                topLeft = Offset(centerX - legSpacing - 11f * scaleFactor, ankleY - 4f),
                size = Size(20f * scaleFactor, 16f * scaleFactor),
                cornerRadius = CornerRadius(6f, 6f)
            )
            // Right shoe
            drawRoundRect(
                color = shoeColor,
                topLeft = Offset(centerX + legSpacing - 9f * scaleFactor, ankleY - 4f),
                size = Size(20f * scaleFactor, 16f * scaleFactor),
                cornerRadius = CornerRadius(6f, 6f)
            )

            // --- 4. TORSO (Skin base) ---
            val torsoPath = Path().apply {
                moveTo(centerX - shoulderWidth, shoulderY)
                lineTo(centerX + shoulderWidth, shoulderY)
                lineTo(centerX + waistWidth, waistY)
                lineTo(centerX + hipWidth, hipY)
                lineTo(centerX - hipWidth, hipY)
                lineTo(centerX - waistWidth, waistY)
                close()
            }
            drawPath(torsoPath, skinColor)

            // Neck
            drawRect(
                color = skinColor,
                topLeft = Offset(centerX - 10f * scaleFactor, neckTop),
                size = Size(20f * scaleFactor, neckBottom - neckTop + 6f)
            )

            // --- 5. ARMS ---
            val armW = 11f * scaleFactor
            val armL = (hipY - shoulderY) * 1.15f
            // Left arm
            drawRoundRect(
                color = skinColor,
                topLeft = Offset(centerX - shoulderWidth - armW + 2f, shoulderY + 4f),
                size = Size(armW, armL),
                cornerRadius = CornerRadius(6f, 6f)
            )
            // Right arm
            drawRoundRect(
                color = skinColor,
                topLeft = Offset(centerX + shoulderWidth - 2f, shoulderY + 4f),
                size = Size(armW, armL),
                cornerRadius = CornerRadius(6f, 6f)
            )

            // --- 6. TOP GARMENT ---
            if (topGarment != null) {
                val topColor = parseColorSafe(topGarment.colorHex, Color(0xFFF4F1EA))
                val isDress = topGarment.category == "Vestidos"

                val topPath = Path().apply {
                    moveTo(centerX - shoulderWidth - 2f, shoulderY - 2f)
                    lineTo(centerX + shoulderWidth + 2f, shoulderY - 2f)
                    if (isDress) {
                        lineTo(centerX + hipWidth * 1.2f, kneeY)
                        lineTo(centerX - hipWidth * 1.2f, kneeY)
                    } else {
                        lineTo(centerX + waistWidth * 1.05f, waistY + 12f)
                        lineTo(centerX - waistWidth * 1.05f, waistY + 12f)
                    }
                    close()
                }
                drawPath(topPath, topColor)

                // Neckline detail
                drawOval(
                    color = skinColor,
                    topLeft = Offset(centerX - 12f * scaleFactor, shoulderY - 4f),
                    size = Size(24f * scaleFactor, 18f * scaleFactor)
                )

                // Sleeves
                drawRoundRect(
                    color = topColor,
                    topLeft = Offset(centerX - shoulderWidth - armW - 1f, shoulderY + 2f),
                    size = Size(armW + 3f, armL * 0.45f),
                    cornerRadius = CornerRadius(4f, 4f)
                )
                drawRoundRect(
                    color = topColor,
                    topLeft = Offset(centerX + shoulderWidth - 2f, shoulderY + 2f),
                    size = Size(armW + 3f, armL * 0.45f),
                    cornerRadius = CornerRadius(4f, 4f)
                )
            }

            // --- 7. OUTERWEAR (Blazer / Jacket) ---
            if (outerGarment != null) {
                val outerColor = parseColorSafe(outerGarment.colorHex, Color(0xFFA84A33))
                // Left lapel & panel
                val leftPanel = Path().apply {
                    moveTo(centerX - shoulderWidth - 4f, shoulderY - 2f)
                    lineTo(centerX - 6f, shoulderY + 8f)
                    lineTo(centerX - 10f, waistY + 18f)
                    lineTo(centerX - waistWidth - 8f, waistY + 18f)
                    close()
                }
                drawPath(leftPanel, outerColor)

                // Right lapel & panel
                val rightPanel = Path().apply {
                    moveTo(centerX + shoulderWidth + 4f, shoulderY - 2f)
                    lineTo(centerX + 6f, shoulderY + 8f)
                    lineTo(centerX + 10f, waistY + 18f)
                    lineTo(centerX + waistWidth + 8f, waistY + 18f)
                    close()
                }
                drawPath(rightPanel, outerColor)

                // Outer sleeves
                drawRoundRect(
                    color = outerColor,
                    topLeft = Offset(centerX - shoulderWidth - armW - 3f, shoulderY),
                    size = Size(armW + 5f, armL * 0.9f),
                    cornerRadius = CornerRadius(6f, 6f)
                )
                drawRoundRect(
                    color = outerColor,
                    topLeft = Offset(centerX + shoulderWidth - 2f, shoulderY),
                    size = Size(armW + 5f, armL * 0.9f),
                    cornerRadius = CornerRadius(6f, 6f)
                )
            }

            // --- 8. ACCESSORY (Bag) ---
            if (accGarment != null && accGarment.name.contains("Bolso", ignoreCase = true)) {
                val bagColor = parseColorSafe(accGarment.colorHex, Color(0xFFA36B3B))
                // Bag strap
                drawLine(
                    color = bagColor,
                    start = Offset(centerX - shoulderWidth + 4f, shoulderY),
                    end = Offset(centerX + waistWidth + 8f, waistY + 10f),
                    strokeWidth = 3f
                )
                // Bag body
                drawRoundRect(
                    color = bagColor,
                    topLeft = Offset(centerX + waistWidth + 2f, waistY + 10f),
                    size = Size(28f * scaleFactor, 32f * scaleFactor),
                    cornerRadius = CornerRadius(6f, 6f)
                )
            }

            // --- 9. HEAD & FACE ---
            drawCircle(
                color = skinColor,
                radius = headRadius,
                center = Offset(centerX, headCenterY)
            )

            // Subtle facial features (gentle stylized eyes & lips)
            drawOval(
                color = Color(0x40000000),
                topLeft = Offset(centerX - 10f * scaleFactor, headCenterY - 2f),
                size = Size(5f * scaleFactor, 3f * scaleFactor)
            )
            drawOval(
                color = Color(0x40000000),
                topLeft = Offset(centerX + 5f * scaleFactor, headCenterY - 2f),
                size = Size(5f * scaleFactor, 3f * scaleFactor)
            )
            // Smile
            val smilePath = Path().apply {
                moveTo(centerX - 5f * scaleFactor, headCenterY + 10f * scaleFactor)
                quadraticTo(
                    centerX, headCenterY + 14f * scaleFactor,
                    centerX + 5f * scaleFactor, headCenterY + 10f * scaleFactor
                )
            }
            drawPath(smilePath, Color(0x809E5B4F), style = Stroke(width = 2f))

            // --- 10. HAIR ---
            when (profile.hairStyle) {
                "Ondulado largo" -> {
                    // Back hair flow
                    val leftHair = Path().apply {
                        moveTo(centerX - headRadius - 2f, headCenterY)
                        quadraticTo(
                            centerX - headRadius - 12f, shoulderY + 16f,
                            centerX - headRadius + 4f, shoulderY + 44f
                        )
                        lineTo(centerX - headRadius + 14f, shoulderY + 42f)
                        quadraticTo(
                            centerX - headRadius, shoulderY + 16f,
                            centerX - headRadius + 4f, headCenterY - 6f
                        )
                        close()
                    }
                    drawPath(leftHair, hairColor)

                    val rightHair = Path().apply {
                        moveTo(centerX + headRadius + 2f, headCenterY)
                        quadraticTo(
                            centerX + headRadius + 12f, shoulderY + 16f,
                            centerX + headRadius - 4f, shoulderY + 44f
                        )
                        lineTo(centerX + headRadius - 14f, shoulderY + 42f)
                        quadraticTo(
                            centerX + headRadius, shoulderY + 16f,
                            centerX + headRadius - 4f, headCenterY - 6f
                        )
                        close()
                    }
                    drawPath(rightHair, hairColor)

                    // Hair crown
                    drawOval(
                        color = hairColor,
                        topLeft = Offset(centerX - headRadius - 2f, headCenterY - headRadius - 4f),
                        size = Size((headRadius + 2f) * 2f, headRadius * 1.4f)
                    )
                }
                "Rizado corto" -> {
                    drawOval(
                        color = hairColor,
                        topLeft = Offset(centerX - headRadius - 8f, headCenterY - headRadius - 8f),
                        size = Size((headRadius + 8f) * 2f, headRadius * 2.2f)
                    )
                }
                "Corte Bob" -> {
                    drawOval(
                        color = hairColor,
                        topLeft = Offset(centerX - headRadius - 4f, headCenterY - headRadius - 4f),
                        size = Size((headRadius + 4f) * 2f, headRadius * 1.8f)
                    )
                }
                "Recogido elegante" -> {
                    // Bun top
                    drawCircle(
                        color = hairColor,
                        radius = headRadius * 0.5f,
                        center = Offset(centerX, headCenterY - headRadius - 6f)
                    )
                    // Neat crown
                    drawOval(
                        color = hairColor,
                        topLeft = Offset(centerX - headRadius, headCenterY - headRadius - 2f),
                        size = Size(headRadius * 2f, headRadius * 1.2f)
                    )
                }
                else -> { // Lacio medio
                    drawOval(
                        color = hairColor,
                        topLeft = Offset(centerX - headRadius - 2f, headCenterY - headRadius - 2f),
                        size = Size((headRadius + 2f) * 2f, headRadius * 1.6f)
                    )
                    drawRect(
                        color = hairColor,
                        topLeft = Offset(centerX - headRadius - 2f, headCenterY),
                        size = Size(8f, 32f)
                    )
                    drawRect(
                        color = hairColor,
                        topLeft = Offset(centerX + headRadius - 6f, headCenterY),
                        size = Size(8f, 32f)
                    )
                }
            }
        }

        // Privacy indicator & body info pill
        if (showPrivacyBadge) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xCCFFFFFF),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5DDD5))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = "Privacidad protegida",
                        tint = SageSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Avatar 100% privado • ${profile.bodyType}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }
        }

        // Method tag (Manual / Escáner)
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            shape = RoundedCornerShape(12.dp),
            color = TerracottaPrimary.copy(alpha = 0.12f)
        ) {
            Text(
                // The avatar is always built from measurements the user typed
                // in. The old "Escaneo 3D" badge claimed a capability that did
                // not exist (AUDITORIA.md B-05).
                text = "Personalizado",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = TerracottaPrimary,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp
            )
        }
    }
}
