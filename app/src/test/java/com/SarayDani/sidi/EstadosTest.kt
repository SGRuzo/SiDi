package com.SarayDani.sidi

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for the Estados enum to ensure all game states are properly defined.
 */
class EstadosTest {

    @Test
    fun `Estados should have Inicio state`() {
        val estado = Estados.Inicio
        assertNotNull(estado)
        assertEquals("Inicio", estado.name)
    }

    @Test
    fun `Estados should have GenerarSecuencia state`() {
        val estado = Estados.GenerarSecuencia
        assertNotNull(estado)
        assertEquals("GenerarSecuencia", estado.name)
    }

    @Test
    fun `Estados should have IntroducirSecuencia state`() {
        val estado = Estados.IntroducirSecuencia
        assertNotNull(estado)
        assertEquals("IntroducirSecuencia", estado.name)
    }

    @Test
    fun `Estados should have GameOver state`() {
        val estado = Estados.GameOver
        assertNotNull(estado)
        assertEquals("GameOver", estado.name)
    }

    @Test
    fun `Estados should have Pausa state`() {
        val estado = Estados.Pausa
        assertNotNull(estado)
        assertEquals("Pausa", estado.name)
    }

    @Test
    fun `Estados should have exactly 5 values`() {
        val estados = Estados.entries
        assertEquals(5, estados.size)
    }

    @Test
    fun `all Estados values should be accessible`() {
        val expectedStates = listOf("Inicio", "GenerarSecuencia", "IntroducirSecuencia", "GameOver", "Pausa")
        val actualStates = Estados.entries.map { it.name }
        
        assertTrue(actualStates.containsAll(expectedStates))
    }
}
