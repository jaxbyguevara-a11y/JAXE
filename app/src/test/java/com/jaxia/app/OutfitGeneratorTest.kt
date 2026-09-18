package com.jaxia.app

import com.jaxia.app.data.model.Garment
import com.jaxia.app.data.repository.JaxiaRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Covers the rule-based outfit engine.
 *
 * Two regressions are pinned here: the generator used to ignore its `occasion`
 * and `mood` arguments entirely (AUDITORIA.md M-11), and the proposals it
 * produced all carried `id = 0`, so marking one as a favourite updated no row
 * (A-13).
 */
class OutfitGeneratorTest {

    private fun garment(
        id: Long,
        name: String,
        category: String,
        style: String = "Casual",
        texture: String = "Algodón",
        price: Double = 100_000.0,
        wearCount: Int = 1,
        isForgotten: Boolean = false,
    ) = Garment(
        id = id,
        name = name,
        category = category,
        color = "Terracota",
        texture = texture,
        style = style,
        originalPrice = price,
        wearCount = wearCount,
        isForgotten = isForgotten,
    )

    private val closet = listOf(
        garment(1, "Blazer Sastre", "Abrigos y Chaquetas", style = "Elegante Estructurado"),
        garment(2, "Chaqueta Denim", "Abrigos y Chaquetas", style = "Casual"),
        garment(3, "Blusa de Seda", "Tops", style = "Elegante"),
        garment(4, "Camiseta Algodón", "Tops", style = "Cómodo", texture = "Algodón"),
        garment(5, "Pantalón Sastre", "Pantalones y Faldas", style = "Elegante"),
        garment(6, "Jeans Rectos", "Pantalones y Faldas", style = "Casual"),
        garment(7, "Mocasines", "Calzado", style = "Elegante"),
        garment(8, "Sneakers Blancos", "Calzado", style = "Cómodo"),
        garment(9, "Bolso Cuero", "Accesorios y Bolsos"),
        garment(10, "Falda Plisada", "Pantalones y Faldas", wearCount = 0, isForgotten = true),
    )

    private fun repository() = JaxiaRepository(
        garmentDao = FakeDaos.garment(closet),
        avatarDao = FakeDaos.avatar(),
        outfitDao = FakeDaos.outfit(),
        communityDao = FakeDaos.community(),
        secondChanceDao = FakeDaos.secondChance(),
        externalScope = CoroutineScope(Job()),
    )

    @Test
    fun `empty closet yields no proposals instead of empty-garment outfits`() {
        val result = repository().generateOutfitsForOccasion("Trabajo", "Segura", emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `occasion changes the proposals`() {
        val repo = repository()

        val work = repo.generateOutfitsForOccasion("Reunión de trabajo", "Quiero sentirme segura", closet)
        val casual = repo.generateOutfitsForOccasion("Plan casual", "Quiero sentirme cómoda", closet)

        // The old implementation returned byte-identical lists for any input.
        assertNotEquals(
            work.first().garmentIds,
            casual.first().garmentIds,
        )
    }

    @Test
    fun `occasion appears in the proposal title`() {
        val result = repository()
            .generateOutfitsForOccasion("Reunión de trabajo", "Quiero sentirme segura", closet)

        assertTrue(
            "Title should name the occasion, was '${result.first().title}'",
            result.first().title.contains("Reunión de trabajo"),
        )
    }

    @Test
    fun `work occasion favours tailored pieces`() {
        val result = repository()
            .generateOutfitsForOccasion("Reunión de trabajo", "Quiero sentirme segura", closet)

        val recommendedIds = result.first().garmentIds.split(",").mapNotNull { it.toLongOrNull() }
        val chosen = closet.filter { it.id in recommendedIds }

        assertTrue(
            "Expected a tailored outer layer, got ${chosen.map { it.name }}",
            chosen.any { it.name.contains("Blazer") || it.name.contains("Sastre") },
        )
    }

    @Test
    fun `rescue proposal centres on the least worn garment`() {
        val result = repository()
            .generateOutfitsForOccasion("Plan casual", "Quiero sentirme creativa", closet)

        val rescue = result.first { it.proposalType == "Creativo" }
        val ids = rescue.garmentIds.split(",").mapNotNull { it.toLongOrNull() }

        // Garment 10 is flagged isForgotten with wearCount 0.
        assertTrue("Rescue look should include the forgotten piece", 10L in ids)
    }

    @Test
    fun `estimated value equals the real price of the pieces in the look`() {
        val result = repository()
            .generateOutfitsForOccasion("Reunión de trabajo", "Quiero sentirme segura", closet)

        val outfit = result.first()
        val ids = outfit.garmentIds.split(",").mapNotNull { it.toLongOrNull() }
        val expected = closet.filter { it.id in ids }.sumOf { it.originalPrice }

        // Was a hardcoded 195.000 regardless of what the look contained.
        assertEquals(expected, outfit.estimatedSavings, 0.0)
    }

    @Test
    fun `persisted proposals receive distinct non-zero ids`() = runTest {
        val repo = repository()

        val proposals = repo.generateAndPersistProposals(
            "Reunión de trabajo",
            "Quiero sentirme segura",
            closet,
        )

        assertTrue("Expected proposals", proposals.isNotEmpty())
        assertTrue("No proposal may keep id 0", proposals.none { it.id == 0L })
        assertEquals(
            "Proposal ids must be unique so favourites target the right row",
            proposals.size,
            proposals.map { it.id }.toSet().size,
        )
    }
}
