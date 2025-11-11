package com.SarayDani.sidi

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun IU() {
    val score = remember { mutableStateOf(0) }
    val gameState = remember { mutableStateOf(GameState.INACTIVE) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF360157)) // Fondo morado
    ) {
        // Barra superior para el score
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            ScoreDisplay(score = score.value)
        }

        // Área principal con botones y control superpuesto
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            // Botonera que ocupa el espacio disponible
            Botonera(
                modifier = Modifier.fillMaxSize()
            )

            // Botón de Start/Pause superpuesto y más pequeño
            ControlButton(
                gameState = gameState.value,
                onToggle = {
                    gameState.value = if (gameState.value == GameState.INACTIVE)
                        GameState.ACTIVE
                    else
                        GameState.INACTIVE
                },
                modifier = Modifier.size(80.dp) // Más pequeño
            )
        }
    }
}

@Composable
fun ScoreDisplay(score: Int, modifier: Modifier = Modifier) {
    Text(
        text = "Score: $score",
        color = Color.White,
        fontSize = 20.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        modifier = modifier
    )
}

@Composable
fun ControlButton(
    gameState: GameState,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onToggle,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = (Color(0xFF360157))
        ),
        shape =  CircleShape// Sin bordes redondeados
    ) {
        Text(
            text = if (gameState == GameState.INACTIVE) "▶" else "||",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold

        )
    }
}

@Composable
fun Botonera(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        // Fila superior (rojo y amarillo)
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(2.dp) // Reducido el padding
            ) {
                Boton(Colores.CLASE_ROJO)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(2.dp) // Reducido el padding
            ) {
                Boton(Colores.CLASE_AMARILLO)
            }
        }
        // Fila inferior (verde y azul)
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(2.dp) // Reducido el padding
            ) {
                Boton(Colores.CLASE_VERDE)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(2.dp) // Reducido el padding
            ) {
                Boton(Colores.CLASE_AZUL)
            }
        }
    }
}

@Composable
fun Boton(enum_color: Colores) {
    Button(
        colors = ButtonDefaults.buttonColors(containerColor = enum_color.color),
        onClick = { Log.d("Juego", "Click en ${enum_color.txt}") },
        modifier = Modifier.fillMaxSize(),
        shape = RectangleShape // Sin bordes redondeados
    ) {
        Text(text = enum_color.txt, fontSize = 10.sp)
    }
}

enum class GameState {
    INACTIVE,
    ACTIVE
}

@Preview(showBackground = true)
@Composable
fun IUPreview() {
    IU()
}
