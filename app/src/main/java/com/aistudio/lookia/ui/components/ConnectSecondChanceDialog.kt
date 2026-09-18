package com.aistudio.lookia.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.lookia.data.model.SecondChanceItem
import com.aistudio.lookia.ui.theme.EspressoTextPrimary
import com.aistudio.lookia.ui.theme.EspressoTextSecondary
import com.aistudio.lookia.ui.theme.SageContainer
import com.aistudio.lookia.ui.theme.SageSecondary
import com.aistudio.lookia.ui.theme.TerracottaPrimary

@Composable
fun ConnectSecondChanceDialog(
    item: SecondChanceItem,
    onDismiss: () -> Unit,
    onConfirmConnect: () -> Unit
) {
    var message by remember {
        mutableStateOf(
            "¡Hola ${item.ownerName}! Vi tu ${item.title} en Segunda Oportunidad. Me encantaría coordinar el ${item.actionType.lowercase()} con respeto y gratitud."
        )
    }
    var isSent by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("connect_second_chance_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = TerracottaPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Conectar con ${item.ownerName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EspressoTextPrimary
                )
            }
        },
        text = {
            if (isSent) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SageSecondary,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "¡Interés enviado con éxito!",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = EspressoTextPrimary
                    )
                    Text(
                        text = "${item.ownerName} ha recibido tu interés. Podrán coordinar los detalles en un marco de respeto y moda consciente.",
                        style = MaterialTheme.typography.bodySmall,
                        color = EspressoTextSecondary,
                        lineHeight = 18.sp
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = SageSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Espacio protegido: LookIA cuida tu privacidad. No compartimos tu número ni dirección hasta que ambas partes lo acuerden.",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = EspressoTextPrimary
                            )
                        }
                    }

                    Text(
                        text = "Prenda: ${item.title}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = EspressoTextPrimary
                    )
                    Text(
                        text = "Modalidad: ${item.actionType} • ${item.exchangeOrPrice}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TerracottaPrimary,
                        fontWeight = FontWeight.Medium
                    )

                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Tu mensaje de conexión") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        minLines = 3
                    )
                }
            }
        },
        confirmButton = {
            if (isSent) {
                Button(
                    onClick = {
                        onConfirmConnect()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SageSecondary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cerrar")
                }
            } else {
                Button(
                    onClick = {
                        isSent = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Enviar interés respetuoso")
                }
            }
        },
        dismissButton = {
            if (!isSent) {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        }
    )
}
