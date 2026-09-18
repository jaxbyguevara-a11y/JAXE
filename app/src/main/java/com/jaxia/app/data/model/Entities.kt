package com.jaxia.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "garments")
data class Garment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String, // "Tops", "Pantalones y Faldas", "Abrigos y Chaquetas", "Calzado", "Accesorios y Bolsos", "Vestidos"
    val color: String,
    val colorHex: String = "#8D6E63",
    val texture: String,
    val style: String,
    val originalPrice: Double = 120000.0,
    val wearCount: Int = 1,
    val lastWornTimestamp: Long = System.currentTimeMillis(),
    val isForgotten: Boolean = false,
    val privacy: String = "Privada", // "Privada", "Amigas", "Pública"
    val repairNotes: String = "",
    val imageUri: String = ""
) {
    val costPerWear: Double
        get() = if (wearCount > 0) originalPrice / wearCount else originalPrice
}

@Entity(tableName = "avatar_profile")
data class AvatarProfile(
    @PrimaryKey
    val id: Int = 1,
    val userName: String = "Tu estilo",
    // Kept for schema continuity. The simulated "scan" path was removed
    // (AUDITORIA.md B-05), so this is always "manual".
    val creationMethod: String = "manual",
    val bodyType: String = "Reloj de arena", // "Reloj de arena", "Rectangular", "Triángulo", "Triángulo invertido", "Ovalado"
    val heightCm: Int = 165,
    val skinToneHex: String = "#D59E7C",
    val hairColorHex: String = "#3C2419",
    val hairStyle: String = "Ondulado largo", // "Ondulado largo", "Lacio medio", "Rizado corto", "Corte Bob", "Recogido elegante"
    val topSize: String = "M",
    val bottomSize: String = "38 / M",
    val footwearSize: String = "37",
    val stylePreference: String = "Elegante y consciente",
    val comfortPreference: String = "Equilibrado", // "Muy holgado", "Equilibrado", "Ajustado"
    val preferredFootwear: String = "Tenis y flats",
    val isPrivateAccount: Boolean = true
)

@Entity(tableName = "outfits")
data class Outfit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val occasion: String,
    val mood: String,
    val proposalType: String, // "Recomendado", "Cómodo", "Creativo"
    val garmentIds: String, // Comma separated garment IDs: "1,3,5"
    val hairstyleTip: String = "",
    val makeupTip: String = "",
    val estimatedSavings: Double = 180000.0,
    val isFavorite: Boolean = false,
    val wornCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "outfit_feedbacks")
data class OutfitFeedback(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val outfitId: Long,
    val feltComfortable: Boolean = true,
    val feltAuthentic: Boolean = true,
    val feltConfident: Boolean = true,
    val wouldRewear: Boolean = true,
    val fitOccasionWell: Boolean = true,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "community_posts")
data class CommunityPost(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val authorName: String,
    val authorAvatarBody: String = "Reloj de arena",
    val question: String,
    val lookDescription: String,
    val optionATitle: String = "Opción 1: Con blazer",
    val optionBTitle: String = "Opción 2: Con chaqueta denim",
    val votesA: Int = 18,
    val votesB: Int = 24,
    val userVoted: Int = 0, // 0 = none, 1 = A, 2 = B
    val likesCount: Int = 32,
    val isLiked: Boolean = false,
    val commentsCount: Int = 9,
    val challengeTag: String = "Una prenda, 5 looks",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "second_chance_items")
data class SecondChanceItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ownerName: String,
    val ownerAvatarBody: String = "Reloj de arena",
    val title: String,
    val category: String, // "Tops", "Pantalones y Faldas", "Abrigos y Chaquetas", "Calzado", "Accesorios y Bolsos", "Vestidos"
    val actionType: String, // "Cambio / Trueque", "Regalo con amor", "Venta consciente"
    val size: String = "M",
    val condition: String = "Excelente estado", // "Nueva con etiqueta", "Como nueva", "Excelente estado", "Upcycling con amor"
    val color: String = "Terracota",
    val colorHex: String = "#A84A33",
    val exchangeOrPrice: String = "Gratis (Regalo)", // "Gratis (Regalo)", "$35.000 COP", "Cambio por chaleco o falda"
    val story: String = "", // Historia consciente o por qué busca nueva dueña
    val location: String = "Bogotá, Colombia",
    val interestedCount: Int = 0,
    val isUserInterested: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
