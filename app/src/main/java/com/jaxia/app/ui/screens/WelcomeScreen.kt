package com.jaxia.app.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.jaxia.app.R
import com.jaxia.app.ui.theme.EspressoTextSecondary
import com.jaxia.app.ui.theme.LinenBackground
import com.jaxia.app.ui.theme.TerracottaPrimary
import kotlinx.coroutines.delay

/** Cuánto permanece visible la bienvenida antes de pasar a la app. */
private const val WELCOME_DURATION_MS = 1900L
private const val FADE_IN_MS = 600

/**
 * Pantalla de bienvenida de JAXIA.
 *
 * Muestra el logotipo **completo** —con el wordmark y el lema—, que es donde
 * tiene sentido: a tamaño de pantalla el texto sí es legible, a diferencia del
 * icono del launcher, donde solo cabe el símbolo.
 *
 * El fondo coincide con `Theme.Jaxia` (`@color/jaxia_cream`), de modo que no hay
 * destello blanco entre el arranque del sistema y el primer frame de Compose.
 *
 * [onFinished] se dispara solo una vez, ya sea por el temporizador o por un
 * toque, para que la usuaria pueda saltarla.
 */
@Composable
fun WelcomeScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    autoAdvance: Boolean = true,
) {
    var visible by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }

    fun finishOnce() {
        if (!finished) {
            finished = true
            onFinished()
        }
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = FADE_IN_MS, easing = LinearEasing),
        label = "welcomeAlpha",
    )
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.94f,
        animationSpec = tween(durationMillis = FADE_IN_MS, easing = LinearEasing),
        label = "welcomeScale",
    )

    LaunchedEffect(Unit) {
        visible = true
        if (autoAdvance) {
            delay(WELCOME_DURATION_MS)
            finishOnce()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LinenBackground)
            .testTag("welcome_screen"),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_jaxia_logo),
                // La pantalla ya muestra el nombre y el lema como texto debajo,
                // así que el logotipo es decorativo para TalkBack.
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(300.dp)
                    .alpha(alpha)
                    .scale(scale)
                    .testTag("welcome_logo"),
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Vístete con lo que tienes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = TerracottaPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(alpha),
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Tu clóset, aprovechado al máximo",
                style = MaterialTheme.typography.bodyMedium,
                color = EspressoTextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(alpha),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun WelcomeScreenPreview() {
    WelcomeScreen(onFinished = {}, autoAdvance = false)
}
