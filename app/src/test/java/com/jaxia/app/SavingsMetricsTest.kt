package com.jaxia.app

import com.jaxia.app.data.model.Garment
import com.jaxia.app.data.model.Outfit
import com.jaxia.app.data.repository.JaxiaRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Covers the savings engine, which previously reported fabricated figures
 * (AUDITORIA.md A-10) and had no test at all (M-06).
 *
 * `calculateSavingsMetrics` is a pure function, so the DAOs are never touched
 * and can be left null behind the repository's constructor.
 */
class SavingsMetricsTest {

    private val repository = JaxiaRepository(
        garmentDao = FakeDaos.garment(),
        avatarDao = FakeDaos.avatar(),
        outfitDao = FakeDaos.outfit(),
        communityDao = FakeDaos.community(),
        secondChanceDao = FakeDaos.secondChance(),
        // Job() with no dispatcher: seeding is launched but never runs, which is
        // exactly what these pure-function tests want.
        externalScope = CoroutineScope(Job()),
    )

    private fun garment(
        id: Long,
        price: Double,
        wearCount: Int,
        category: String = "Tops",
    ) = Garment(
        id = id,
        name = "Prenda $id",
        category = category,
        color = "Terracota",
        texture = "Algodón",
        style = "Casual",
        originalPrice = price,
        wearCount = wearCount,
    )

    @Test
    fun `empty closet reports zero, not a fabricated floor`() {
        val metrics = repository.calculateSavingsMetrics(emptyList(), emptyList())

        // The old engine floored outfit count at 3 and multiplied by 180.000,
        // so an empty closet claimed "$540.000 saved".
        assertEquals(0.0, metrics.reWearValueCOP, 0.0)
        assertEquals(0, metrics.closetUtilizationRate)
        assertEquals(0.0, metrics.averageCostPerWear, 0.0)
        assertEquals(0, metrics.garmentsRescued)
    }

    @Test
    fun `garments worn once contribute no re-wear value`() {
        val garments = listOf(
            garment(id = 1, price = 100_000.0, wearCount = 1),
            garment(id = 2, price = 200_000.0, wearCount = 1),
        )

        val metrics = repository.calculateSavingsMetrics(garments, emptyList())

        // Buying and wearing once is not re-wearing anything.
        assertEquals(0.0, metrics.reWearValueCOP, 0.0)
    }

    @Test
    fun `re-wear value counts every wear after the first`() {
        val garments = listOf(
            garment(id = 1, price = 100_000.0, wearCount = 4), // 3 extra wears
            garment(id = 2, price = 50_000.0, wearCount = 3),  // 2 extra wears
        )

        val metrics = repository.calculateSavingsMetrics(garments, emptyList())

        // 100.000*3 + 50.000*2
        assertEquals(400_000.0, metrics.reWearValueCOP, 0.0)
    }

    @Test
    fun `never-worn garments cannot push re-wear value negative`() {
        val garments = listOf(garment(id = 1, price = 100_000.0, wearCount = 0))

        val metrics = repository.calculateSavingsMetrics(garments, emptyList())

        // wearCount - 1 would be -1 without the coerceAtLeast(0) guard.
        assertEquals(0.0, metrics.reWearValueCOP, 0.0)
    }

    @Test
    fun `utilization is the share of garments worn three or more times`() {
        val garments = listOf(
            garment(id = 1, price = 10_000.0, wearCount = 5),
            garment(id = 2, price = 10_000.0, wearCount = 3),
            garment(id = 3, price = 10_000.0, wearCount = 1),
            garment(id = 4, price = 10_000.0, wearCount = 0),
        )

        val metrics = repository.calculateSavingsMetrics(garments, emptyList())

        assertEquals(50, metrics.closetUtilizationRate)
        assertEquals(2, metrics.garmentsRescued)
    }

    @Test
    fun `average cost per wear divides spend by total wears`() {
        val garments = listOf(
            garment(id = 1, price = 100_000.0, wearCount = 5),
            garment(id = 2, price = 100_000.0, wearCount = 5),
        )

        val metrics = repository.calculateSavingsMetrics(garments, emptyList())

        // 200.000 / 10
        assertEquals(20_000.0, metrics.averageCostPerWear, 0.0)
    }

    @Test
    fun `zero total wears does not divide by zero`() {
        val garments = listOf(garment(id = 1, price = 100_000.0, wearCount = 0))

        val metrics = repository.calculateSavingsMetrics(garments, emptyList())

        assertEquals(0.0, metrics.averageCostPerWear, 0.0)
    }

    @Test
    fun `outfit count is reported as-is, with no minimum`() {
        val outfits = listOf(
            Outfit(
                id = 1,
                title = "Look",
                occasion = "Trabajo",
                mood = "Segura",
                proposalType = "Recomendado",
                garmentIds = "1",
            )
        )

        val metrics = repository.calculateSavingsMetrics(
            listOf(garment(id = 1, price = 10_000.0, wearCount = 1)),
            outfits,
        )

        // The old engine applied coerceAtLeast(3) here.
        assertEquals(1, metrics.totalOutfitsGenerated)
    }
}
