package com.aistudio.lookia.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.aistudio.lookia.data.model.Outfit
import com.aistudio.lookia.ui.theme.CardBorder
import com.aistudio.lookia.ui.theme.EspressoTextMuted
import com.aistudio.lookia.ui.theme.EspressoTextPrimary
import com.aistudio.lookia.ui.theme.EspressoTextSecondary
import com.aistudio.lookia.ui.theme.SageSecondary
import com.aistudio.lookia.ui.theme.TerracottaDark
import com.aistudio.lookia.ui.theme.TerracottaPrimary

@Composable
fun FeedbackSurveyDialog(
    outfit: Outfit,
    onDismiss: () -> Unit,
    onSubmit: (comfortable: Boolean, authentic: Boolean, confident: Boolean, wouldRewear: Boolean, fitOccasion: Boolean, notes: String) -> Unit
) {
    var comfortable by remember { mutableStateOf(true) }
    var authentic by remember { mutableStateOf(true) }
    var confident by remember { mutableStateOf(true) }
    var wouldRewear by remember { mutableStateOf(true) }
    var fitOccasion by remember { mutableStateOf(true) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("feedback_survey_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Spa,
                    contentDescription = null,
                    tint = TerracottaPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Autentica tu look",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EspressoTextPrimary
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Tu opinión ayuda a LookIA a entender qué te hace sentir bien, no qué dictan las tendencias.",
                    style = MaterialTheme.typography.bodySmall,
                    color = EspressoTextSecondary,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                FeedbackQuestionToggle(
                    question = "¿Te sentiste cómoda?",
                    isChecked = comfortable,
                    onToggle = { comfortable = it }
                )

                FeedbackQuestionToggle(
                    question = "¿Te sentiste auténtica?",
                    isChecked = authentic,
                    onToggle = { authentic = it }
                )

                FeedbackQuestionToggle(
                    question = "¿Te sentiste segura de ti misma?",
                    isChecked = confident,
                    onToggle = { confident = it }
                )

                FeedbackQuestionToggle(
                    question = "¿Volverías a utilizar este look?",
                    isChecked = wouldRewear,
                    onToggle = { wouldRewear = it }
                )

                FeedbackQuestionToggle(
                    question = "¿Respondió bien a la ocasión?",
                    isChecked = fitOccasion,
                    onToggle = { fitOccasion = it }
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas de estilo (opcional)") },
                    placeholder = { Text("Ej: Me encantó combinar los mocasines con el blazer...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(comfortable, authentic, confident, wouldRewear, fitOccasion, notes)
                },
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Guardar reflexión")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
fun FeedbackQuestionToggle(
    question: String,
    isChecked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF9F6F3))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = question,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = EspressoTextPrimary,
            modifier = Modifier.weight(1f)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isChecked) TerracottaPrimary else Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isChecked) TerracottaPrimary else CardBorder),
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onToggle(true) }
            ) {
                Text(
                    text = "Sí",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isChecked) Color.White else EspressoTextSecondary,
                    fontWeight = FontWeight.Bold
                )
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (!isChecked) Color(0xFFC62828) else Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (!isChecked) Color(0xFFC62828) else CardBorder),
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onToggle(false) }
            ) {
                Text(
                    text = "No",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (!isChecked) Color.White else EspressoTextSecondary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
