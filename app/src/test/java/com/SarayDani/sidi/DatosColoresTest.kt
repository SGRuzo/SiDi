package com.SarayDani.sidi

import androidx.compose.ui.graphics.Color
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for the Datos data class.
 */
class DatosTest {

    @Test
    fun `Datos should have default values`() {
        val datos = Datos()
        
        assertEquals(0, datos.ronda)
        assertTrue(datos.secuencia.isEmpty())
        assertTrue(datos.secuenciaIntroducida.isEmpty())
        assertEquals(0, datos.record)
        assertEquals(Estados.Inicio, datos.estados)
    }

    @Test
    fun `Datos should accept custom ronda`() {
        val datos = Datos(ronda = 5)
        assertEquals(5, datos.ronda)
    }

    @Test
    fun `Datos should accept custom secuencia`() {
        val secuencia = mutableListOf(1, 2, 3)
        val datos = Datos(secuencia = secuencia)
        assertEquals(secuencia, datos.secuencia)
    }

    @Test
    fun `Datos should accept custom secuenciaIntroducida`() {
        val secuenciaIntroducida = mutableListOf(0, 1)
        val datos = Datos(secuenciaIntroducida = secuenciaIntroducida)
        assertEquals(secuenciaIntroducida, datos.secuenciaIntroducida)
    }

    @Test
    fun `Datos should accept custom record`() {
        val datos = Datos(record = 10)
        assertEquals(10, datos.record)
    }

    @Test
    fun `Datos should accept custom estados`() {
        val datos = Datos(estados = Estados.GameOver)
        assertEquals(Estados.GameOver, datos.estados)
    }

    @Test
    fun `Datos should allow modification of ronda`() {
        val datos = Datos()
        datos.ronda = 3
        assertEquals(3, datos.ronda)
    }

    @Test
    fun `Datos should allow modification of record`() {
        val datos = Datos()
        datos.record = 7
        assertEquals(7, datos.record)
    }

    @Test
    fun `Datos should allow adding to secuencia`() {
        val datos = Datos()
        datos.secuencia.add(2)
        assertEquals(1, datos.secuencia.size)
        assertEquals(2, datos.secuencia[0])
    }

    @Test
    fun `Datos should allow adding to secuenciaIntroducida`() {
        val datos = Datos()
        datos.secuenciaIntroducida.add(1)
        assertEquals(1, datos.secuenciaIntroducida.size)
        assertEquals(1, datos.secuenciaIntroducida[0])
    }

    @Test
    fun `Datos copy should create independent copy`() {
        val original = Datos(ronda = 5, record = 10)
        val copy = original.copy(ronda = 6)
        
        assertEquals(5, original.ronda)
        assertEquals(6, copy.ronda)
        assertEquals(10, copy.record)
    }
}

/**
 * Unit tests for the Colores enum.
 */
class ColoresTest {

    @Test
    fun `Colores should have CLASE_ROJO`() {
        val color = Colores.CLASE_ROJO
        assertNotNull(color)
        assertEquals("roxo", color.txt)
        assertNotNull(color.color)
    }

    @Test
    fun `Colores should have CLASE_VERDE`() {
        val color = Colores.CLASE_VERDE
        assertNotNull(color)
        assertEquals("verde", color.txt)
        assertNotNull(color.color)
    }

    @Test
    fun `Colores should have CLASE_AZUL`() {
        val color = Colores.CLASE_AZUL
        assertNotNull(color)
        assertEquals("azul", color.txt)
        assertNotNull(color.color)
    }

    @Test
    fun `Colores should have CLASE_AMARILLO`() {
        val color = Colores.CLASE_AMARILLO
        assertNotNull(color)
        assertEquals("melo", color.txt)
        assertNotNull(color.color)
    }

    @Test
    fun `Colores should have CLASE_START`() {
        val color = Colores.CLASE_START
        assertNotNull(color)
        assertEquals("Start", color.txt)
        assertEquals(Color.Magenta, color.color)
    }

    @Test
    fun `Colores should have exactly 5 values`() {
        val colores = Colores.entries
        assertEquals(5, colores.size)
    }

    @Test
    fun `Colores ROJO should have correct hex color`() {
        val expectedColor = Color(0xFFBB0000)
        assertEquals(expectedColor, Colores.CLASE_ROJO.color)
    }

    @Test
    fun `Colores VERDE should have correct hex color`() {
        val expectedColor = Color(0xFF00BB00)
        assertEquals(expectedColor, Colores.CLASE_VERDE.color)
    }

    @Test
    fun `Colores AZUL should have correct hex color`() {
        val expectedColor = Color(0xFF0082E7)
        assertEquals(expectedColor, Colores.CLASE_AZUL.color)
    }

    @Test
    fun `Colores AMARILLO should have correct hex color`() {
        val expectedColor = Color(0xFFFFC107)
        assertEquals(expectedColor, Colores.CLASE_AMARILLO.color)
    }

    @Test
    fun `all Colores should have non-empty txt`() {
        Colores.entries.forEach { color ->
            assertTrue("Color ${color.name} should have non-empty txt", color.txt.isNotEmpty())
        }
    }
}
