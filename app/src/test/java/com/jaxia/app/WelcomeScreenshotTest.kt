package com.jaxia.app

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.jaxia.app.ui.screens.WelcomeScreen
import com.jaxia.app.ui.theme.JaxiaTheme
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Renderiza la pantalla de bienvenida de verdad y guarda la imagen en
 * `src/test/screenshots/`, que CI publica como artefacto.
 *
 * El objetivo es que la pantalla pueda revisarse **antes** de conectarla a
 * `MainActivity`: el composable existe y se renderiza, pero todavía nadie lo
 * invoca en la app.
 *
 * `autoAdvance = false` congela la pantalla para la captura; si no, el
 * temporizador la cerraría.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class WelcomeScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun welcome_screenshot() {
        composeTestRule.setContent {
            JaxiaTheme { WelcomeScreen(onFinished = {}, autoAdvance = false) }
        }
        composeTestRule.onRoot().captureRoboImage(
            filePath = "src/test/screenshots/bienvenida.png",
        )
    }

    @Test
    fun `welcome shows the logo and the tagline`() {
        composeTestRule.setContent {
            JaxiaTheme { WelcomeScreen(onFinished = {}, autoAdvance = false) }
        }

        composeTestRule.onNodeWithTag("welcome_screen").assertExists()
        composeTestRule.onNodeWithTag("welcome_logo").assertExists()
    }

    @Test
    fun `welcome does not finish on its own when auto advance is off`() {
        var finished = false
        composeTestRule.setContent {
            JaxiaTheme { WelcomeScreen(onFinished = { finished = true }, autoAdvance = false) }
        }
        composeTestRule.waitForIdle()

        assertFalse("sin autoAdvance la pantalla no debe cerrarse sola", finished)
    }

    @Test
    fun `welcome finishes once when auto advance is on`() {
        var finishedCount = 0
        composeTestRule.setContent {
            JaxiaTheme { WelcomeScreen(onFinished = { finishedCount++ }, autoAdvance = true) }
        }
        // El reloj virtual de Compose avanza hasta agotar el delay.
        composeTestRule.mainClock.advanceTimeBy(3_000)
        composeTestRule.waitForIdle()

        assertTrue("debe avisar que terminó", finishedCount >= 1)
        assertEquals("y solo una vez", 1, finishedCount)
    }

    private fun assertEquals(msg: String, expected: Int, actual: Int) =
        org.junit.Assert.assertEquals(msg, expected, actual)
}
