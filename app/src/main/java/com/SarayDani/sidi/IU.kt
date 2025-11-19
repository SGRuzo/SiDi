package com.SarayDani.sidi

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun IU(vm: MyViewModel) {

    val estado by vm.estadoActual.collectAsState()
    val ronda by vm.ronda.collectAsState()
    val record by vm.record.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF4C007A)) // Fondo morado ORIGINAL
            .statusBarsPadding()           // <-- evita solapamiento con barra superior / notch
            .navigationBarsPadding()       // <-- opcional: evita solapamiento con barra de navegación
    ) {

        TopNotification(estado = estado)
        // ------------------------------
        // BARRA SUPERIOR: RONDA (antes Score)
        // ------------------------------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Mostramos el récord actual
                Text(
                    text = "Récord: $record",
                    color = Color.White,
                    fontSize = 20.sp
                )
                Text(
                    text = "Ronda: $ronda",
                    color = Color.White,
                    fontSize = 20.sp
                )
            }
        }

        // ------------------------------
        // ÁREA PRINCIPAL (Botonera + Start en medio)
        // ------------------------------
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {

            // 4 botones originales
            Botonera(
                modifier = Modifier.fillMaxSize(),
                onColorClick = { vm.introducirSecuencia(it) }
            )

            // Botón START pequeño y centrado COMO EL ORIGINAL
            if (estado == Estados.Inicio) {
                Button(
                    onClick = { vm.empezarJuego() },
                    modifier = Modifier.size(80.dp),  // Tamaño original
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4C007A) // El mismo color que el original
                    ),
                    shape = CircleShape
                ) {
                    Text(
                        text = "▶",
                        fontSize = 40.sp,
                        color = Color.White
                    )
                }
            }
        }
    }

    // Comprobamos si el estado es GameOver para mostrar el diálogo
    if (estado == Estados.GameOver) {
        GameOverDialog(
            rondaActual = ronda,
            record = record,
            onPlayAgain = { vm.empezarJuego() },
            onClose = { vm.resetToInicio() } // Nueva función en el VM
        )
    }
}



/**
 * Diálogo que se muestra al perder (Game Over).
 */
@Composable
fun GameOverDialog(
    rondaActual: Int,
    record: Int,
    onPlayAgain: () -> Unit,
    onClose: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose, // Se llama si el usuario pulsa fuera del diálogo
        containerColor = Color(0xFF4C007A),
        title = {
            Text(
                text = "¡Has perdido!",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color(0xFFFDFBF6) // Título en tono crema claro
            )
        },
        text = {
            Column {
                Text(
                    text = "Llegaste a la ronda: $rondaActual",
                    fontSize = 18.sp,
                    color = Color(0xFFFDFBF6) // Texto principal en tono cálido
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tu récord es: $record",
                    fontSize = 18.sp,
                    color = Color(0xFFFDFBF6) // Texto secundario más claro
                )
            }
        },
        confirmButton = {
            Button(onClick = onPlayAgain) {
                Text("Jugar de Nuevo")
            }
        },
        dismissButton = {
            TextButton(onClick = onClose) {
                Text("Cerrar")
            }
        }
    )
}


/**
 * Banner superior que aparece/desaparece según el estado.
 */
@Composable
fun TopNotification(estado: Estados) {
    var visible by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    val bgColor = when (estado) {
        Estados.GenerarSecuencia -> Color(0xFF2E7D32) // verde oscuro
        Estados.IntroducirSecuencia -> Color(0xFF1565C0) // azul
        Estados.GameOver -> Color(0xFFB00020) // rojo
        Estados.Inicio -> Color(0xFF6A1B9A) // morado
        Estados.Pausa -> Color(0xFF37474F) // gris
    }

    LaunchedEffect(estado) {
        message = when (estado) {
            Estados.GenerarSecuencia -> "Reproduciendo secuencia..."
            Estados.IntroducirSecuencia -> "Tu turno: introduce la secuencia"
            Estados.GameOver -> "¡Game Over!"
            Estados.Inicio -> "Pulsa START para comenzar"
            Estados.Pausa -> "Pausa"
        }
        // Mostrar banner brevemente
        visible = true

    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(durationMillis = 350)
        ) + fadeIn(animationSpec = tween(200)),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(durationMillis = 300)
        ) + fadeOut(animationSpec = tween(200))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgColor)
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * BOTONERA EXACTA A LA ORIGINAL
 */
@Composable
fun Botonera(
    modifier: Modifier = Modifier,
    onColorClick: (Int) -> Unit
) {
    Column(modifier = modifier) {

        // Fila superior: ROJO - AMARILLO
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(2.dp)
            ) {
                Boton(Colores.CLASE_ROJO, 0, onColorClick)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(2.dp)
            ) {
                Boton(Colores.CLASE_AMARILLO, 3, onColorClick)
            }
        }

        // Fila inferior: VERDE - AZUL
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(2.dp)
            ) {
                Boton(Colores.CLASE_VERDE, 1, onColorClick)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(2.dp)
            ) {
                Boton(Colores.CLASE_AZUL, 2, onColorClick)
            }
        }
    }
}

/**
 * BOTÓN DE COLOR EXACTO AL ORIGINAL
 */
@Composable
fun Boton(color: Colores, index: Int, onColorClick: (Int) -> Unit) {
    Button(
        onClick = {
            Log.d("IU", "Click en ${color.txt}")
            onColorClick(index)
        },
        modifier = Modifier.fillMaxSize(),
        colors = ButtonDefaults.buttonColors(color.color),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(color.txt, fontSize = 10.sp, color = Color.Black)
    }
}
