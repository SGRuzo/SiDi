package com.SarayDani.sidi

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MyViewModelTest {

    private lateinit var vm: MyViewModel


    @Test
    fun testEmpezarJuego() = runTest {
        vm = MyViewModel()
        vm.empezarJuego()
        advanceUntilIdle()

        assertEquals(1, vm.ronda.value)
        assertEquals(1, vm.secuencia.value.size)
        assertEquals(Estados.GenerarSecuencia, vm.estadoActual.value)
    }


    @Test
    fun testGenerarSecuencia() = runTest {
        vm = MyViewModel()
        vm.empezarJuego()
        advanceUntilIdle()

        val size1 = vm.secuencia.value.size
        vm.introducirSecuencia(vm.secuencia.value[0]) // acierta y genera nuevo color
        advanceTimeBy(1500L) // darle tiempo a generar nuevo color
        advanceUntilIdle()


        val size2 = vm.secuencia.value.size

        assertTrue(size2 == size1)
    }


    @Test
    fun testAciertoSecuencia() = runTest {
        vm = MyViewModel()
        vm.empezarJuego()
        advanceUntilIdle()

        val rondaInicial = vm.ronda.value
        val colorCorrecto = vm.secuencia.value[0]

        vm.introducirSecuencia(colorCorrecto)
        advanceUntilIdle()

        assertEquals(rondaInicial, vm.ronda.value)
    }


    @Test
    fun testFalloSecuencia() = runTest {
        vm = MyViewModel()
        vm.empezarJuego()

        // Espera hasta que el estado sea el turno del jugador
        while (vm.estadoActual.value != Estados.IntroducirSecuencia) {
            advanceTimeBy(100L)
            advanceUntilIdle()
        }

        // Introducimos un color incorrecto
        val colorIncorrecto = (0..3).first { it != vm.secuencia.value[0] }
        vm.introducirSecuencia(colorIncorrecto)

        // Procesa la corutina que cambiará a GameOver
        advanceUntilIdle()

        assertEquals(Estados.GameOver, vm.estadoActual.value)
    }


    @Test
    fun testRecord() = runTest {
        vm = MyViewModel()

        // Simula rondas anteriores
        repeat(5) {
            vm.empezarJuego()
            advanceUntilIdle()
            while (vm.estadoActual.value != Estados.IntroducirSecuencia) {
                advanceTimeBy(100L)
                advanceUntilIdle()
            }
            // Introduce la secuencia correcta (el primer color)
            vm.introducirSecuencia(vm.secuencia.value.first())
            advanceUntilIdle()
        }

        // Ahora forzamos fallo
        while (vm.estadoActual.value != Estados.IntroducirSecuencia) {
            advanceTimeBy(100L)
            advanceUntilIdle()
        }
        vm.introducirSecuencia(99) // falla
        advanceUntilIdle()

        assertEquals(2, vm.record.value)
    }
}
