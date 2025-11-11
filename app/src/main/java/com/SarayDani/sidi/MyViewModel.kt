package com.SarayDani.sidi

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class MyViewModel(): ViewModel() {

    private val TAG_LOG = "miDebug"
    val estadoActual: MutableStateFlow<Estados> = MutableStateFlow(Estados.Inicio)

    var _numbers = mutableListOf<Int>()

    init {
        // estado inicial
        Log.d(TAG_LOG, "Inicializamos ViewModel - Estado: ${estadoActual.value}")
    }

    fun crearRandom() {
        estadoActual.value = Estados.GenerarSecuencia
        _numbers.add((0..3).random())
        Log.d(TAG_LOG, "creamos random ${_numbers} - Estado: ${estadoActual.value}")

    }
}