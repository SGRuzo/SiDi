package com.SarayDani.sidi

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for MyViewModel covering:
 * - Sequence generation
 * - Correct input verification (acierto)
 * - Incorrect input verification (fallo)
 * - All game state transitions
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MyViewModelTest {

    private lateinit var testDispatcher: TestDispatcher
    private lateinit var viewModel: MyViewModel
    private var nextRandomValue = 0

    @Before
    fun setUp() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        // Create ViewModel with predictable random generator
        viewModel = MyViewModel(
            randomGenerator = { nextRandomValue },
            dispatcher = testDispatcher
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ==================== Initial State Tests ====================

    @Test
    fun `initial state is Inicio`() {
        assertEquals(Estados.Inicio, viewModel.estadoActual.value)
    }

    @Test
    fun `initial ronda is 0`() {
        assertEquals(0, viewModel.ronda.value)
    }

    @Test
    fun `initial sequence is empty`() {
        assertTrue(viewModel.secuencia.value.isEmpty())
    }

    @Test
    fun `initial player sequence is empty`() {
        assertTrue(viewModel.secuenciaJugador.value.isEmpty())
    }

    @Test
    fun `initial record is 0`() {
        assertEquals(0, viewModel.record.value)
    }

    // ==================== Sequence Generation Tests ====================

    @Test
    fun `empezarJuego generates one color in sequence`() = runTest {
        nextRandomValue = 2
        viewModel.empezarJuego()
        advanceUntilIdle()
        
        assertEquals(1, viewModel.secuencia.value.size)
        assertEquals(2, viewModel.secuencia.value[0])
    }

    @Test
    fun `empezarJuego sets ronda to 1`() = runTest {
        viewModel.empezarJuego()
        advanceUntilIdle()
        
        assertEquals(1, viewModel.ronda.value)
    }

    @Test
    fun `generarColor adds color to sequence`() {
        nextRandomValue = 1
        viewModel.generarColor()
        
        assertEquals(1, viewModel.secuencia.value.size)
        assertEquals(1, viewModel.secuencia.value[0])
    }

    @Test
    fun `generarColor sets state to GenerarSecuencia`() {
        viewModel.generarColor()
        
        assertEquals(Estados.GenerarSecuencia, viewModel.estadoActual.value)
    }

    @Test
    fun `sequence generates colors in range 0 to 3`() {
        // Test all possible values
        for (expected in 0..3) {
            nextRandomValue = expected
            val newViewModel = MyViewModel(
                randomGenerator = { nextRandomValue },
                dispatcher = testDispatcher
            )
            newViewModel.generarColor()
            assertEquals(expected, newViewModel.secuencia.value[0])
        }
    }

    @Test
    fun `multiple generarColor calls add multiple colors`() {
        nextRandomValue = 0
        viewModel.generarColor()
        nextRandomValue = 1
        viewModel.generarColor()
        nextRandomValue = 2
        viewModel.generarColor()
        
        assertEquals(3, viewModel.secuencia.value.size)
        assertEquals(0, viewModel.secuencia.value[0])
        assertEquals(1, viewModel.secuencia.value[1])
        assertEquals(2, viewModel.secuencia.value[2])
    }

    // ==================== State Transition Tests ====================

    @Test
    fun `empezarJuego changes state from Inicio to GenerarSecuencia`() {
        viewModel.empezarJuego()
        
        assertEquals(Estados.GenerarSecuencia, viewModel.estadoActual.value)
    }

    @Test
    fun `reproducirSecuencia eventually changes state to IntroducirSecuencia`() = runTest {
        nextRandomValue = 0
        viewModel.empezarJuego()
        advanceUntilIdle()
        
        assertEquals(Estados.IntroducirSecuencia, viewModel.estadoActual.value)
    }

    @Test
    fun `correct input keeps state as IntroducirSecuencia when sequence incomplete`() = runTest {
        // Set up a sequence of 2 colors
        nextRandomValue = 1
        viewModel.empezarJuego()
        advanceUntilIdle()
        nextRandomValue = 2
        viewModel.generarColor()
        viewModel.estadoActual.value = Estados.IntroducirSecuencia
        
        // Enter first correct color
        viewModel.introducirSecuencia(1)
        
        assertEquals(Estados.IntroducirSecuencia, viewModel.estadoActual.value)
    }

    @Test
    fun `incorrect input changes state to GameOver`() = runTest {
        nextRandomValue = 1
        viewModel.empezarJuego()
        advanceUntilIdle()
        
        // Enter wrong color (sequence has 1, we enter 0)
        viewModel.introducirSecuencia(0)
        
        assertEquals(Estados.GameOver, viewModel.estadoActual.value)
    }

    @Test
    fun `resetToInicio changes state to Inicio`() = runTest {
        // Start game first
        viewModel.empezarJuego()
        advanceUntilIdle()
        
        viewModel.resetToInicio()
        
        assertEquals(Estados.Inicio, viewModel.estadoActual.value)
    }

    @Test
    fun `gameOver changes state to GameOver`() {
        viewModel.ronda.value = 1
        viewModel.gameOver()
        
        assertEquals(Estados.GameOver, viewModel.estadoActual.value)
    }

    @Test
    fun `state transition Inicio to GenerarSecuencia to IntroducirSecuencia`() = runTest {
        // Initial state
        assertEquals(Estados.Inicio, viewModel.estadoActual.value)
        
        // Start game
        viewModel.empezarJuego()
        assertEquals(Estados.GenerarSecuencia, viewModel.estadoActual.value)
        
        // Wait for sequence to finish
        advanceUntilIdle()
        assertEquals(Estados.IntroducirSecuencia, viewModel.estadoActual.value)
    }

    @Test
    fun `state transition IntroducirSecuencia to GameOver on wrong input`() = runTest {
        nextRandomValue = 0
        viewModel.empezarJuego()
        advanceUntilIdle()
        
        assertEquals(Estados.IntroducirSecuencia, viewModel.estadoActual.value)
        
        viewModel.introducirSecuencia(3) // Wrong input
        
        assertEquals(Estados.GameOver, viewModel.estadoActual.value)
    }

    @Test
    fun `state transition IntroducirSecuencia to GenerarSecuencia on round completion`() = runTest {
        nextRandomValue = 0
        viewModel.empezarJuego()
        advanceUntilIdle()
        
        assertEquals(Estados.IntroducirSecuencia, viewModel.estadoActual.value)
        
        viewModel.introducirSecuencia(0) // Correct input
        
        // State should change to GenerarSecuencia for next round
        assertEquals(Estados.GenerarSecuencia, viewModel.estadoActual.value)
    }

    // ==================== Correct Input Verification (Acierto) Tests ====================

    @Test
    fun `correct single input is accepted`() = runTest {
        nextRandomValue = 2
        viewModel.empezarJuego()
        advanceUntilIdle()
        
        viewModel.introducirSecuencia(2)
        
        // Should not be GameOver
        assertNotEquals(Estados.GameOver, viewModel.estadoActual.value)
    }

    @Test
    fun `correct input increments ronda when sequence complete`() = runTest {
        nextRandomValue = 0
        viewModel.empezarJuego()
        advanceUntilIdle()
        
        assertEquals(1, viewModel.ronda.value)
        
        viewModel.introducirSecuencia(0)
        
        assertEquals(2, viewModel.ronda.value)
    }

    @Test
    fun `correct input clears player sequence when round complete`() = runTest {
        nextRandomValue = 1
        viewModel.empezarJuego()
        advanceUntilIdle()
        
        viewModel.introducirSecuencia(1)
        
        assertTrue(viewModel.secuenciaJugador.value.isEmpty())
    }

    @Test
    fun `correct input generates new color for next round`() = runTest {
        nextRandomValue = 0
        viewModel.empezarJuego()
        advanceUntilIdle()
        
        val initialSequenceSize = viewModel.secuencia.value.size
        
        viewModel.introducirSecuencia(0)
        
        assertEquals(initialSequenceSize + 1, viewModel.secuencia.value.size)
    }

    @Test
    fun `multiple correct inputs complete the round`() = runTest {
        // Start game
        nextRandomValue = 0
        viewModel.empezarJuego()
        advanceUntilIdle()
        
        // Complete round 1
        viewModel.introducirSecuencia(0)
        advanceUntilIdle()
        
        // Now sequence should have 2 elements
        assertEquals(2, viewModel.secuencia.value.size)
        assertEquals(2, viewModel.ronda.value)
    }

    @Test
    fun `player sequence accumulates partial inputs`() = runTest {
        // Set up longer sequence
        nextRandomValue = 0
        viewModel.empezarJuego()
        advanceUntilIdle()
        
        // Add more colors manually for testing
        nextRandomValue = 1
        viewModel.generarColor()
        nextRandomValue = 2
        viewModel.generarColor()
        viewModel.estadoActual.value = Estados.IntroducirSecuencia
        
        // Enter first correct input
        viewModel.introducirSecuencia(0)
        assertEquals(1, viewModel.secuenciaJugador.value.size)
        
        // Enter second correct input
        viewModel.introducirSecuencia(1)
        assertEquals(2, viewModel.secuenciaJugador.value.size)
    }

    // ==================== Incorrect Input Verification (Fallo) Tests ====================

    @Test
    fun `incorrect first input triggers gameOver`() = runTest {
        nextRandomValue = 0
        viewModel.empezarJuego()
        advanceUntilIdle()
        
        viewModel.introducirSecuencia(1) // Wrong
        
        assertEquals(Estados.GameOver, viewModel.estadoActual.value)
    }

    @Test
    fun `incorrect second input triggers gameOver`() = runTest {
        nextRandomValue = 0
        viewModel.empezarJuego()
        advanceUntilIdle()
        
        // Add more to sequence
        nextRandomValue = 1
        viewModel.generarColor()
        viewModel.estadoActual.value = Estados.IntroducirSecuencia
        
        // First correct
        viewModel.introducirSecuencia(0)
        // Second wrong
        viewModel.introducirSecuencia(2)
        
        assertEquals(Estados.GameOver, viewModel.estadoActual.value)
    }

    @Test
    fun `incorrect input in middle of sequence triggers gameOver`() = runTest {
        nextRandomValue = 0
        viewModel.empezarJuego()
        advanceUntilIdle()
        
        // Build longer sequence: 0, 1, 2
        nextRandomValue = 1
        viewModel.generarColor()
        nextRandomValue = 2
        viewModel.generarColor()
        viewModel.estadoActual.value = Estados.IntroducirSecuencia
        
        viewModel.introducirSecuencia(0) // Correct
        viewModel.introducirSecuencia(0) // Wrong (should be 1)
        
        assertEquals(Estados.GameOver, viewModel.estadoActual.value)
    }

    @Test
    fun `gameOver updates record if new high score`() = runTest {
        nextRandomValue = 0
        viewModel.empezarJuego()
        advanceUntilIdle()
        
        // Complete round 1
        viewModel.introducirSecuencia(0)
        advanceUntilIdle()
        
        // Complete round 2
        viewModel.introducirSecuencia(0)
        viewModel.introducirSecuencia(0)
        advanceUntilIdle()
        
        // Complete round 3
        viewModel.introducirSecuencia(0)
        viewModel.introducirSecuencia(0)
        viewModel.introducirSecuencia(0)
        advanceUntilIdle()
        
        // Fail in round 4
        viewModel.introducirSecuencia(1) // Wrong
        
        assertEquals(Estados.GameOver, viewModel.estadoActual.value)
        assertEquals(4, viewModel.record.value)
    }

    @Test
    fun `gameOver does not update record if not new high score`() = runTest {
        // First game - reach round 3
        viewModel.record.value = 5
        viewModel.ronda.value = 3
        
        viewModel.gameOver()
        
        assertEquals(5, viewModel.record.value)
    }

    @Test
    fun `input ignored when state is not IntroducirSecuencia`() = runTest {
        // State is Inicio initially
        viewModel.introducirSecuencia(0)
        
        assertTrue(viewModel.secuenciaJugador.value.isEmpty())
    }

    @Test
    fun `input ignored during GenerarSecuencia state`() {
        viewModel.estadoActual.value = Estados.GenerarSecuencia
        
        viewModel.introducirSecuencia(0)
        
        assertTrue(viewModel.secuenciaJugador.value.isEmpty())
    }

    @Test
    fun `input ignored during GameOver state`() {
        viewModel.estadoActual.value = Estados.GameOver
        
        viewModel.introducirSecuencia(0)
        
        assertTrue(viewModel.secuenciaJugador.value.isEmpty())
    }

    @Test
    fun `input ignored during Pausa state`() {
        viewModel.estadoActual.value = Estados.Pausa
        
        viewModel.introducirSecuencia(0)
        
        assertTrue(viewModel.secuenciaJugador.value.isEmpty())
    }

    // ==================== Record Tests ====================

    @Test
    fun `record updates when current round exceeds previous record`() {
        viewModel.record.value = 2
        viewModel.ronda.value = 3
        
        viewModel.gameOver()
        
        assertEquals(3, viewModel.record.value)
    }

    @Test
    fun `record stays same when current round equals previous record`() {
        viewModel.record.value = 3
        viewModel.ronda.value = 3
        
        viewModel.gameOver()
        
        assertEquals(3, viewModel.record.value)
    }

    @Test
    fun `record stays same when current round is less than previous record`() {
        viewModel.record.value = 5
        viewModel.ronda.value = 2
        
        viewModel.gameOver()
        
        assertEquals(5, viewModel.record.value)
    }

    @Test
    fun `record persists across multiple games`() = runTest {
        // First game - reach round 3
        nextRandomValue = 0
        viewModel.empezarJuego()
        advanceUntilIdle()
        viewModel.introducirSecuencia(0)
        advanceUntilIdle()
        viewModel.introducirSecuencia(0)
        viewModel.introducirSecuencia(0)
        advanceUntilIdle()
        viewModel.introducirSecuencia(1) // Fail at round 3
        
        assertEquals(3, viewModel.record.value)
        
        // Second game - reach only round 2
        viewModel.empezarJuego()
        advanceUntilIdle()
        viewModel.introducirSecuencia(0)
        advanceUntilIdle()
        viewModel.introducirSecuencia(1) // Fail at round 2
        
        // Record should still be 3
        assertEquals(3, viewModel.record.value)
    }

    // ==================== Reset Tests ====================

    @Test
    fun `resetToInicio clears sequence`() = runTest {
        viewModel.empezarJuego()
        advanceUntilIdle()
        
        assertFalse(viewModel.secuencia.value.isEmpty())
        
        viewModel.resetToInicio()
        
        assertTrue(viewModel.secuencia.value.isEmpty())
    }

    @Test
    fun `resetToInicio clears player sequence`() = runTest {
        viewModel.empezarJuego()
        advanceUntilIdle()
        viewModel.introducirSecuencia(viewModel.secuencia.value[0])
        // Don't let it complete the round
        viewModel.estadoActual.value = Estados.IntroducirSecuencia
        
        viewModel.resetToInicio()
        
        assertTrue(viewModel.secuenciaJugador.value.isEmpty())
    }

    @Test
    fun `resetToInicio sets ronda to 0`() = runTest {
        viewModel.empezarJuego()
        advanceUntilIdle()
        
        assertEquals(1, viewModel.ronda.value)
        
        viewModel.resetToInicio()
        
        assertEquals(0, viewModel.ronda.value)
    }

    @Test
    fun `resetToInicio preserves record`() = runTest {
        viewModel.record.value = 5
        
        viewModel.resetToInicio()
        
        assertEquals(5, viewModel.record.value)
    }

    // ==================== Game Flow Tests ====================

    @Test
    fun `complete game flow from start to game over`() = runTest {
        // Start
        assertEquals(Estados.Inicio, viewModel.estadoActual.value)
        
        nextRandomValue = 0
        viewModel.empezarJuego()
        assertEquals(Estados.GenerarSecuencia, viewModel.estadoActual.value)
        
        advanceUntilIdle()
        assertEquals(Estados.IntroducirSecuencia, viewModel.estadoActual.value)
        
        // Complete round 1
        viewModel.introducirSecuencia(0)
        assertEquals(Estados.GenerarSecuencia, viewModel.estadoActual.value)
        assertEquals(2, viewModel.ronda.value)
        
        advanceUntilIdle()
        assertEquals(Estados.IntroducirSecuencia, viewModel.estadoActual.value)
        
        // Fail in round 2
        viewModel.introducirSecuencia(3) // Wrong
        assertEquals(Estados.GameOver, viewModel.estadoActual.value)
        assertEquals(2, viewModel.record.value)
    }

    @Test
    fun `empezarJuego clears previous game data`() = runTest {
        // Play first game
        nextRandomValue = 1
        viewModel.empezarJuego()
        advanceUntilIdle()
        viewModel.introducirSecuencia(0) // Wrong
        
        assertEquals(Estados.GameOver, viewModel.estadoActual.value)
        
        // Start new game
        nextRandomValue = 2
        viewModel.empezarJuego()
        
        assertEquals(1, viewModel.ronda.value)
        assertEquals(1, viewModel.secuencia.value.size)
        assertEquals(2, viewModel.secuencia.value[0])
        assertTrue(viewModel.secuenciaJugador.value.isEmpty())
    }

    @Test
    fun `multiple rounds increase sequence length`() = runTest {
        nextRandomValue = 0
        viewModel.empezarJuego()
        advanceUntilIdle()
        
        assertEquals(1, viewModel.secuencia.value.size)
        
        // Complete round 1
        viewModel.introducirSecuencia(0)
        advanceUntilIdle()
        
        assertEquals(2, viewModel.secuencia.value.size)
        
        // Complete round 2
        viewModel.introducirSecuencia(0)
        viewModel.introducirSecuencia(0)
        advanceUntilIdle()
        
        assertEquals(3, viewModel.secuencia.value.size)
    }
}
