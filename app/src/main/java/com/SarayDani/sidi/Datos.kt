package com.SarayDani.sidi

import androidx.compose.ui.graphics.Color

data class Datos (
    var ronda: Int = 0,                       // Número de ronda actual
    var secuencia: MutableList<Int> = mutableListOf(),  // Secuencia generada por el juego
    var secuenciaIntroducida: MutableList<Int> = mutableListOf(), // Secuencia del jugador
    var record: Int = 0,                      // Máximo número de rondas superadas
    var estados: Estados = Estados.Inicio,    // Estado actual del juego
)

enum class Colores(val color: Color, val txt: String) {
    CLASE_ROJO(color = Color(0xFFBB0000), txt = "roxo"),
    CLASE_VERDE(color = Color(0xFF00BB00), txt = "verde"),
    CLASE_AZUL(color = Color(0xFF0082E7), txt = "azul"),
    CLASE_AMARILLO(color = Color(0xFFFFC107), txt = "melo"),
    CLASE_START(color = Color.Magenta, txt = "Start")
}

enum class Estados {
    Inicio,
    GenerarSecuencia,
    IntroducirSecuencia,
    GameOver,
    Pausa
}