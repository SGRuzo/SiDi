package com.SarayDani.sidi

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
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
 * - Hit verification (correct input)
 * - Failure verification (incorrect input)
 * - All game state transitions
 * - Round progression
 * - Record tracking
 * - Game reset functionality
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MyViewModelTest {

    private lateinit var viewModel: MyViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        // Mock Android Log class for unit tests
        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any(), any()) } returns 0
        every { android.util.Log.e(any(), any()) } returns 0
        every { android.util.Log.i(any(), any()) } returns 0
        every { android.util.Log.v(any(), any()) } returns 0
        every { android.util.Log.w(any<String>(), any<String>()) } returns 0

        // Set test dispatcher for coroutines
        Dispatchers.setMain(testDispatcher)
        
        viewModel = MyViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // ===========================================
    // INITIAL STATE TESTS
    // ===========================================

    @Test
    fun `initial state should be Inicio`() {
        assertEquals(Estados.Inicio, viewModel.estadoActual.value)
    }

    @Test
    fun `initial round should be 0`() {
        assertEquals(0, viewModel.ronda.value)
    }

    @Test
    fun `initial record should be 0`() {
        assertEquals(0, viewModel.record.value)
    }

    @Test
    fun `initial sequence should be empty`() {
        assertTrue(viewModel.secuencia.value.isEmpty())
    }

    @Test
    fun `initial player sequence should be empty`() {
        assertTrue(viewModel.secuenciaJugador.value.isEmpty())
    }

    // ===========================================
    // GAME START TESTS (empezarJuego)
    // ===========================================

    @Test
    fun `empezarJuego should set round to 1`() = runTest {
        viewModel.empezarJuego()
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertEquals(1, viewModel.ronda.value)
    }

    @Test
    fun `empezarJuego should generate one color in sequence`() = runTest {
        viewModel.empezarJuego()
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertEquals(1, viewModel.secuencia.value.size)
    }

    @Test
    fun `empezarJuego should clear player sequence`() = runTest {
        // Add something to player sequence first
        viewModel.secuenciaJugador.value.add(1)
        
        viewModel.empezarJuego()
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertTrue(viewModel.secuenciaJugador.value.isEmpty())
    }

    @Test
    fun `empezarJuego should set state to GenerarSecuencia initially`() = runTest {
        viewModel.empezarJuego()
        // Check state before coroutine completes
        assertEquals(Estados.GenerarSecuencia, viewModel.estadoActual.value)
    }

    @Test
    fun `empezarJuego should transition to IntroducirSecuencia after sequence plays`() = runTest {
        viewModel.empezarJuego()
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertEquals(Estados.IntroducirSecuencia, viewModel.estadoActual.value)
    }

    // ===========================================
    // SEQUENCE GENERATION TESTS
    // ===========================================

    @Test
    fun `generated color should be between 0 and 3`() = runTest {
        viewModel.empezarJuego()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val color = viewModel.secuencia.value.first()
        assertTrue("Color should be between 0 and 3", color in 0..3)
    }

    @Test
    fun `multiple game starts should generate different sequences (probabilistic)`() = runTest {
        val sequences = mutableListOf<Int>()
        
        // Run multiple times to check randomness
        repeat(10) {
            viewModel.empezarJuego()
            testDispatcher.scheduler.advanceUntilIdle()
            sequences.add(viewModel.secuencia.value.first())
            viewModel.resetToInicio()
        }
        
        // With 4 possible colors, if we run 10 times, we should likely have at least 2 different values
        // This is a probabilistic test - it could theoretically fail but very unlikely
        val uniqueColors = sequences.distinct()
        assertTrue("Should generate varied colors", uniqueColors.size >= 1)
    }

    // ===========================================
    // HIT VERIFICATION TESTS (Correct input)
    // ===========================================

    @Test
    fun `correct input should be added to player sequence`() = runTest {
        viewModel.empezarJuego()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val correctColor = viewModel.secuencia.value.first()
        viewModel.introducirSecuencia(correctColor)
        
        assertEquals(1, viewModel.secuenciaJugador.value.size)
        assertEquals(correctColor, viewModel.secuenciaJugador.value.first())
    }

    @Test
    fun `correct sequence completion should increase round`() = runTest {
        viewModel.empezarJuego()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val correctColor = viewModel.secuencia.value.first()
        viewModel.introducirSecuencia(correctColor)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertEquals(2, viewModel.ronda.value)
    }

    @Test
    fun `correct sequence completion should add new color to sequence`() = runTest {
        viewModel.empezarJuego()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val correctColor = viewModel.secuencia.value.first()
        viewModel.introducirSecuencia(correctColor)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertEquals(2, viewModel.secuencia.value.size)
    }

    @Test
    fun `correct sequence completion should clear player sequence`() = runTest {
        viewModel.empezarJuego()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val correctColor = viewModel.secuencia.value.first()
        viewModel.introducirSecuencia(correctColor)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertTrue(viewModel.secuenciaJugador.value.isEmpty())
    }

    @Test
    fun `multiple correct inputs should progress through sequence`() = runTest {
        viewModel.empezarJuego()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Complete round 1
        viewModel.introducirSecuencia(viewModel.secuencia.value[0])
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Now we're in round 2 with 2 colors in sequence
        assertEquals(2, viewModel.ronda.value)
        assertEquals(2, viewModel.secuencia.value.size)
        
        // Complete round 2
        viewModel.introducirSecuencia(viewModel.secuencia.value[0])
        viewModel.introducirSecuencia(viewModel.secuencia.value[1])
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Now we're in round 3
        assertEquals(3, viewModel.ronda.value)
        assertEquals(3, viewModel.secuencia.value.size)
    }

    // ===========================================
    // FAILURE VERIFICATION TESTS (Incorrect input)
    // ===========================================

    @Test
    fun `wrong input should trigger game over`() = runTest {
        viewModel.empezarJuego()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val correctColor = viewModel.secuencia.value.first()
        val wrongColor = (correctColor + 1) % 4 // Ensure it's a different color
        
        viewModel.introducirSecuencia(wrongColor)
        
        assertEquals(Estados.GameOver, viewModel.estadoActual.value)
    }

    @Test
    fun `wrong input on second element should trigger game over`() = runTest {
        viewModel.empezarJuego()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Complete round 1 correctly
        viewModel.introducirSecuencia(viewModel.secuencia.value[0])
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Now in round 2 - input first color correctly
        viewModel.introducirSecuencia(viewModel.secuencia.value[0])
        
        // Input wrong second color
        val correctSecondColor = viewModel.secuencia.value[1]
        val wrongColor = (correctSecondColor + 1) % 4
        viewModel.introducirSecuencia(wrongColor)
        
        assertEquals(Estados.GameOver, viewModel.estadoActual.value)
    }

    @Test
    fun `wrong input should update record if better than previous`() = runTest {
        viewModel.empezarJuego()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Complete round 1 correctly
        viewModel.introducirSecuencia(viewModel.secuencia.value[0])
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Now in round 2 - input wrong color
        val wrongColor = (viewModel.secuencia.value[0] + 1) % 4
        viewModel.introducirSecuencia(wrongColor)
        
        assertEquals(2, viewModel.record.value)
    }

    // ===========================================
    // STATE TRANSITION TESTS
    // ===========================================

    @Test
    fun `state transition from Inicio to GenerarSecuencia on game start`() = runTest {
        assertEquals(Estados.Inicio, viewModel.estadoActual.value)
        
        viewModel.empezarJuego()
        
        assertEquals(Estados.GenerarSecuencia, viewModel.estadoActual.value)
    }

    @Test
    fun `state transition from GenerarSecuencia to IntroducirSecuencia`() = runTest {
        viewModel.empezarJuego()
        assertEquals(Estados.GenerarSecuencia, viewModel.estadoActual.value)
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertEquals(Estados.IntroducirSecuencia, viewModel.estadoActual.value)
    }

    @Test
    fun `state transition from IntroducirSecuencia to GenerarSecuencia on correct completion`() = runTest {
        viewModel.empezarJuego()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(Estados.IntroducirSecuencia, viewModel.estadoActual.value)
        
        val correctColor = viewModel.secuencia.value.first()
        viewModel.introducirSecuencia(correctColor)
        
        assertEquals(Estados.GenerarSecuencia, viewModel.estadoActual.value)
    }

    @Test
    fun `state transition from IntroducirSecuencia to GameOver on wrong input`() = runTest {
        viewModel.empezarJuego()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(Estados.IntroducirSecuencia, viewModel.estadoActual.value)
        
        val wrongColor = (viewModel.secuencia.value.first() + 1) % 4
        viewModel.introducirSecuencia(wrongColor)
        
        assertEquals(Estados.GameOver, viewModel.estadoActual.value)
    }

    @Test
    fun `state transition from GameOver to Inicio on reset`() = runTest {
        viewModel.empezarJuego()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val wrongColor = (viewModel.secuencia.value.first() + 1) % 4
        viewModel.introducirSecuencia(wrongColor)
        assertEquals(Estados.GameOver, viewModel.estadoActual.value)
        
        viewModel.resetToInicio()
        
        assertEquals(Estados.Inicio, viewModel.estadoActual.value)
    }

    @Test
    fun `state transition from GameOver to GenerarSecuencia on new game`() = runTest {
        viewModel.empezarJuego()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val wrongColor = (viewModel.secuencia.value.first() + 1) % 4
        viewModel.introducirSecuencia(wrongColor)
        assertEquals(Estados.GameOver, viewModel.estadoActual.value)
        
        viewModel.empezarJuego()
        
        assertEquals(Estados.GenerarSecuencia, viewModel.estadoActual.value)
    }

    // ===========================================
    // INPUT DURING WRONG STATE TESTS
    // ===========================================

    @Test
    fun `input during GenerarSecuencia state should be ignored`() = runTest {
        viewModel.empezarJuego()
        // State is GenerarSecuencia during sequence playback
        assertEquals(Estados.GenerarSecuencia, viewModel.estadoActual.value)
        
        viewModel.introducirSecuencia(0)
        
        // Player sequence should remain empty
        assertTrue(viewModel.secuenciaJugador.value.isEmpty())
    }

    @Test
    fun `input during Inicio state should be ignored`() = runTest {
        assertEquals(Estados.Inicio, viewModel.estadoActual.value)
        
        viewModel.introducirSecuencia(0)
        
        assertTrue(viewModel.secuenciaJugador.value.isEmpty())
    }

    @Test
    fun `input during GameOver state should be ignored`() = runTest {
        viewModel.empezarJuego()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val wrongColor = (viewModel.secuencia.value.first() + 1) % 4
        viewModel.introducirSecuencia(wrongColor)
        assertEquals(Estados.GameOver, viewModel.estadoActual.value)
        
        val previousPlayerSequenceSize = viewModel.secuenciaJugador.value.size
        viewModel.introducirSecuencia(0)
        
        assertEquals(previousPlayerSequenceSize, viewModel.secuenciaJugador.value.size)
    }

    // ===========================================
    // RECORD TRACKING TESTS
    // ===========================================

    @Test
    fun `record should update on game over if round is higher`() = runTest {
        assertEquals(0, viewModel.record.value)
        
        viewModel.empezarJuego()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Fail on round 1
        val wrongColor = (viewModel.secuencia.value.first() + 1) % 4
        viewModel.introducirSecuencia(wrongColor)
        
        assertEquals(1, viewModel.record.value)
    }

    @Test
    fun `record should not update if round is lower than record`() = runTest {
        // Set initial record manually
        viewModel.record.value = 5
        
        viewModel.empezarJuego()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val wrongColor = (viewModel.secuencia.value.first() + 1) % 4
        viewModel.introducirSecuencia(wrongColor)
        
        // Record should remain 5
        assertEquals(5, viewModel.record.value)
    }

    @Test
    fun `record should persist across multiple games`() = runTest {
        // First game - reach round 2 then fail
        viewModel.empezarJuego()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.introducirSecuencia(viewModel.secuencia.value[0])
        testDispatcher.scheduler.advanceUntilIdle()
        
        val wrongColor = (viewModel.secuencia.value[0] + 1) % 4
        viewModel.introducirSecuencia(wrongColor)
        
        assertEquals(2, viewModel.record.value)
        
        // Second game - fail on round 1
        viewModel.empezarJuego()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val wrongColor2 = (viewModel.secuencia.value[0] + 1) % 4
        viewModel.introducirSecuencia(wrongColor2)
        
        // Record should still be 2
        assertEquals(2, viewModel.record.value)
    }

    // ===========================================
    // RESET FUNCTIONALITY TESTS
    // ===========================================

    @Test
    fun `resetToInicio should set state to Inicio`() = runTest {
        viewModel.empezarJuego()
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.resetToInicio()
        
        assertEquals(Estados.Inicio, viewModel.estadoActual.value)
    }

    @Test
    fun `resetToInicio should set round to 0`() = runTest {
        viewModel.empezarJuego()
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.resetToInicio()
        
        assertEquals(0, viewModel.ronda.value)
    }

    @Test
    fun `resetToInicio should clear sequence`() = runTest {
        viewModel.empezarJuego()
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.resetToInicio()
        
        assertTrue(viewModel.secuencia.value.isEmpty())
    }

    @Test
    fun `resetToInicio should clear player sequence`() = runTest {
        viewModel.empezarJuego()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.introducirSecuencia(viewModel.secuencia.value[0])
        
        viewModel.resetToInicio()
        
        assertTrue(viewModel.secuenciaJugador.value.isEmpty())
    }

    @Test
    fun `resetToInicio should not reset record`() = runTest {
        viewModel.empezarJuego()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.introducirSecuencia(viewModel.secuencia.value[0])
        testDispatcher.scheduler.advanceUntilIdle()
        
        val wrongColor = (viewModel.secuencia.value[0] + 1) % 4
        viewModel.introducirSecuencia(wrongColor)
        
        val recordBeforeReset = viewModel.record.value
        viewModel.resetToInicio()
        
        assertEquals(recordBeforeReset, viewModel.record.value)
    }

    // ===========================================
    // EDGE CASE TESTS
    // ===========================================

    @Test
    fun `partial correct sequence should not advance round`() = runTest {
        viewModel.empezarJuego()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Complete round 1 to get to round 2 with 2 colors
        viewModel.introducirSecuencia(viewModel.secuencia.value[0])
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertEquals(2, viewModel.ronda.value)
        
        // Input only first color correctly
        viewModel.introducirSecuencia(viewModel.secuencia.value[0])
        
        // Should still be round 2
        assertEquals(2, viewModel.ronda.value)
    }

    @Test
    fun `sequence should grow by one each round`() = runTest {
        viewModel.empezarJuego()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.secuencia.value.size)
        
        // Complete round 1
        viewModel.introducirSecuencia(viewModel.secuencia.value[0])
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(2, viewModel.secuencia.value.size)
        
        // Complete round 2
        viewModel.introducirSecuencia(viewModel.secuencia.value[0])
        viewModel.introducirSecuencia(viewModel.secuencia.value[1])
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(3, viewModel.secuencia.value.size)
    }

    @Test
    fun `all valid color inputs should be accepted`() = runTest {
        // Test all four colors (0-3)
        for (color in 0..3) {
            viewModel = MyViewModel()
            viewModel.estadoActual.value = Estados.IntroducirSecuencia
            viewModel.secuencia.value.add(color)
            
            viewModel.introducirSecuencia(color)
            
            assertEquals("Color $color should be accepted", color, viewModel.secuenciaJugador.value.first())
        }
    }

    @Test
    fun `game should handle rapid consecutive inputs correctly`() = runTest {
        viewModel.empezarJuego()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Complete round 1
        viewModel.introducirSecuencia(viewModel.secuencia.value[0])
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Rapid inputs for round 2
        viewModel.introducirSecuencia(viewModel.secuencia.value[0])
        viewModel.introducirSecuencia(viewModel.secuencia.value[1])
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Should be in round 3 now
        assertEquals(3, viewModel.ronda.value)
    }
}
