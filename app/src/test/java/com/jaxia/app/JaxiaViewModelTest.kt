package com.jaxia.app

import com.jaxia.app.data.model.AvatarProfile
import com.jaxia.app.data.model.CommunityPost
import com.jaxia.app.data.model.Garment
import com.jaxia.app.data.model.Outfit
import com.jaxia.app.data.model.SecondChanceItem
import com.jaxia.app.data.repository.JaxiaRepository
import com.jaxia.app.ui.JaxiaViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * The ViewModel had zero coverage (AUDITORIA.md M-06), which is how the state
 * race in `init` (A-04) and the dead `activeTab` field (A-06) survived.
 *
 * Every DAO is pre-seeded so `seedInitialDataIfNeeded` finds each table
 * non-empty and leaves the fixtures alone — the assertions below depend on
 * exactly this data, not on DefaultSeedData.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class JaxiaViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val garments = listOf(
        Garment(
            id = 1, name = "Blazer Sastre", category = "Abrigos y Chaquetas",
            color = "Terracota", texture = "Lino", style = "Elegante",
            originalPrice = 200_000.0, wearCount = 4,
        ),
        Garment(
            id = 2, name = "Blusa de Seda", category = "Tops",
            color = "Marfil", texture = "Seda", style = "Elegante",
            originalPrice = 120_000.0, wearCount = 3,
        ),
        Garment(
            id = 3, name = "Camiseta Algodón", category = "Tops",
            color = "Blanco", texture = "Algodón", style = "Cómodo",
            originalPrice = 40_000.0, wearCount = 2,
        ),
        Garment(
            id = 4, name = "Pantalón Sastre", category = "Pantalones y Faldas",
            color = "Negro", texture = "Lana", style = "Elegante",
            originalPrice = 150_000.0, wearCount = 5,
        ),
        Garment(
            id = 5, name = "Vestido Midi", category = "Vestidos",
            color = "Terracota", texture = "Lino", style = "Elegante",
            originalPrice = 180_000.0, wearCount = 1,
        ),
        Garment(
            id = 6, name = "Mocasines", category = "Calzado",
            color = "Cuero", texture = "Cuero", style = "Elegante",
            originalPrice = 160_000.0, wearCount = 6,
        ),
    )

    private fun viewModel(scope: kotlinx.coroutines.CoroutineScope): JaxiaViewModel {
        val repository = JaxiaRepository(
            garmentDao = FakeDaos.garment(garments),
            avatarDao = FakeDaos.avatar(AvatarProfile(id = 1, userName = "Prueba")),
            outfitDao = FakeDaos.outfit(
                listOf(
                    Outfit(
                        id = 1, title = "Look guardado", occasion = "Trabajo",
                        mood = "Segura", proposalType = "Recomendado",
                        garmentIds = "1,2", isFavorite = true, wornCount = 1,
                    )
                )
            ),
            communityDao = FakeDaos.community(
                listOf(
                    CommunityPost(
                        id = 1, authorName = "Ejemplo",
                        question = "¿Cuál va mejor?", lookDescription = "Dos opciones",
                    )
                )
            ),
            secondChanceDao = FakeDaos.secondChance(
                listOf(
                    SecondChanceItem(
                        id = 1, ownerName = "Ejemplo", title = "Falda",
                        category = "Pantalones y Faldas", actionType = "Regalo con amor",
                    )
                )
            ),
            externalScope = scope,
        )
        return JaxiaViewModel(repository)
    }

    @Test
    fun `state exposes the database rows once collected`() = runTest(UnconfinedTestDispatcher()) {
        val vm = viewModel(this)
        val state = vm.uiState.value

        assertEquals(6, state.garments.size)
        assertEquals("Prueba", state.avatarProfile.userName)
        assertEquals(1, state.communityPosts.size)
        assertEquals(1, state.secondChanceItems.size)
        assertFalse("isLoading debe apagarse tras la primera emisión", state.isLoading)
    }

    @Test
    fun `proposals are seeded once and carry persisted ids`() = runTest(UnconfinedTestDispatcher()) {
        val vm = viewModel(this)
        val proposals = vm.uiState.value.generatedProposals

        assertTrue("debe generar propuestas", proposals.isNotEmpty())
        assertTrue("ninguna propuesta puede quedar con id 0", proposals.none { it.id == 0L })
    }

    @Test
    fun `wearing a second top replaces the first`() = runTest(UnconfinedTestDispatcher()) {
        val vm = viewModel(this)
        vm.clearProbador()

        vm.toggleGarmentWearing(garments[1])   // Blusa de Seda (Tops)
        vm.toggleGarmentWearing(garments[2])   // Camiseta Algodón (Tops)

        val wearing = vm.uiState.value.selectedWearingGarments
        assertEquals("Tops es un slot único", 1, wearing.count { it.category == "Tops" })
        assertEquals(3L, wearing.first { it.category == "Tops" }.id)
    }

    @Test
    fun `wearing a dress displaces top and bottom`() = runTest(UnconfinedTestDispatcher()) {
        val vm = viewModel(this)
        vm.clearProbador()

        vm.toggleGarmentWearing(garments[1])   // Top
        vm.toggleGarmentWearing(garments[3])   // Bottom
        vm.toggleGarmentWearing(garments[4])   // Vestido

        val wearing = vm.uiState.value.selectedWearingGarments
        assertTrue("el vestido reemplaza top y bottom", wearing.none {
            it.category == "Tops" || it.category == "Pantalones y Faldas"
        })
        assertEquals(1, wearing.count { it.category == "Vestidos" })
    }

    @Test
    fun `toggling the same garment twice removes it`() = runTest(UnconfinedTestDispatcher()) {
        val vm = viewModel(this)
        vm.clearProbador()

        vm.toggleGarmentWearing(garments[0])
        vm.toggleGarmentWearing(garments[0])

        assertTrue(vm.uiState.value.selectedWearingGarments.isEmpty())
    }

    @Test
    fun `deleting a garment drops it from the try-on selection`() = runTest(UnconfinedTestDispatcher()) {
        val vm = viewModel(this)
        vm.clearProbador()
        vm.toggleGarmentWearing(garments[0])
        assertEquals(1, vm.uiState.value.selectedWearingGarments.size)

        vm.deleteGarment(garments[0].id)

        assertTrue(
            "la prenda borrada no puede seguir puesta",
            vm.uiState.value.selectedWearingGarments.none { it.id == garments[0].id },
        )
        assertTrue(vm.uiState.value.garments.none { it.id == garments[0].id })
    }

    @Test
    fun `a database emission does not clobber freshly requested proposals`() =
        runTest(UnconfinedTestDispatcher()) {
            val vm = viewModel(this)

            vm.requestOutfits("Plan casual", "Quiero sentirme cómoda")
            val requested = vm.uiState.value.generatedProposals
            assertTrue(requested.isNotEmpty())

            // Any write triggers a new emission from the Room flows. Before the
            // A-04 fix, the combine transform re-read _uiState and overwrote the
            // proposals the user had just asked for.
            vm.markGarmentWorn(garments[0].id)

            assertEquals(
                "las propuestas deben sobrevivir a una emisión de la base de datos",
                requested.map { it.id },
                vm.uiState.value.generatedProposals.map { it.id },
            )
            assertEquals("Plan casual", vm.uiState.value.currentOccasion)
        }

    @Test
    fun `adding a garment surfaces in state`() = runTest(UnconfinedTestDispatcher()) {
        val vm = viewModel(this)
        val before = vm.uiState.value.garments.size

        vm.addGarment(
            name = "Chaqueta Denim", category = "Abrigos y Chaquetas",
            color = "Azul", colorHex = "#2F4F6F", texture = "Denim",
            style = "Casual", price = 90_000.0,
        )

        assertEquals(before + 1, vm.uiState.value.garments.size)
        assertNotNull(vm.uiState.value.garments.find { it.name == "Chaqueta Denim" })
    }

    @Test
    fun `smart purchase analysis is set and cleared`() = runTest(UnconfinedTestDispatcher()) {
        val vm = viewModel(this)

        vm.evaluateSmartPurchase("Otra blusa", 130_000.0, "Tops", "Marfil", "Elegante")
        val analysis = vm.uiState.value.smartPurchaseAnalysis
        assertNotNull(analysis)
        assertEquals("Otra blusa", analysis!!.itemName)

        vm.clearSmartPurchaseAnalysis()
        assertNull(vm.uiState.value.smartPurchaseAnalysis)
    }

    @Test
    fun `deleting all user data clears the closet`() = runTest(UnconfinedTestDispatcher()) {
        val vm = viewModel(this)
        assertTrue(vm.uiState.value.garments.isNotEmpty())

        vm.deleteAllUserData()

        // The repository re-seeds demo content afterwards, so the assertion is
        // that the user's own fixtures are gone, not that the table is empty.
        assertTrue(
            "las prendas del usuario deben desaparecer",
            vm.uiState.value.garments.none { it.name == "Blazer Sastre" },
        )
        assertTrue(vm.uiState.value.selectedWearingGarments.isEmpty())
        assertNull(vm.uiState.value.smartPurchaseAnalysis)
    }

    @Test
    fun `daily inspiration cycles backwards without crashing`() = runTest(UnconfinedTestDispatcher()) {
        val vm = viewModel(this)
        val start = vm.uiState.value.todayInspiration.dayNumber

        // A negative offset used to rely on adding all.size before the modulo;
        // this pins that a backwards step lands on a real message.
        vm.cycleDailyInspiration(-1)
        val back = vm.uiState.value.todayInspiration.dayNumber
        assertTrue(back > 0)

        vm.cycleDailyInspiration(1)
        assertEquals(start, vm.uiState.value.todayInspiration.dayNumber)
    }

    @Test
    fun `category filter is stored`() = runTest(UnconfinedTestDispatcher()) {
        val vm = viewModel(this)
        vm.setCategoryFilter("Calzado")
        assertEquals("Calzado", vm.uiState.value.selectedCategoryFilter)
    }
}
